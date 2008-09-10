package com.dgrid.test;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dgrid.gen.Host;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.joblets.HostFactRunnable;
import com.dgrid.plugin.PluginManager;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridExecutorService;
import com.dgrid.service.DGridPluginManager;
import com.dgrid.util.io.HostnameDiscovery;

public abstract class BaseTestCase extends TestCase {
	protected AbstractApplicationContext ctx;

	protected DGridClient gridClient;

	protected DGridPluginManager pluginManager;

	protected Host host;

	public BaseTestCase() {
		super();
	}

	public void setUp() throws Exception {
		String[] paths = new String[] { "/applicationContext.xml",
				"/applicationContext-hibernate.xml" };
		ctx = new ClassPathXmlApplicationContext(paths);
		gridClient = (DGridClient) ctx.getBean(DGridClient.NAME);
		pluginManager = (DGridPluginManager) ctx.getBean(PluginManager.NAME);
		host = gridClient.registerHost(HostnameDiscovery.getHostname());
		pluginManager.init();
		pluginManager.start();

		DGridExecutorService executor = (DGridExecutorService) ctx
				.getBean(DGridExecutorService.NAME);
		executor.scheduleAtFixedRate(new HostFactRunnable(ctx), 0l, 5l,
				TimeUnit.MINUTES);
	}

	public void tearDown() throws Exception {
		ctx.registerShutdownHook();
	}

	protected JobletResult doWork() throws Exception {
		Joblet joblet = gridClient.getWork();
		JobletResult result = gridClient.executeJoblet(joblet);
		return result;
	}

	protected Object getBean(String name) {
		return ctx.getBean(name);
	}

	protected String getHostname() {
		return HostnameDiscovery.getHostname();
	}

}