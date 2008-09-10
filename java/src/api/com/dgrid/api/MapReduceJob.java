package com.dgrid.api;

import java.util.List;

import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public interface MapReduceJob {

	public List<Joblet> getMappers(Joblet joblet, DGridClient gridClient);

	public SimpleJobletResult reduce(List<JobletResult> results);
}
