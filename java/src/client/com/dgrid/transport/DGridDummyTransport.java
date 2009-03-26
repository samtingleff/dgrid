package com.dgrid.transport;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Host;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.InvalidJobletId;
import com.dgrid.gen.JOB_CALLBACK_TYPES;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.handlers.CallbackJobletTypeHandler;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridTransport;
import com.dgrid.util.io.HostnameDiscovery;

public class DGridDummyTransport implements DGridTransport {

	private Log log = LogFactory.getLog(getClass());

	private DGridClient gridClient;

	private List<Joblet> workQueue = new ArrayList<Joblet>();

	private Hashtable<Integer, Joblet> activeQueue = new Hashtable<Integer, Joblet>();

	private Hashtable<Integer, Job> jobs = new Hashtable<Integer, Job>();

	private Hashtable<Integer, JobletResult> results = new Hashtable<Integer, JobletResult>();

	private Hashtable<String, Host> hosts = new Hashtable<String, Host>();

	private Random random = new Random();

	public void setGridClient(DGridClient gridClient) {
		this.gridClient = gridClient;
	}

	public void setApiKey(String apiKey) {
	}

	public void setEndpoint(String endpoint) {
	}

	public void setPort(int port) {
	}

	public Host registerHost(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		Host h = hosts.get(hostname);
		if (h == null) {
			h = new Host(random.nextInt(), hostname,
					new HashMap<String, String>());
			hosts.put(hostname, h);
		}
		return h;
	}

	public Host getHost(int id) throws TransportException, InvalidApiKey,
			InvalidHost {
		for (Host h : hosts.values()) {
			if (h.getId() == id)
				return h;
		}
		throw new InvalidHost();
	}

	public Host getHostByName(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		Host h = hosts.get(hostname);
		if (h == null)
			throw new InvalidHost();
		return h;
	}

	public void setHostFacts(int hostid, Map<String, String> facts)
			throws TransportException, InvalidApiKey, InvalidHost {
	}

	public String getHostSetting(int hostid, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost {
		return defaultValue;
	}

	public String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey {
		return defaultValue;
	}

	public void log(int jobletId, int jobletStatus, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		log.info(String.format("log(%1$d, %2$d, %3$s", jobletId, jobletStatus,
				message));
	}

	public JobletResult gridExecute(Joblet joblet, int retries)
			throws InvalidApiKey {
		log.debug("Dummy transport doing gridExecute()");
		return gridClient.execute(joblet);
	}

	public void completeJoblet(int jobletId, JobletResult result, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		Joblet j = activeQueue.get(jobletId);
		if (j == null)
			throw new InvalidJobletId();
		j.setStatus(result.getStatus());
		activeQueue.remove(jobletId);
		result.setJoblet(j);
		results.put(jobletId, result);
		// complete job if all joblets completed
		Job job = jobs.get(j.getJobId());
		boolean finished = true;
		boolean failed = false;
		for (Joblet joblet : job.getJoblets()) {
			if ((joblet.getStatus() == JOB_STATUS.PROCESSING)
					|| (joblet.getStatus() == JOB_STATUS.QUEUED)
					|| (joblet.getStatus() == JOB_STATUS.RECEIVED)) {
				finished = false;
				break;
			} else if (joblet.getStatus() == JOB_STATUS.FAILED)
				failed = true;
		}
		if (finished) {
			int status = (failed) ? JOB_STATUS.FAILED : JOB_STATUS.COMPLETED;
			job.setStatus(status);
			try {
				if (job.getCallbackType() != 0)
					executeCallback(job);
			} catch (InvalidJobId e) {
				log.warn("Could not call executeCallback()", e);
			}
		}
	}

	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
			InvalidJobId {
		Job j = jobs.get(jobId);
		if (j == null)
			throw new InvalidJobId();
		return j;
	}

	public int getJobletQueueSize() throws TransportException, InvalidApiKey {
		return workQueue.size();
	}

	public List<Joblet> listActiveJoblets(String submitter, int offset,
			int limit) throws TransportException, InvalidApiKey {
		log.trace("getJobletQueueSize()");
		List<Joblet> results = new ArrayList<Joblet>();
		for (Joblet joblet : workQueue) {
			if (submitter != null) {
				if (joblet.getSubmitter().equals(submitter))
					results.add(joblet);
			} else {
				results.add(joblet);
			}
			// don't worry about offset
			if (results.size() >= limit)
				break;
		}
		return results;
	}

	public JobletResult getJobletResult(int jobletId)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		JobletResult jr = results.get(jobletId);
		if (jr == null)
			throw new InvalidJobletId();
		return jr;
	}

