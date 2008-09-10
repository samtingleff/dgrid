package com.dgrid.util;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Execute {
	private static Log log = LogFactory.getLog(Execute.class);

	private Execute() {
	}

	/**
	 * Execute a process and log stdout/stderr
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 * @throws ConversionFailedException
	 * @throws InterruptedException
	 */
	public static int execute(String cmd) throws IOException,
			InterruptedException {
		log.trace("execute()");
		log.info(String.format("Executing (%1$s)", cmd));

		Process p = Runtime.getRuntime().exec(cmd);

		return readProcessOutput(p);
	}

	/**
	 * Execute a process and log stdout/stderr
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 * @throws ConversionFailedException
	 * @throws InterruptedException
	 */
	public static int execute(String[] cmd) throws IOException,
			InterruptedException {
		log.trace("execute()");
		log.info(String.format("Executing (%1$s)", cmd[0]));

		Process p = Runtime.getRuntime().exec(cmd);

		return readProcessOutput(p);
	}

	public static int execute(String cmd, PrintStream stdout, PrintStream stderr)
			throws InterruptedException, IOException {
		log.trace("execute()");
		Process p = Runtime.getRuntime().exec(cmd);
		return readProcessOutput(p, stdout, stderr);

	}

	public static int execute(String cmd, StringBuffer stdout,
			StringBuffer stderr) throws InterruptedException, IOException {
		log.trace("execute()");
		Process p = Runtime.getRuntime().exec(cmd);
		return readProcessOutput(p, stdout, stderr);

	}

	public static int execute(String[] cmd, StringBuffer stdout,
			StringBuffer stderr) throws InterruptedException, IOException {
		log.trace("execute()");
		Process p = Runtime.getRuntime().exec(cmd);
		return readProcessOutput(p, stdout, stderr);

	}

	private static int readProcessOutput(Process p) throws InterruptedException {
		log.trace("readProcessOutput()");
		return readProcessOutput(p, System.out, System.err);
	}

	private static int readProcessOutput(Process p, PrintStream stdout,
			PrintStream stderr) throws InterruptedException {
		log.trace("readProcessOutput()");
		// create thread for reading inputStream (process' stdout)
		PrintStreamReaderThread outThread = new PrintStreamReaderThread(p
				.getInputStream(), stdout);
		// create thread for reading errorStream (process' stderr)
		PrintStreamReaderThread errThread = new PrintStreamReaderThread(p
				.getErrorStream(), stderr);
		return readProcessOutput(p, outThread, errThread);
	}

	private static int readProcessOutput(Process p, StringBuffer stdout,
			StringBuffer stderr) throws InterruptedException {
		log.trace("readProcessOutput()");
		// create thread for reading inputStream (process' stdout)
		StreamReaderThread outThread = new StreamReaderThread(p
				.getInputStream(), stdout);
		// create thread for reading errorStream (process' stderr)
		StreamReaderThread errThread = new StreamReaderThread(p
				.getErrorStream(), stderr);
		return readProcessOutput(p, outThread, errThread);
	}

	private static int readProcessOutput(Process p, Thread outThread,
			Thread errThread) throws InterruptedException {
		log.trace("readProcessOutput()");
		// start both threads
		outThread.start();
		errThread.start();

		int retval = p.waitFor();
		// finish reading whatever's left in the buffers
		outThread.join();
		errThread.join();

		return retval;
	}
}
