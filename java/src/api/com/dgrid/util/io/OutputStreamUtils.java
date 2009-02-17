package com.dgrid.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;

public class OutputStreamUtils {
	public static final int DEFAULT_BUFFER_SIZE = 1024;

	public static void writeStringToFile(String content, File dest)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(dest);
		PrintStream ps = new PrintStream(fos);
		ps.print(content);
		ps.close();
		fos.close();
	}

	public static void writeStreamToStream(InputStream in, OutputStream out)
			throws IOException {
		writeStreamToStream(in, out, DEFAULT_BUFFER_SIZE);
	}

	public static void writeStreamToStream(InputStream in, OutputStream out,
			int bufferSize) throws IOException {
		int read = 0;
		byte[] buffer = new byte[bufferSize];
		while ((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		out.flush();
	}

	public static void writeStreamToFile(InputStream in, File dest)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(dest);
		writeStreamToStream(in, fos);
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
