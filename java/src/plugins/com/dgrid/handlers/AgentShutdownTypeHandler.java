package com.dgrid.handlers;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class AgentShutdownTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	public static final String EXIT_VALUE_PARAM = "exitValue";

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		log.trace("execute()");
		int exitCode = 0;
		try {
			exitCode = (joblet.getParameters().get(EXIT_VALUE_PARAM) != null) ? Integer
					.parseInt(joblet.getParameters().get(EXIT_VALUE_PARAM))
					: 0;
		} catch (NumberFormatException e) {
		}
		// need to self-complete this job
		gridClient.completeJoblet(joblet.getId(), new JobletResult(0, 0l, 0, 1,
				JOB_STATUS.COMPLETED, "", joblet), "Shutting down agent");
		System.exit(exitCode);
		return null;
	}

}
