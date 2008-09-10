package com.dgrid.handlers;

import com.dgrid.api.JobletTypeHandler;
import com.dgrid.errors.UnknownJobletTypeException;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class UnknownJobletTypeHandler implements JobletTypeHandler {

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws UnknownJobletTypeException, Exception {
		throw new UnknownJobletTypeException(String.format(
				"Unknown joblet type (%1$s)", joblet.getJobletType()));
	}
}
