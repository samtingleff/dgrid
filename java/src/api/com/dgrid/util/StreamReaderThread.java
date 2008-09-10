package com.dgrid.util;

import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamReaderThread extends Thread {
	private StringBuffer out;

	private InputStreamReader isr;

	public StreamReaderThread(InputStream in, StringBuffer out) {
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
