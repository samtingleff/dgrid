package com.dgrid.handlers;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class JavaJobletTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	public static final String CLASS_NAME_PARAM = "class";

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		log.trace("execute()");
		long start = System.currentTimeMillis();
		Class cls = Class.forName(joblet.getParameters().get(CLASS_NAME_PARAM));
		Object impl = cls.newInstance();
		SimpleJoblet sj = (SimpleJoblet) impl;
		SimpleJobletResult sjr = sj.execute(joblet, gridClient);
		JobletResult result = new JobletResult(0, 0l, sjr.getReturnCode(),
				(System.currentTimeMillis() - start), sjr.getStatus(), sjr
						.getDetails(), joblet);
		return result;
	}

}
