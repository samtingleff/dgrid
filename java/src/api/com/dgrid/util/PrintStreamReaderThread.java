package com.dgrid.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class PrintStreamReaderThread extends Thread {
	private PrintStream out;

	private InputStreamReader isr;

	public PrintStreamReaderThread(InputStream in, PrintStream out) {
		this.out = out;
		this.isr = new InputStreamReader(in);
	}

	public void run() {
		int ch;
		try {
			while (-1 != (ch = isr.read()))
				out.append((char) ch);
		} catch (Exception e) {
			out.append("\nRead error: " + e.getMessage());
		}
	}
}
