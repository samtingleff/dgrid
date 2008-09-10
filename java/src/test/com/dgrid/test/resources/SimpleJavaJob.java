package com.dgrid.test.resources;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.service.DGridClient;

public class SimpleJavaJob implements SimpleJoblet {

	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient) {
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, joblet
				.getContent());
	}
}
