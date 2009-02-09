package com.dgrid.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

public class PidFileWriter {
	/**
	 * Return the process id of this process. Seems to work on Linux and OS X...
	 * if you're on some weird OS like BeOS or Windows this may or may not work.
	 * 
	 * @return pid The numeric process id of the running java process.
	 */
	public int getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		if ((name != null) && (name.matches("\\d+@.+"))) {
			String pidString = name.substring(0, name.indexOf('@'));
			int pid = Integer.parseInt(pidString);
			return pid;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Write the current process id to the file name specified by the system
	 * property pid.file (if present).
	 * 
	 * @throws IOException
	 */
	public void writePidFile() throws IOException {
		int pid = getPid();
		String filename = System.getProperty("pid.file");
		if ((filename != null) && (filename.length() > 0)) {
			File file = new File(filename);
			OutputStream out = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(out);
			pw.print(pid);
			pw.close();
			out.close();
		}
	}
}
