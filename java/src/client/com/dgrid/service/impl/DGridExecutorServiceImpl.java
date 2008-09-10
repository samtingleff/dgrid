package com.dgrid.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.service.DGridExecutorService;
import com.dgrid.threads.DGridSimpleThreadFactory;
import com.dgrid.threads.DGridTaskListener;

public class DGridExecutorServiceImpl extends ScheduledThreadPoolExecutor
		implements DGridExecutorService, ExecutorService {
	private Log log = LogFactory.getLog(getClass());

	private List<DGridTaskListener> listeners = new ArrayList<DGridTaskListener>(
			1);

	private int maxThreadCount = 1;

	public DGridExecutorServiceImpl(int maxCpuCores, int threadsPerCore) {
		super(
				((Runtime.getRuntime().availableProcessors() < maxCpuCores) ? Runtime
						.getRuntime().availableProcessors()
						: maxCpuCores), new DGridSimpleThreadFactory());
		log.trace("DGridExecutorServiceImpl()");
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		int cpuCores = (availableProcessors < maxCpuCores) ? availableProcessors
				: maxCpuCores;
		this.maxThreadCount = cpuCores * threadsPerCore;
	}

	public void beforeExecute(Thread t, Runnable r) {
		log.trace("beforeExecute()");
		super.beforeExecute(t, r);
		int activeThreadCount = getActiveCount();
		for (DGridTaskListener listener : listeners) {
			try {
				listener.beforeExecute(t, r, activeThreadCount, maxThreadCount);
			} catch (Exception e) {
				log.warn("Exception calling beforeExecute() on listener", e);
			}
		}
	}

	public void afterExecute(Runnable r, Throwable t) {
		log.trace("afterExecute()");
		super.afterExecute(r, t);
		int activeThreadCount = getActiveCount();
		for (DGridTaskListener listener : listeners) {
			try {
				listener.afterExecute(r, t, activeThreadCount, maxThreadCount);
			} catch (Exception e) {
				log.warn("Exception calling afterExecute() on listener", e);
			}
		}
	}

	public void addListener(DGridTaskListener listener) {
		log.trace("addListener()");
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeListener(DGridTaskListener listener) {
		log.trace("removeListener()");
		listeners.remove(listener);
	}

	public int getActiveCount() {
		log.trace("getActiveCount()");
		return super.getActiveCount();
	}

}
