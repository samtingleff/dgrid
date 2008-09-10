package com.dgrid.service.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.api.JobletTypeHandler;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.handlers.UnknownJobletTypeHandler;
import com.dgrid.helpers.PropertyConfigurator;
import com.dgrid.plugin.JavaPluginsPlugin;
import com.dgrid.plugin.Plugin;
import com.dgrid.plugin.PluginContext;
import com.dgrid.plugin.PluginManager;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridPluginContext;
import com.dgrid.service.DGridPluginManager;

public class DGridPluginManagerImpl implements PluginManager,
		DGridPluginManager {
	private Log log = LogFactory.getLog(getClass());

	private Hashtable<String, Plugin> plugins = new Hashtable<String, Plugin>();

	private HashMap<String, JobletTypeHandler> typeHandlers = new HashMap<String, JobletTypeHandler>();

	private DGridPluginContext context;

	private PropertyConfigurator props;

	public void setPluginContext(PluginContext context) {
		this.context = (DGridPluginContext) context;
	}

	public void setPropertyConfigurator(PropertyConfigurator props) {
		this.props = props;
	}

	public void init() {
		log.trace("init()");
		safeLoadPlugin(JavaPluginsPlugin.class);
	}

	public void start() {
		log.trace("start()");
		startPlugins();
	}

	public void stop() {
		log.trace("stop()");
		for (Plugin plugin : plugins.values()) {
			try {
				plugin.stop();
			} catch (Exception e) {
				log.warn(String.format(
						"Exception calling stop() on plugin (%1$s)", plugin));
			}
		}
	}

	public Plugin loadPlugin(Plugin plugin) {
		log.trace("loadPlugin()");
		initPlugin(plugin);
		return plugin;
	}

	public void unloadPlugin(String identifier) {
		log.trace("unloadPlugin()");
		Plugin p = plugins.get(identifier);
		if (p != null) {
			p.stop();
			plugins.remove(identifier);
		}
	}

	public Collection<Plugin> list() {
		log.trace("list()");
		return plugins.values();
	}

	public Plugin getPlugin(String identifier) {
		return plugins.get(identifier);
	}

	public String getProperty(String key) {
		log.trace("getProperty()");
		try {
			return props.getProperty(key);
		} catch (IOException e) {
			log.error("IOException in getProperty()", e);
			return null;
		}
	}

	public void setJobletTypeHandler(String type, JobletTypeHandler handler) {
		log.trace("setJobletTypeHandler()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("Adding type (%1$s) with class (%2$s)",
					type, handler));
		}
		typeHandlers.put(type, handler);
	}

	public JobletTypeHandler getJobletTypeHandler(String type) {
		log.trace("getJobletTypeHandler()");
		JobletTypeHandler h = typeHandlers.get(type);
		if (h == null) {
			return new UnknownJobletTypeHandler();
		} else {
			return h;
		}
	}

	public void removeJobletTypeHandler(String type) {
		log.trace("removeJobletTypeHandler()");
		if (typeHandlers.containsKey(type)) {
			typeHandlers.remove(type);
		}
	}

	private void startPlugin(Plugin plugin) {
		log.trace("startPlugin()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("Loading plugin class (%1$s)", plugin
					.getClass().getName()));
			log.debug(String.format("Identifier: (%1$s)", plugin
					.getIdentifier()));
			log.debug(String.format("Name: (%1$s)", plugin.getName()));
			log.debug(String.format("Description: (%1$s)", plugin
					.getDescription()));
		}
		plugin.start();
	}

	private void safeLoadPlugin(Class cls) {
		log.trace("safeLoadPlugin()");
		try {
			Object obj = cls.newInstance();
			Plugin plugin = (Plugin) obj;
			initPlugin(plugin);
		} catch (Exception e) {
			log.warn(String.format("Could not plugin from class %1$s", cls), e);
		} finally {
		}
	}

	private void startPlugins() {
		log.trace("startPlugins()");
		// because plugins can add other plugins (and thus add to our plugins
		// hashtable)
		// we need to snapshot the list and iterate over the snapshot,
		// avoiding java.util.ConcurrentModificationException errors.
		Hashtable<String, Plugin> clone = (Hashtable<String, Plugin>) plugins
				.clone();
		for (Iterator<Plugin> iter = clone.values().iterator(); iter.hasNext();) {
			Plugin p = iter.next();
			try {
				startPlugin(p);
			} catch (Exception e) {
				log
						.warn(String
								.format(
										"Could not start plugin with identifier (%1$s). It will be disabled.",
										p.getIdentifier()));
				log.warn("Could not start plugin", e);
				plugins.remove(p.getIdentifier());
			}
		}
	}

	private void initPlugin(Plugin plugin) {
		log.trace("initPlugin()");
		if (plugins.containsKey(plugin.getIdentifier())) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Removing old plugin %1$s", plugin
						.getIdentifier()));
			}
			Plugin old = plugins.get(plugin.getIdentifier());
			old.stop();
			plugins.remove(old.getIdentifier());
		}
		try {
			if (enabled(plugin)) {
				plugin.setPluginContext(context);
				plugin.setPluginManager(this);
				plugin.init();
				plugins.put(plugin.getIdentifier(), plugin);
			}
		} catch (Exception e) {
			log.trace("Exception in initPlugin()", e);
		}
	}

	private boolean enabled(Plugin plugin) throws TransportException,
			InvalidApiKey, InvalidHost {
		log.trace("enabled()");
		String setting = String.format("plugin.%1$s.enable", plugin
				.getIdentifier());
		DGridClient gridClient = context.getGridClient();
		boolean systemEnabled = Boolean.parseBoolean(gridClient.getSetting(
				setting, Boolean.toString(plugin.enabledByDefault())));
		if ((systemEnabled)
				&& (Boolean.parseBoolean(gridClient.getHostSetting(gridClient
						.getHost().getId(), setting, Boolean
						.toString(systemEnabled))))) {
			return true;
		} else
			return false;
	}
}
