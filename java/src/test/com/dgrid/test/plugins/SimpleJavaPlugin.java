package com.dgrid.test.plugins;

import com.dgrid.plugin.Plugin;
import com.dgrid.plugin.PluginContext;
import com.dgrid.plugin.PluginManager;

public class SimpleJavaPlugin implements Plugin {

	private PluginContext context;

	private PluginManager manager;

	public void setPluginContext(PluginContext context) {
		this.context = context;
	}

	public void setPluginManager(PluginManager manager) {
		this.manager = manager;
	}

	public boolean enabledByDefault() {
		return false;
	}

	public String getIdentifier() {
		return getClass().getName();
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	public String getDescription() {
		return "Simple java plugin from external jar";
	}

	public void init() {
		System.err.println("init()");
	}

	public void start() {
		System.err.println("start()");
	}

	public void stop() {
		System.err.println("stop()");
	}

}
