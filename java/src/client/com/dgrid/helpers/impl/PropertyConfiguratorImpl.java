package com.dgrid.helpers.impl;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.dgrid.helpers.PropertyConfigurator;

public class PropertyConfiguratorImpl extends PropertyPlaceholderConfigurer
		implements PropertyConfigurator {

	private Properties props = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.helpers.impl.PropertyConfigurator#getProperty(java.lang.String)
	 */
	public String getProperty(String key) throws IOException {
		if (props == null)
			props = super.mergeProperties();
		return props.getProperty(key);
	}
}
