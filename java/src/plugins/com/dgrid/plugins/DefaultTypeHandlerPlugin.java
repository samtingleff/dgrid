package com.dgrid.plugins;

import com.dgrid.gen.Constants;
import com.dgrid.handlers.AgentRestartTypeHandler;
import com.dgrid.handlers.AgentShutdownTypeHandler;
import com.dgrid.handlers.CallbackJobletTypeHandler;
import com.dgrid.handlers.JavaAppJobletTypeHandler;
import com.dgrid.handlers.JavaJobletTypeHandler;
import com.dgrid.handlers.SystemJobletTypeHandler;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.plugin.Plugin;
import com.dgrid.service.DGridPluginManager;

public class DefaultTypeHandlerPlugin extends BaseDGridPlugin implements Plugin {

	public String getDescription() {
		return "Sets up default joblet type handlers";
	}

	public void start() {
		log.trace("start()");
		DGridPluginManager mgr = 
			(DGridPluginManager) pluginManager;
		mgr.setJobletTypeHandler(Constants.JAVA_JOBLET,
				new JavaJobletTypeHandler());
		mgr.setJobletTypeHandler(Constants.JAVA_APP_JOBLET,
				new JavaAppJobletTypeHandler());
		mgr.setJobletTypeHandler(Constants.SYSTEM_JOBLET,
				new SystemJobletTypeHandler());
		mgr.setJobletTypeHandler(Constants.AGENT_SHUTDOWN_JOBLET,
				new AgentShutdownTypeHandler());
		mgr.setJobletTypeHandler(Constants.AGENT_RESTART_JOBLET,
				new AgentRestartTypeHandler());
		mgr.setJobletTypeHandler("callback", new CallbackJobletTypeHandler());
	}

	public void stop() {
		log.trace("stop()");
		DGridPluginManager mgr = 
			(DGridPluginManager) pluginManager;
		mgr.removeJobletTypeHandler(Constants.AGENT_RESTART_JOBLET);
		mgr.removeJobletTypeHandler(Constants.AGENT_SHUTDOWN_JOBLET);
		mgr.removeJobletTypeHandler(Constants.SYSTEM_JOBLET);
		mgr.removeJobletTypeHandler(Constants.JAVA_APP_JOBLET);
		mgr.removeJobletTypeHandler(Constants.JAVA_JOBLET);
	}

}
