package com.dgrid.threads;

import java.util.concurrent.ThreadFactory;

public class DGridSimpleThreadFactory implements ThreadFactory {

	public Thread newThread(Runnable r) {
		return new Thread(r);
	}

}
