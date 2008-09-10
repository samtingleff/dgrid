package com.dgrid.driver;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.dgrid.joblets.HostFactRunnable;
import com.dgrid.service.DGridExecutorService;
import com.dgrid.service.DGridProcessor;

public class Worker extends BaseDgridDriver {
	private static Log log = LogFactory.getLog(Worker.class);

	public static void main(String[] args) throws Exception {
		log.trace("main()");
		AbstractApplicationContext ctx = getContext();
		DGridProcessor driver = (DGridProcessor) ctx
				.getBean(DGridProcessor.NAME);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(driver));
		driver.init();
		provisionFactRunnable(ctx);
		Thread t = new Thread(driver);
		t.start();
	}

	/*
	 * Set host facts every 10 minutes
	 */
	private static void provisionFactRunnable(ApplicationContext ctx)
			throws Exception {
		log.trace("provisionFactRunnable()");
		DGridExecutorService executor = (DGridExecutorService) ctx
				.getBean(DGridExecutorService.NAME);
		executor.scheduleAtFixedRate(new HostFactRunnable(ctx), 0l, 5l,
				TimeUnit.MINUTES);
	}

	private static class ShutdownHook extends Thread {
		DGridProcessor driver;

		ShutdownHook(DGridProcessor driver) {
			this.driver = driver;
		}

		public void run() {
			log.trace("run()");
			// execute in new thread
			Thread t = new Thread() {
				public void run() {
					driver.stop();
				}
			};
			t.start();
		}
	}
}
