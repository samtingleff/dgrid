package com.dgrid.api;

import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public interface JobletTypeHandler {
	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Throwable;
}
