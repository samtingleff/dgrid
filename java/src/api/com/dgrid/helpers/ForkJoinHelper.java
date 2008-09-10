package com.dgrid.helpers;

import java.util.List;
import java.util.concurrent.Callable;

import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;

public interface ForkJoinHelper {
	public static final String NAME = "forkJoinHelper";

	@SuppressWarnings("unchecked")
	public List executeConcurrently(List<Callable> tasks, int maxThreadCount);

	public List<JobletResult> gridExecuteConcurrently(List<Joblet> joblets,
			int maxThreadCount, int retries);
}
