package com.dgrid.service;

import com.dgrid.plugin.PluginContext;
import com.dgrid.threads.DGridTaskListener;

public interface DGridPluginContext extends PluginContext {

	public DGridClient getGridClient();

	public void addExecutionListener(DGridTaskListener listener);

	public void removeExecutionListener(DGridTaskListener listener);
}
