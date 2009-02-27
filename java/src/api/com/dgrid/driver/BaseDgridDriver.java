package com.dgrid.driver;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public abstract class BaseDgridDriver {
	private static String[] defaultSpringPaths = new String[] {
			"/applicationContext.xml", "/applicationContext-hibernate.xml" };

	private static Log log = LogFactory.getLog(BaseDgridDriver.class);

	protected static AbstractApplicationContext getContext() {
		AbstractApplicationContext ctx = null;
		String springConfig = System.getProperty("spring.config");
		if ((springConfig != null) && (springConfig.length() > 0)) {
			File file = new File(springConfig);
			if (file.exists() && file.canRead()) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(
							"Loading spring context from file %1$s",
							springConfig));
				}
				ctx = new FileSystemXmlApplicationContext(springConfig);
			} else {
				log
						.warn(String
								.format(
										"Attempt to load spring context from non-existent or non-readable file %1$s. Using defaults.",
										springConfig));
			}
		}
		if (ctx == null) {
			ctx = new ClassPathXmlApplicationContext(defaultSpringPaths);
		}
		return ctx;
	}
}
