package com.dgrid.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.dgrid.util.JarFileFilter;
import com.dgrid.util.io.FileListener;

public class JavaPluginsPlugin extends AbstractDynamicPlugin implements Plugin,
		FileListener {
	public JavaPluginsPlugin() {
		super(new File("plugins/java"), 3000);
	}

	public String getDescription() {
		return "Loads plugins from external jars";
	}

	@Override
	protected Plugin loadPluginFromFile(File file) throws Exception {
		log.trace("loadPluginFromFile()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("Loading plugin from file (%1$s)", file
					.getAbsolutePath()));
		}
		String className = extractPluginClassName(file);
		Class pluginClass = createPluginClass(className, file);
		Object obj = pluginClass.newInstance();
		Plugin plugin = (Plugin) obj;
		return plugin;
	}

	@Override
	protected FileFilter getFileFilter() {
		return new JarFileFilter();
	}

	private String extractPluginClassName(File jar) throws IOException {
		JarFile jarFile = new JarFile(jar);
		Manifest manifest = jarFile.getManifest();
		Attributes attribs = manifest.getMainAttributes();
		return attribs.getValue("Plugin-Class");
	}

	private Class createPluginClass(String className, File jar)
			throws ClassNotFoundException, MalformedURLException {
		ClassLoader pluginClassLoader = getClassLoader(jar);
		Class pluginClass = pluginClassLoader.loadClass(className);
		if (log.isDebugEnabled()) {
			log.debug(String.format("Loaded plugin class %1$s", pluginClass));
			log.debug(String.format("From class loader %1$s", pluginClass
					.getClassLoader()));
		}
		return pluginClass;
	}

	private ClassLoader getClassLoader(File jar) throws MalformedURLException {
		File extLibDir = new File("lib/ext");
		File[] extJars = extLibDir.listFiles(new JarFileFilter());
		URL[] urls = new URL[extJars.length + 1];
		urls[0] = jar.toURI().toURL();
		for (int i = 1; i <= extJars.length; ++i) {
			urls[i] = extJars[i - 1].toURI().toURL();
		}
		URLClassLoader cl = new URLClassLoader(urls, Thread.currentThread()
				.getContextClassLoader());
		return cl;
	}
}