	public List<JobletResult> getResults(int jobId) throws TransportException,
			InvalidApiKey, InvalidJobId {
		List<JobletResult> values = new LinkedList<JobletResult>();
		Enumeration<JobletResult> e = results.elements();
		while (e.hasMoreElements()) {
			JobletResult jr = e.nextElement();
			if (jr.getJoblet().getJobId() == jobId)
				values.add(jr);
		}
		return values;
	}

	public Joblet getWork() throws TransportException, InvalidApiKey,
			InvalidHost, NoWorkAvailable {
		try {
			Joblet joblet = workQueue.remove(0);
			activeQueue.put(joblet.getId(), joblet);
			return joblet;
		} catch (IndexOutOfBoundsException e) {
			throw new NoWorkAvailable();
		}
	}

	public void releaseJoblet(int jobletId) throws InvalidApiKey,
			TransportException, InvalidJobletId {
		Joblet j = activeQueue.get(jobletId);
		if (j == null)
			throw new InvalidJobletId();
		j.setStatus(JOB_STATUS.QUEUED);
		workQueue.add(j);
	}

	public Job submitHostJob(String hostname, Job job)
			throws TransportException, InvalidHost, InvalidApiKey {
		return submitJob(job);
	}

	public Joblet submitHostJoblet(String hostname, Joblet joblet, int jobId,
			int callbackType, String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId, InvalidHost {
		return submitJoblet(joblet, jobId, callbackType, callbackAddress,
				callbackContent);
	}

	public Job submitJob(Job job) throws TransportException, InvalidApiKey {
		job.setId(random.nextInt());
		jobs.put(job.getId(), job);
		for (Joblet joblet : job.getJoblets()) {
			joblet.setId(random.nextInt());
			joblet.setJobId(job.getId());
			workQueue.add(joblet);
		}
		return job;
	}

	public Joblet submitJoblet(Joblet joblet, int jobId, int callbackType,
			String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId {
		Job j = null;
		if (jobId != 0) {
			j = getJob(jobId);
		} else {
			j = new Job(random.nextInt(), 0l, joblet.getSubmitter(), joblet
					.getDescription(), new ArrayList<Joblet>(1), 0, "", "",
					JOB_STATUS.QUEUED);
			joblet.setJobId(j.getId());
			jobs.put(j.getId(), j);
		}
		joblet.setJobId(j.getId());
		joblet.setId(random.nextInt());
		j.getJoblets().add(joblet);
		workQueue.add(joblet);
		return joblet;
	}

	private void executeCallback(Job job) throws TransportException,
			InvalidApiKey, InvalidJobId {
		Map<String, String> params = new HashMap<String, String>();
		params.put("jobId", Integer.toString(job.getId()));
		params.put("jobStatus", Integer.toString(job.getStatus()));
		params.put("callbackType", Integer.toString(job.getCallbackType()));
		params.put("callbackAddress", job.getCallbackAddress());
		Joblet callbackJoblet = new Joblet(0, 0l, 0, 0, job.getSubmitter(), 2,
				CallbackJobletTypeHandler.TYPE, "Completion callback", params,
				"", job.getStatus());
		this.submitJoblet(callbackJoblet, 0, JOB_CALLBACK_TYPES.NONE, "", "");
	}

	private String getHostname() {
		return HostnameDiscovery.getHostname();
	}

}
