package com.dgrid.plugin;

public interface PluginContext {
	public static final String NAME = "pluginContext";

	public Object getBean(String name);
}
