package com.dgrid.service;

import com.dgrid.gen.HostState;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.facebook.thrift.TException;

public interface DGridSyncJobService {
	public static final String NAME = "syncJobService";

	public JobletResult gridExecute(String hostname, Joblet joblet)
			throws InvalidApiKey, TException;

	public HostState status(String hostname) throws InvalidApiKey, TException;

}