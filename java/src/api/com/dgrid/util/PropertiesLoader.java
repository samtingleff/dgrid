package com.dgrid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

	public static Properties getProperties(String name) {
		Properties props = new Properties();
		String[] searchPaths = new String[] {
				String.format("/etc/%1$s", name),
				String.format("%1$s/.%2$s", System.getProperty("user.home"),
						name), String.format("/%1$s", name), name };
		for (String path : searchPaths) {
			InputStream is = null;
			try {
				File f = new File(path);
				if (f.isFile()) {
					is = new FileInputStream(f);
				} else {
					is = PropertiesLoader.class.getResourceAsStream(path);
				}
				if (is != null) {
					loadProperties(props, is);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return props;
	}

	private static void loadProperties(Properties props, InputStream is)
			throws Exception {
		props.load(is);
		is.close();
	}
}
