package com.dgrid.util;

import java.io.File;
import java.io.FileFilter;

public class JarFileFilter implements FileFilter {

	public boolean accept(File pathname) {
		return pathname.toString().toLowerCase().endsWith(".jar");
	}

}
