package com.dgrid.handlers;

import java.util.LinkedList;
import java.util.List;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.api.MapReduceJob;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_CALLBACK_TYPES;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class JavaMapReduceTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	public static final String CLASS_NAME_PARAM = "class";

	public JavaMapReduceTypeHandler() {
	}

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Throwable {
		log.trace("execute()");
		Class cls = Class.forName(joblet.getParameters().get(CLASS_NAME_PARAM));
		Object impl = cls.newInstance();
		MapReduceJob mrj = (MapReduceJob) impl;
		JobletResult jr = execute(mrj, joblet, gridClient);
		return jr;
	}

	protected JobletResult execute(MapReduceJob mrj, Joblet joblet,
			DGridClient gridClient) throws Throwable {
		long start = System.currentTimeMillis();
		JobletResult result = null;
		if (joblet.getJobletType().equals(Constants.JAVA_MR_JOB)) {
			result = map(mrj, joblet, gridClient);
			Joblet reducer = new Joblet(0, 0l, joblet.getJobId(), 0, joblet
					.getSubmitter(), joblet.getPriority(),
					Constants.JAVA_MR_REDUCER, "", joblet.getParameters(),
					joblet.getContent(), JOB_STATUS.SAVED);
			gridClient.submitJoblet(reducer, joblet.getJobId());
		} else if (joblet.getJobletType().equals(Constants.JAVA_MR_REDUCER))
			result = reduce(Constants.JAVA_MR_JOB, mrj, joblet, gridClient);
		else
			throw new UnsupportedOperationException(String.format(
					"Unknown joblet type (%1$s)", joblet.getJobletType()));
		return result;
	}

	protected JobletResult map(MapReduceJob mrj, Joblet joblet,
			DGridClient gridClient) throws Throwable {
		long start = System.currentTimeMillis();
		submitMappers(mrj, joblet, gridClient);
		JobletResult result = new JobletResult(0, 0l, 0, (System
				.currentTimeMillis() - start), JOB_STATUS.COMPLETED, "", joblet);
		return result;

	}

	protected JobletResult reduce(String mapperType, MapReduceJob mrj,
			Joblet joblet, DGridClient gridClient) throws Throwable {
		long start = System.currentTimeMillis();
		SimpleJobletResult sjr = executeReducer(mapperType, mrj, joblet,
				gridClient);
		JobletResult result = new JobletResult(0, 0l, sjr.getReturnCode(),
				(System.currentTimeMillis() - start), sjr.getStatus(), sjr
						.getDetails(), joblet);
		return result;
	}

	protected SimpleJobletResult executeReducer(String mapType,
			MapReduceJob mrj, Joblet joblet, DGridClient gridClient)
			throws Throwable {
		// List<JobletResult> results = gridClient.getResults(joblet.jobId);
		List<JobletResult> results = new LinkedList<JobletResult>();
		Job job = gridClient.getJob(joblet.getJobId());
		for (Joblet j : job.getJoblets()) {
			if ((!j.getJobletType().equals(mapType))
					&& (j.getStatus() == JOB_STATUS.COMPLETED)) {
				JobletResult jr = gridClient.getJobletResult(j.getId());
				results.add(jr);
			}
		}
		SimpleJobletResult result = mrj.reduce(results);
		return result;
	}

	private void submitMappers(MapReduceJob mrj, Joblet joblet,
			DGridClient gridClient) throws Throwable {
		List<Joblet> joblets = mrj.getMappers(joblet, gridClient);
		for (Joblet j : joblets) {
			gridClient.submitJoblet(j, joblet.getJobId(),
					JOB_CALLBACK_TYPES.NONE, "", null);
		}
	}
}
