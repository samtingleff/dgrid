package com.dgrid.handlers;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class AgentRestartTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	private static final int EXIT_CODE = 100;

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		log.trace("execute()");
		// need to self-complete this job
		gridClient.completeJoblet(joblet.getId(), new JobletResult(0, 0l, 0, 1,
				JOB_STATUS.COMPLETED, "", joblet), "Restarting agent");
		System.exit(EXIT_CODE);
		return null;
	}

}
