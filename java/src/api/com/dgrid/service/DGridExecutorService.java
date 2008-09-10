package com.dgrid.service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.dgrid.threads.DGridTaskListener;

public interface DGridExecutorService extends Executor, ExecutorService,
		ScheduledExecutorService {

	public static final String NAME = "threadPool";

	public void addListener(DGridTaskListener listener);

	public void removeListener(DGridTaskListener listener);

	public int getActiveCount();
}
