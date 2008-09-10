package com.dgrid.plugins;

import java.io.File;

import com.dgrid.gen.Constants;
import com.dgrid.handlers.GroovyJobletTypeHandler;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.plugin.Plugin;
import com.dgrid.service.DGridPluginManager;

public class GroovyScriptPlugin extends BaseDGridPlugin implements Plugin {

	private File dir;

	public GroovyScriptPlugin() {
		super();
		this.dir = new File("plugins/groovy/joblets");
	}

	public String getDescription() {
		return "Provides groovy scripting support";
	}

	public void start() {
		log.trace("start()");
		((DGridPluginManager) super.pluginManager).setJobletTypeHandler(
				Constants.GROOVY_JOBLET, new GroovyJobletTypeHandler(dir));
	}

	public void stop() {
		log.trace("stop()");
		((DGridPluginManager) super.pluginManager)
				.removeJobletTypeHandler(Constants.GROOVY_JOBLET);
	}
}
