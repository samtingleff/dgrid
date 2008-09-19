package com.dgrid.service;

import java.util.List;
import java.util.Map;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Host;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.InvalidJobletId;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.NoHostAvailable;
import com.dgrid.gen.NoWorkAvailable;

public interface DGridTransport {
	public static final String NAME = "transport";

	public void setApiKey(String apiKey);

	public void setEndpoint(String endpoint);

	public void setPort(int port);

	public Host registerHost(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost;

	public Host getHostByName(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost;

	public void setHostFacts(int hostid, Map<String, String> facts)
			throws TransportException, InvalidApiKey, InvalidHost;

	public String getHostSetting(int hostid, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost;

	public String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey;

	public void log(int jobletId, int jobletStatus, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId;

	public int submitJob(Job job) throws TransportException, InvalidApiKey;

	public int submitJoblet(Joblet joblet, int jobId, int callbackType,
			String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId;

	public JobletResult gridExecute(Joblet joblet, int retries)
			throws InvalidApiKey, TransportException, NoHostAvailable;

	public Joblet getWork() throws TransportException, InvalidApiKey,
			InvalidHost, NoWorkAvailable;

	public void completeJoblet(int jobletId, JobletResult result,
			String logMessage) throws TransportException, InvalidApiKey,
			InvalidJobletId;

	public void releaseJoblet(int jobletId) throws InvalidApiKey,
			TransportException, InvalidJobletId;

	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
			InvalidJobId;

	public JobletResult getJobletResult(int jobletId)
			throws TransportException, InvalidApiKey, InvalidJobletId;

	public List<JobletResult> getResults(int jobId) throws TransportException,
			InvalidApiKey, InvalidJobId;

	public int getJobletQueueSize() throws TransportException, InvalidApiKey;

}
