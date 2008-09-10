package com.dgrid.service.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.dgrid.plugin.PluginContext;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridExecutorService;
import com.dgrid.service.DGridPluginContext;
import com.dgrid.threads.DGridTaskListener;

public class DGridPluginContextImpl implements PluginContext,
		DGridPluginContext, ApplicationContextAware {

	private ApplicationContext ctx;

	private DGridClient gridClient;

	private DGridExecutorService executor;

	public DGridPluginContextImpl() {
	}

	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	public void setGridClient(DGridClient gridClient) {
		this.gridClient = gridClient;
	}

	public void setThreadPool(DGridExecutorService service) {
		this.executor = service;
	}

	public Object getBean(String name) {
		return ctx.getBean(name);
	}

	public DGridClient getGridClient() {
		return gridClient;
	}

	public void addExecutionListener(DGridTaskListener listener) {
		executor.addListener(listener);
	}

	public void removeExecutionListener(DGridTaskListener listener) {
		executor.removeListener(listener);
	}
}
