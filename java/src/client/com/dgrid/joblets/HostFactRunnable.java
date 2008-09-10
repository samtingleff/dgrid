package com.dgrid.joblets;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.dgrid.gen.Host;
import com.dgrid.plugin.Plugin;
import com.dgrid.plugin.PluginManager;
import com.dgrid.service.DGridClient;

public class HostFactRunnable implements Runnable {
	private Log log = LogFactory.getLog(getClass());

	private ApplicationContext ctx;

	public HostFactRunnable() {
	}

	public HostFactRunnable(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	public void run() {
		log.trace("run()");
		try {
			doWork();
		} catch (Throwable t) {
			log.error("Throwable caught in doRun()", t);
		}
	}

	public void execute(DGridClient gridClient) throws Exception {
		log.trace("execute()");
		Host host = gridClient.getHost();
		Properties props = System.getProperties();
		Map<String, String> facts = new HashMap<String, String>();
		Set<Map.Entry<Object, Object>> entries = props.entrySet();
		for (Map.Entry<Object, Object> entry : entries) {
			facts.put((String) entry.getKey(), (String) entry.getValue());
		}
		setSystemFacts(facts);
		setRuntimeMXBeanFacts(facts);
		setMemoryMXBeanFacts(facts);
		setOperatingSystemMXBeanFacts(facts);
		setThreadMXBeanFacts(facts);
		setGCMXBeanFacts(facts);
		setPluginFacts(facts);
		gridClient.setHostFacts(host.getId(), facts);
	}

	private void doWork() throws Exception {
		log.trace("doWork()");
		DGridClient gridClient = (DGridClient) ctx.getBean(DGridClient.NAME);
		this.execute(gridClient);
	}

	private void setSystemFacts(Map<String, String> facts)
			throws UnknownHostException {
		log.trace("setSystemFacts()");
		InetAddress inet = InetAddress.getLocalHost();
		String ip = inet.getHostAddress();
		String hostname = inet.getHostName();
		Runtime runtime = Runtime.getRuntime();
		int cpuCores = runtime.availableProcessors();
		int activeThreads = getActiveThreadCount();
		long freeMemory = runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();

		facts.put("cpu.cores", Integer.toString(cpuCores));

		facts.put("cpu.cores", Integer.toString(cpuCores));
		facts.put("threads.active", Integer.toString(activeThreads));
		facts.put("network.hostname", hostname);
		facts.put("network.ip", ip);
		facts.put("memory.free", Long.toString(freeMemory));
		facts.put("memory.max", Long.toString(maxMemory));
		facts.put("memory.total", Long.toString(totalMemory));
	}

	private void setRuntimeMXBeanFacts(Map<String, String> facts)
			throws UnknownHostException {
		log.trace("setRuntimeMXBeanFacts()");
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		facts.put("jvm.startTime", Long.toString(runtime.getStartTime()));
		facts.put("jvm.uptime.millis", Long.toString(runtime.getUptime()));
		facts.put("jvm.uptime.seconds", Long
				.toString(runtime.getUptime() / 1000l));
		facts.put("jvm.uptime.minutes", Long.toString(runtime.getUptime()
				/ (1000l * 60l)));
		facts.put("jvm.uptime.hours", Long.toString(runtime.getUptime()
				/ (1000l * 60l * 60l)));
		facts.put("jvm.uptime.days", Long.toString(runtime.getUptime()
				/ (1000l * 60l * 60l * 24l)));

		long duration = runtime.getUptime();
		long ms = duration % 1000l;
		duration = duration / 1000l;
		long s = duration % 60l;
		duration = duration / 60l;
		long m = duration % 60l;
		duration = duration / 60l;
		long h = duration % 60l;
		duration = duration / 60l;
		long d = duration % 24l;

		facts.put("jvm.uptime", String.format("%1$d days %2$d:%3$d:%4$d.%5$d",
				d, h, m, s, ms));
	}

	private void setMemoryMXBeanFacts(Map<String, String> facts)
			throws UnknownHostException {
		log.trace("setMemoryMXBeanFacts()");
		MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = memory.getHeapMemoryUsage();
		MemoryUsage nonheap = memory.getNonHeapMemoryUsage();

		setMemoryUsageFacts(facts, "heap", heap);
		setMemoryUsageFacts(facts, "nonheap", nonheap);

		facts.put("memory.gc.objectsPending", Integer.toString(memory
				.getObjectPendingFinalizationCount()));
	}

	private void setMemoryUsageFacts(Map<String, String> facts, String type,
			MemoryUsage details) {
		log.trace("setMemoryUsageFacts()");
		facts.put(String.format("memory.%1$s.committed", type), Long
				.toString(details.getCommitted()));
		facts.put(String.format("memory.%1$s.init", type), Long
				.toString(details.getInit()));
		facts.put(String.format("memory.%1$s.max", type), Long.toString(details
				.getMax()));
		facts.put(String.format("memory.%1$s.used", type), Long
				.toString(details.getUsed()));
	}

	private void setOperatingSystemMXBeanFacts(Map<String, String> facts) {
		log.trace("setOperatingSystemMXBeanFacts()");
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		facts.put("system.load", Double.toString(os.getSystemLoadAverage()));
	}

	private void setThreadMXBeanFacts(Map<String, String> facts) {
		log.trace("setThreadMXBeanFacts()");
		ThreadMXBean mx = ManagementFactory.getThreadMXBean();
		facts.put("threads.current.cpuTime", Long.toString(mx
				.getCurrentThreadCpuTime()));
		facts.put("threads.current.userTime", Long.toString(mx
				.getCurrentThreadUserTime()));
		facts.put("threads.total.started", Long.toString(mx
				.getTotalStartedThreadCount()));
		facts.put("threads.daemon.count", Long.toString(mx
				.getDaemonThreadCount()));
		facts.put("threads.peak.count", Long.toString(mx.getPeakThreadCount()));
		facts.put("threads.current.count", Long.toString(mx.getThreadCount()));
	}

	private void setGCMXBeanFacts(Map<String, String> facts) {
		log.trace("setGCMXBeanFacts()");
		List<GarbageCollectorMXBean> beans = ManagementFactory
				.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean bean : beans) {
			String name = bean.getName();
			facts.put(String.format("gc.%1$s.collectionCount", name), Long
					.toString(bean.getCollectionCount()));
			facts.put(String.format("gc.%1$s.collectionTime", name), Long
					.toString(bean.getCollectionTime()));
		}
	}

	private void setPluginFacts(Map<String, String> facts) {
		log.trace("setPluginFacts()");
		PluginManager mgr = (PluginManager) ctx.getBean(PluginManager.NAME);
		Collection<Plugin> plugins = mgr.list();
		for (Plugin plugin : plugins) {
			facts.put(String.format("plugin.%1$s", plugin.getIdentifier()),
					Boolean.toString(true));
		}
	}

	private int getActiveThreadCount() {
		log.trace("getActiveThreadCount()");
		ThreadGroup next = Thread.currentThread().getThreadGroup();
		ThreadGroup top = next;
		while (next != null) {
			top = next;
			next = next.getParent();
		}
		return top.activeCount();
	}
}
