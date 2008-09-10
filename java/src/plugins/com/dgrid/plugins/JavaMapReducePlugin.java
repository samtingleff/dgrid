package com.dgrid.plugins;

import com.dgrid.gen.Constants;
import com.dgrid.handlers.JavaMapReduceTypeHandler;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.service.DGridPluginManager;

public class JavaMapReducePlugin extends BaseDGridPlugin {

	public String getDescription() {
		return "Adds support for simple java-based map/reduce jobs";
	}

	public void start() {
		DGridPluginManager mgr = (DGridPluginManager) pluginManager;
		JavaMapReduceTypeHandler handler = new JavaMapReduceTypeHandler();
		mgr.setJobletTypeHandler(Constants.JAVA_MR_JOB, handler);
		mgr.setJobletTypeHandler(Constants.JAVA_MR_REDUCER, handler);
	}

	public void stop() {
		DGridPluginManager mgr = (DGridPluginManager) pluginManager;
		mgr.removeJobletTypeHandler(Constants.JAVA_MR_JOB);
		mgr.removeJobletTypeHandler(Constants.JAVA_MR_REDUCER);
	}

}
