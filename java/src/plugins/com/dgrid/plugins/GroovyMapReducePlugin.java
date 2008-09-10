package com.dgrid.plugins;

import java.io.File;

import com.dgrid.gen.Constants;
import com.dgrid.handlers.GroovyMapReduceTypeHandler;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.service.DGridPluginManager;

public class GroovyMapReducePlugin extends BaseDGridPlugin {

	public String getDescription() {
		return "Adds support for simple groovy-based map/reduce jobs";
	}

	public void start() {
		DGridPluginManager mgr = (DGridPluginManager) pluginManager;
		GroovyMapReduceTypeHandler handler = new GroovyMapReduceTypeHandler(
				new File("plugins/groovy/mr"));
		mgr.setJobletTypeHandler(Constants.GROOVY_MR_JOB, handler);
		mgr.setJobletTypeHandler(Constants.GROOVY_MR_REDUCER, handler);
	}

	public void stop() {
		DGridPluginManager mgr = (DGridPluginManager) pluginManager;
		mgr.removeJobletTypeHandler(Constants.GROOVY_MR_JOB);
		mgr.removeJobletTypeHandler(Constants.GROOVY_MR_REDUCER);
	}

}
