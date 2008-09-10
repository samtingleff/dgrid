package com.dgrid.plugins;

import java.io.File;

import com.dgrid.handlers.JavascriptJobletTypeHandler;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.plugin.Plugin;
import com.dgrid.service.DGridPluginManager;

public class JavascriptPlugin extends BaseDGridPlugin implements Plugin {

	public String getDescription() {
		return "Provides javascript support for joblets";
	}

	public void start() {
		log.trace("start()");
		((DGridPluginManager) super.pluginManager).setJobletTypeHandler(
				"javascript", new JavascriptJobletTypeHandler(new File(
						"plugins/javascript/joblets")));
	}

	public void stop() {
		log.trace("stop()");
		((DGridPluginManager) super.pluginManager)
				.removeJobletTypeHandler("javascript");
	}

}
