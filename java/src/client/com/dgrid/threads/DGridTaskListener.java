package com.dgrid.threads;

public interface DGridTaskListener {
	public void beforeExecute(Thread t, Runnable r, int activeThreadCount,
			int maxThreadCount);

	public void afterExecute(Runnable r, Throwable t, int activeThreadCount,
			int maxThreadCount);
}
