package com.dgrid.plugins;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FileFilter;

import com.dgrid.plugin.AbstractDynamicPlugin;
import com.dgrid.plugin.Plugin;

public class GroovyPluginsPlugin extends AbstractDynamicPlugin implements
		Plugin {

	public GroovyPluginsPlugin() {
		super(new File("plugins/groovy"), 5000);
	}

	public String getDescription() {
		return "Loads all built-in groovy plugins";
	}

	public void start() {
		log.trace("start()");
		super.start();
	}

	public void stop() {
		log.trace("stop()");
		super.stop();
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {
			public boolean accept(File pathname) {
				if ((pathname.isFile())
						&& (pathname.getName().endsWith(".groovy")))
					return true;
				return false;
			}
		};
	}

	protected Plugin loadPluginFromFile(File file) throws Exception {
		GroovyClassLoader loader = new GroovyClassLoader(getClass()
				.getClassLoader());
		Class groovyClass = loader.parseClass(file);
		Object obj = groovyClass.newInstance();
		Plugin plugin = (Plugin) obj;
		return plugin;
	}

}
