package com.dgrid.plugin;

import java.util.Collection;

public interface PluginManager {
	public static final String NAME = "pluginManager";

	public void init();

	public void start();

	public void stop();

	public Collection<Plugin> list();

	public Plugin loadPlugin(Plugin plugin);

	public void unloadPlugin(String identifier);

	public Plugin getPlugin(String identifier);
}
