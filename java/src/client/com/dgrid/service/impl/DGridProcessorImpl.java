package com.dgrid.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Host;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridExecutorService;
import com.dgrid.service.DGridPluginManager;
import com.dgrid.service.DGridProcessor;
import com.dgrid.threads.DGridRunnable;
import com.dgrid.threads.DGridTaskListener;
import com.dgrid.util.io.HostnameDiscovery;

public class DGridProcessorImpl implements Runnable, DGridProcessor,
		DGridTaskListener {
	private Log log = LogFactory.getLog(getClass());

	private DGridExecutorService executor;

	private DGridClient client;

	private DGridPluginManager pluginMgr;

	private int threadsPerCore = 1;

	private int maxCpuCores = 0;

	private int threadCount = 1;

	private long sleepTime = 30000;

	private Object lock = new Object();

	public void setPluginManager(DGridPluginManager mgr) {
		this.pluginMgr = mgr;
	}

	public void setGridClient(DGridClient client) {
		this.client = client;
	}

	public void setThreadPool(DGridExecutorService service) {
		this.executor = service;
	}

	public void setSleepTime(long sleepTimeMillis) {
		this.sleepTime = sleepTimeMillis;
	}

	public void setThreadsPerCore(int threadsPerCore) {
		this.threadsPerCore = threadsPerCore;
	}

	public void setMaxCpuCores(int maxCpuCores) {
		this.maxCpuCores = maxCpuCores;
	}

	public void init() throws Exception {
		log.trace("init()");

		this.threadCount = getThreadCount();

		// call this before initializing plugins
		// want to guarantee that getHost() returns a valid host
		Host host = client.registerHost(HostnameDiscovery.getHostname());

		Map<String, String> facts = new HashMap<String, String>(1);
		facts.put("threads.total", Integer.toString(threadCount));
		facts.put("threads.perCore", Integer.toString(threadsPerCore));
		client.setHostFacts(host.getId(), facts);

		pluginMgr.init();
		pluginMgr.start();

		executor.addListener(this);
	}

	public void stop() {
		log.trace("stop()");
		try {
			executor.shutdown(); // Disable new tasks from being submitted
			// Wait a while for existing tasks to terminate
			log.info("Waiting two minutes for existing tasks to stop");
			if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				executor.shutdownNow();
				log.info("Waiting for tasks to cancel");
				// Wait a while for tasks to respond to being cancelled
				if (!executor.awaitTermination(60, TimeUnit.SECONDS))
					log.error("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		pluginMgr.stop();
	}

	public void run() {
		log.trace("run()");
		for (;;) {
			try {
				synchronized (lock) {
					int activeCount = executor.getActiveCount();
					if (activeCount >= threadCount) {
						if (log.isDebugEnabled()) {
							log.debug(String.format(
									"activeCount (%1$d) >= threadCount (%2$d)",
									activeCount, threadCount));
						}
						if (log.isDebugEnabled())
							log.debug("Calling wait() on lock object");
						try {
							lock.wait(sleepTime);
							if (log.isDebugEnabled())
								log.debug("wait() ended");
						} catch (IllegalMonitorStateException e) {
							log
									.warn(
											"IllegalMonitorStateException caught calling wait()",
											e);
						} catch (InterruptedException e) {
							log.warn("InterruptedException calling wait()", e);
						}
						continue;
					}
				}
				Joblet joblet = client.getWork();
				DGridRunnable r = new DGridRunnable(client, joblet);
				executor.submit(r);
			} catch (NoWorkAvailable e) {
				if (log.isDebugEnabled()) {
					log.debug("No work available");
				}
				sleep();
			} catch (TransportException e) {
				log.error("TransportException in run() loop", e);
				sleep();
			} catch (Exception e) {
				log.error("Exception in run() loop", e);
			}
		}
	}

	public void beforeExecute(Thread t, Runnable r, int activeThreadCount,
			int maxThreadCount) {
		log.trace("beforeExecute()");
	}

	public void afterExecute(Runnable r, Throwable t, int activeThreadCount,
			int maxThreadCount) {
		log.trace("afterExecute()");
		synchronized (lock) {
			try {
				lock.notify();
			} catch (IllegalMonitorStateException e) {
				log
						.warn(
								"IllegalMonitorStateException caught in taskCompleted()",
								e);
			}
		}
	}

	public DGridClient getGridClient() {
		log.trace("getGridClient()");
		return client;
	}

	private int getThreadCount() {
		if (maxCpuCores > 0) {
			return maxCpuCores * threadsPerCore;
		} else {
			return Runtime.getRuntime().availableProcessors() * threadsPerCore;
		}
	}

	private void sleep() {
		log.trace("sleep()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("Sleeping for %1$d millis", sleepTime));
		}
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			log.warn("InterruptedException in sleep()", e);
		}
	}

}
