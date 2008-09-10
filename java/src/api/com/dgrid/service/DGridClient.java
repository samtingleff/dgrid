package com.dgrid.service;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;

public interface DGridClient extends DGridTransport {
	public static final String NAME = "gridClient";

	public Object getBean(String name);

	public int submitJoblet(Joblet joblet, int jobId)
			throws TransportException, InvalidApiKey, InvalidJobId;

	public int submitHostJoblet(String hostname, Joblet joblet, int jobId)
			throws TransportException, InvalidApiKey, InvalidJobId, InvalidHost;

	public int submitHostJoblet(String hostname, Joblet joblet, int jobId,
			int callbackType, String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId, InvalidHost;

	public JobletResult executeJoblet(Joblet joblet);

	public JobletResult execute(Joblet joblet);
}
