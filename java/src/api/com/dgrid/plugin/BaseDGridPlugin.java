package com.dgrid.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseDGridPlugin implements Plugin {

	protected Log log = LogFactory.getLog(getClass());

	protected PluginContext context;

	protected PluginManager pluginManager;

	// added for python plugins
	public Log getLog() {
		return log;
	}

	public void setPluginContext(PluginContext context) {
		this.context = context;
	}

	// added for python plugins
	public PluginContext getPluginContext() {
		return context;
	}

	public void setPluginManager(PluginManager manager) {
		this.pluginManager = manager;
	}

	// added for python plugins
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public String getIdentifier() {
		return getClass().getSimpleName();
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	public boolean enabledByDefault() {
		return true;
	}

	public void init() {
		log.trace("init()");
	}
}
