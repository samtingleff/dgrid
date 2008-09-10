package com.dgrid.plugin;

public interface Plugin {

	public void setPluginContext(PluginContext context);

	public void setPluginManager(PluginManager manager);

	public String getIdentifier();

	public String getName();

	public String getDescription();

	public boolean enabledByDefault();

	public void init();

	public void start();

	public void stop();
}
