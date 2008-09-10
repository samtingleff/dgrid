package com.dgrid.service;

import com.dgrid.api.JobletTypeHandler;
import com.dgrid.plugin.PluginManager;

public interface DGridPluginManager extends PluginManager {
	public String getProperty(String key);

	public JobletTypeHandler getJobletTypeHandler(String type);

	public void setJobletTypeHandler(String type, JobletTypeHandler handler);

	public void removeJobletTypeHandler(String type);
}
