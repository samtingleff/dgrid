package com.dgrid.driver;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class BaseDgridDriver {

	protected static AbstractApplicationContext getContext() throws Exception {
		String[] paths = new String[] { "/applicationContext.xml",
				"/applicationContext-hibernate.xml" };
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
				paths);
		return ctx;
	}
}
