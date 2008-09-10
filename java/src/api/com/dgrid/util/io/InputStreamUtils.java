package com.dgrid.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {

	public static String getFileAsString(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		String s = getInputStreamAsString(fis);
		fis.close();
		return s;
	}

	public static String getResourceAsString(Class cls, String resource)
			throws IOException {
		InputStream is = cls.getResourceAsStream(resource);
		String retval = getInputStreamAsString(is);
		is.close();
		return retval;
	}

	public static String getInputStreamAsString(InputStream in)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read = 0;
		StringBuffer sb = new StringBuffer();
		while ((read = in.read(buffer)) > 0) {
			sb.append(new String(buffer, 0, read));
		}
		String retval = sb.toString();
		return retval;
	}
}
