package com.dgrid.plugin;

import java.io.File;
import java.io.FileFilter;
import java.util.Hashtable;

import com.dgrid.util.io.FileListener;
import com.dgrid.util.io.FileMonitor;

public abstract class AbstractDynamicPlugin extends BaseDGridPlugin implements
		Plugin, FileListener {
	protected File dir;

	private FileMonitor monitor;

	private long lastChange = 0;

	private Hashtable<File, Plugin> plugins = new Hashtable<File, Plugin>();

	public AbstractDynamicPlugin(File dir, long watchInterval) {
		this.dir = dir;
		this.monitor = new FileMonitor(watchInterval);
	}

	@Override
	public void init() {
		log.trace("init()");
		monitor.addFile(dir);
		monitor.addListener(this);
		lastChange = System.currentTimeMillis();
		initPlugins();
	}

	public void start() {
		log.trace("start()");
	}

	public void stop() {
		log.trace("stop()");
		for (Plugin plugin : plugins.values()) {
			try {
				pluginManager.unloadPlugin(plugin.getIdentifier());
			} catch (Exception e) {
				log
						.warn(String.format(
								"Exception calling stop() on plugin (%1$s)",
								plugin), e);
			}
		}
		plugins.clear();
	}

	public void fileChanged(File file) {
		log.trace("fileChanged()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("File: %1$s has changed", file));
		}
		if (!file.isDirectory()) {
			if (!file.exists()) {
				// deleted
				if (log.isDebugEnabled()) {
					log.debug(String.format("Removing plugin from file %1$s",
							file));
				}
				if (plugins.get(file) != null) {
					Plugin p = plugins.get(file);
					pluginManager.unloadPlugin(p.getIdentifier());
					plugins.remove(file);
				}
			} else if (getFileFilter().accept(file)) {
				try {
					Plugin p = loadPluginFromFile(file);
					if (plugins.containsKey(file)) {
						pluginManager.unloadPlugin(plugins.get(file)
								.getIdentifier());
						plugins.remove(file);
					}
					Plugin p2 = pluginManager.loadPlugin(p);
					plugins.put(file, p2);
					monitor.addFile(file);
				} catch (Exception e) {
					log.warn(String.format(
							"Could not load plugin from file %1$s", file), e);
					try {
						monitor.removeFile(file);
					} catch (Exception e2) {
					}
				} finally {
				}
			}
		}
		lastChange = System.currentTimeMillis();
	}

	private void initPlugins() {
		log.trace("initPlugins()");
		File[] files = dir.listFiles(getFileFilter());
		for (File file : files) {
			try {
				Plugin plugin = loadPluginFromFile(file);
				pluginManager.loadPlugin(plugin);
				plugins.put(file, plugin);
				monitor.addFile(file);
			} catch (Exception e) {
				log.warn(String.format(
						"Could not init plugin from file (%1$s)", file), e);
			} finally {
			}
		}
	}

	protected abstract Plugin loadPluginFromFile(File file) throws Exception;

	protected abstract FileFilter getFileFilter();
}
