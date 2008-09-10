package com.dgrid.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;

public class OutputStreamUtils {

	public static void writeStringToFile(String content, File dest)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(dest);
		PrintStream ps = new PrintStream(fos);
		ps.print(content);
		ps.close();
		fos.close();
	}

	public static void writeStreamToFile(InputStream in, File dest)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(dest);
		int read = 0;
		byte[] buffer = new byte[1024];
		while ((read = in.read(buffer)) > 0) {
			fos.write(buffer, 0, read);
		}
		fos.close();
	}

	public static void copyFile(File source, File dest) throws IOException {
		FileChannel ic = new FileInputStream(source).getChannel();
		FileChannel oc = new FileOutputStream(dest).getChannel();
		ic.transferTo(0, ic.size(), oc);
		ic.close();
		oc.close();
	}
}
