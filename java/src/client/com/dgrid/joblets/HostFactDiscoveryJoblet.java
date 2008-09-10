package com.dgrid.joblets;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.service.DGridClient;

public class HostFactDiscoveryJoblet implements SimpleJoblet {

	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		HostFactRunnable delegate = new HostFactRunnable();
		delegate.execute(gridClient);
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "Completed");
	}
}
