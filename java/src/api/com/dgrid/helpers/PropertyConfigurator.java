package com.dgrid.helpers;

import java.io.IOException;

public interface PropertyConfigurator {
	public static final String NAME = "propertyConfigurator";

	public abstract String getProperty(String key) throws IOException;

}