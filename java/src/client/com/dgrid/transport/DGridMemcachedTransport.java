package com.dgrid.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Host;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.InvalidJobletId;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.NoHostAvailable;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.helpers.MemcachedHelper;
import com.dgrid.service.DGridTransport;

public class DGridMemcachedTransport implements DGridTransport {
	private static final String HOSTS = "hosts";

	private static final String HOST_REFERENCES = "hosts.references";

	private static final String SETTINGS = "settings";

	private static final String HOST_SETTINGS = "settings.hosts";

	private static final String JOBS = "jobs";

	private static final String JOBLETS = "joblets";

	private static final String SAVED_JOBLETS = "joblets.saved";

	private static final String JOBLET_REFERENCES = "joblets.references";

	private static final String SAVED_JOBLET_REFERENCES = "joblets.saved.references";

	private static final String JOBLET_RESULTS = "joblets.results";

	private static final String JOBLET_QUEUE = "joblets.queue";

	private static final String SAVED_JOBLET_QUEUE = "joblets.saved.queue";

	private Log log = LogFactory.getLog(getClass());

	private MemcachedHelper mcc;

	public void setMemcachedHelper(MemcachedHelper mcc) {
		this.mcc = mcc;
	}

	public void setApiKey(String apiKey) {
		log.trace("setApiKey()");
	}

	public void setEndpoint(String endpoint) {
		log.trace("setEndpoint()");
	}

	public void setPort(int port) {
		log.trace("setPort()");
	}

	public void completeJoblet(int jobletId, JobletResult result,
			String logMessage) throws TransportException, InvalidApiKey,
			InvalidJobletId {
		log.trace("completeJoblet()");
		Joblet joblet = (Joblet) mcc.get(getKey(JOBLETS, jobletId));
		if (joblet == null)
			throw new InvalidJobletId();

		// save the result
		result.setId(jobletId);
		result.setTimeCreated(System.currentTimeMillis());
		result.setJoblet(null);
		mcc.set(getKey(JOBLET_RESULTS, result.getId()), result);
		// update joblet status
		joblet.setStatus(result.getStatus());
		mcc.set(getKey(JOBLETS, jobletId), joblet);

		// get the job
		Job job = (Job) mcc.get(getKey(JOBS, joblet.getJobId()));
		if (job == null)
			throw new InvalidJobletId();

		// TODO:
		// check for job completion
		boolean isCompleted = true;
		boolean success = false;

		int[] jobletIds = (int[]) mcc.get(getKey(JOBLET_REFERENCES, joblet
				.getJobId()));
		List<Integer> savedJobs = new ArrayList<Integer>(0);
		for (int i = 0; i < jobletIds.length; ++i) {
			Joblet j = (Joblet) mcc.get(getKey(JOBLETS, jobletIds[i]));
			int status = j.getStatus();
			// if status is received, queued or processing, we're still chugging
			// along
			if ((status >= JOB_STATUS.RECEIVED)
					&& (status <= JOB_STATUS.PROCESSING)) {
				isCompleted = false;
				break;
			} else if (status == JOB_STATUS.SAVED) {
				savedJobs.add(new Integer(j.getId()));
			} else if (status == JOB_STATUS.COMPLETED) {
				success = true;
			}
		}
		if (isCompleted && (savedJobs.isEmpty())) {
			// completed, no saved jobs to execute
			// set job status and perform any callbacks
			int status = (success) ? JOB_STATUS.COMPLETED : JOB_STATUS.FAILED;
			job.setStatus(status);
			mcc.set(getKey(JOBS, job.getId()), job);
		} else if (isCompleted) {
			// completed but have saved jobs to exec
			for (Integer i : savedJobs) {
				Joblet j = (Joblet) mcc
						.get(getKey(SAVED_JOBLETS, i.intValue()));
				int oldJobletId = j.getId();
				j.setStatus(JOB_STATUS.RECEIVED);
				j.setId(createId(JOBLETS));
				// post to joblets
				mcc.set(getKey(JOBLETS, j.getId()), j);
				// delete from saved joblets
				mcc.delete(getKey(SAVED_JOBLETS, oldJobletId));
			}
		}
		// complete the job
	}

	public Host getHost(int id) throws TransportException, InvalidApiKey,
			InvalidHost {
		log.trace("getHost()");
		Host host = (Host) mcc.get(getKey(HOSTS, id));
		if (host == null)
			throw new InvalidHost();
		else
			return host;
	}

	public Host getHostByName(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		log.trace("getHostByName()");
		String hostid = (String) mcc.get(getKey(HOST_REFERENCES, hostname));
		if (hostid == null) {
			throw new InvalidHost();
		} else {
			Host host = (Host) mcc.get(getKey(HOSTS, Integer.parseInt(hostid)));
			if (host == null) {
				throw new InvalidHost();
			} else {
				return host;
			}
		}
	}

	public String getHostSetting(int hostid, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost {
		log.trace("getHostSetting()");
		String key = getKey(HOST_SETTINGS, String.format("%1$d.%2$s", hostid,
				name));
		String value = (String) mcc.get(key);
		if (value == null) {
			mcc.set(key, defaultValue);
			value = defaultValue;
		}
		return value;
	}

	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
			InvalidJobId {
		log.trace("getJob()");
		Job job = (Job) mcc.get(getKey(JOBS, jobId));
		if (job == null)
			throw new InvalidJobId();
		else {
			// need to fill in joblets
			int[] jobletIds = (int[]) mcc.get(getKey(JOBLET_REFERENCES, jobId));
			if (jobletIds == null) {
				job.setJoblets(Collections.EMPTY_LIST);
			} else {
				List<Joblet> joblets = new ArrayList<Joblet>(jobletIds.length);
				for (int i = 0; i < jobletIds.length; ++i) {
					Joblet joblet = (Joblet) mcc.get(getKey(JOBLETS,
							jobletIds[i]));
					joblets.add(joblet);
				}
				job.setJoblets(joblets);
			}
		}
		return job;
	}

	public int getJobletQueueSize() throws TransportException, InvalidApiKey {
		log.trace("getJobletQueueSize()");
		throw new NoSuchMethodError();
	}

	public List<Joblet> listActiveJoblets(String submitter, int offset,
			int limit) throws TransportException, InvalidApiKey {
		log.trace("getJobletQueueSize()");
		throw new NoSuchMethodError();
	}

	public JobletResult getJobletResult(int jobletId)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		log.trace("getJobletResult()");
		JobletResult result = (JobletResult) mcc.get(getKey(JOBLET_RESULTS,
				jobletId));
		if (result == null)
			throw new InvalidJobletId();
		else {
			// set joblet reference
			Joblet joblet = (Joblet) mcc.get(getKey(JOBLETS, jobletId));
			result.setJoblet(joblet);
		}
		return result;
	}

	public List<JobletResult> getResults(int jobId) throws TransportException,
			InvalidApiKey, InvalidJobId {
		log.trace("getResults()");
		int[] jobletIds = (int[]) mcc.get(getKey(JOBLET_REFERENCES, jobId));
		if (jobletIds == null)
			throw new InvalidJobId();
		else {
			List<JobletResult> results = new ArrayList<JobletResult>(
					jobletIds.length);
			for (int i = 0; i < jobletIds.length; ++i) {
				JobletResult jr = (JobletResult) mcc.get(getKey(JOBLET_RESULTS,
						jobletIds[i]));
				if (jr != null) {
					// set joblet reference
					Joblet joblet = (Joblet) mcc.get(getKey(JOBLETS,
							jobletIds[i]));
					jr.setJoblet(joblet);
					results.add(jr);
				}
			}
			return results;
		}
	}

	public String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey {
		log.trace("getSetting()");
		String key = getKey(SETTINGS, name);
		String value = (String) mcc.get(key);
		if (value == null) {
			mcc.set(key, defaultValue);
			value = defaultValue;
		}
		return value;
	}

	public Joblet getWork() throws TransportException, InvalidApiKey,
			InvalidHost, NoWorkAvailable {
		log.trace("getWork()");
		int counter = getId(JOBLET_QUEUE);
		if (counter < 0) {
			throw new NoWorkAvailable();
		} else {
			// get the joblet at named counter
			Joblet joblet = (Joblet) mcc.get(getKey(JOBLETS, counter));
			if (joblet != null) {
				// update joblet status
				joblet.setStatus(JOB_STATUS.QUEUED);
				mcc.set(getKey(JOBLETS, joblet.getId()), joblet);
				// increment counter
				int next = createId(JOBLET_QUEUE);
				return joblet;
			} else {
				throw new NoWorkAvailable();
			}
		}
	}

	public JobletResult gridExecute(Joblet joblet, int retries)
			throws InvalidApiKey, TransportException, NoHostAvailable {
		log.trace("gridExecute()");
		// TODO Auto-generated method stub
		return null;
	}

	public void log(int jobletId, int jobletStatus, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		log.trace("log()");
		// TODO Auto-generated method stub
	}

	public Host registerHost(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		log.trace("registerHost()");
		Host host = null;
		String hostid = (String) mcc.get(getKey(HOST_REFERENCES, hostname));
		if (hostid == null) {
			int id = createId(HOSTS);
			host = new Host(id, hostname, new HashMap<String, String>());
			mcc.set(getKey(HOSTS, id), host);
			// register alias from hostname -> id
			mcc.set(getKey(HOST_REFERENCES, hostname), id);
		} else {
			int id = Integer.parseInt(hostid);
			host = (Host) mcc.get(getKey(HOSTS, id));
			if (host == null) {
				// strange - have an id reference but no matching host
				host = new Host(id, hostname, new HashMap<String, String>());
				mcc.set(getKey(HOSTS, id), host);
			}
		}
		return host;
	}

	public void releaseJoblet(int jobletId) throws InvalidApiKey,
			TransportException, InvalidJobletId {
		log.trace("releaseJoblet()");
		throw new NoSuchMethodError();
	}

	public void setHostFacts(int hostid, Map<String, String> facts)
			throws TransportException, InvalidApiKey, InvalidHost {
		log.trace("setHostFacts()");
		Host host = (Host) mcc.get(getKey(HOSTS, hostid));
		if (host == null) {
			throw new InvalidHost();
		} else {
			host.getFacts().putAll(facts);
			mcc.set(getKey(HOSTS, hostid), host);
		}
	}

	public Job submitJob(Job job) throws TransportException, InvalidApiKey {
		log.trace("submitJob()");
		int jobId = createId(JOBS);
		job.setId(jobId);
		job.setTimeCreated(System.currentTimeMillis());
		mcc.set(getKey(JOBS, jobId), job);
		int[] jobletReferences = new int[job.getJoblets().size()];
		for (int i = 0; i < jobletReferences.length; ++i) {
			Joblet joblet = job.getJoblets().get(i);
			int jobletId = createId(JOBLETS);
			joblet.setId(jobletId);
			joblet.setJobId(jobId);
			joblet.setTimeCreated(job.getTimeCreated());
			mcc.set(getKey(JOBLETS, jobletId), joblet);
			jobletReferences[i] = jobletId;
			if (jobletId == 1) {
				// set the work queue counter to at least 1
				createId(JOBLET_QUEUE);
			}
		}
		// populate job -> joblets reference
		mcc.set(getKey(JOBLET_REFERENCES, jobId), jobletReferences);
		return job;
	}

	public Joblet submitJoblet(Joblet joblet, int jobId, int callbackType,
			String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId {
		log.trace("submitJoblet()");
		String queueKey = (joblet.getStatus() == JOB_STATUS.SAVED) ? SAVED_JOBLETS
				: JOBLETS;
		String referenceKey = (joblet.getStatus() == JOB_STATUS.SAVED) ? SAVED_JOBLET_REFERENCES
				: JOBLET_REFERENCES;
		String queueCounterKey = (joblet.getStatus() == JOB_STATUS.SAVED) ? SAVED_JOBLET_QUEUE
				: JOBLET_QUEUE;
		int jobletId = createId(queueKey);
		joblet.setId(jobletId);
		joblet.setTimeCreated(System.currentTimeMillis());
		if (jobId == 0) {
			// create the job
			jobId = createId(JOBS);
			Job job = new Job(jobId, joblet.getTimeCreated(), null, null, null,
					callbackType, callbackAddress, callbackContent,
					JOB_STATUS.RECEIVED);
			mcc.set(getKey(JOBS, jobId), job);
			// populate job -> joblets reference
			mcc.set(getKey(referenceKey, jobId), new int[] { jobletId });
		} else {
			int[] jobletIds = (int[]) mcc.get(getKey(referenceKey, jobId));
			int[] newJobletIds = new int[jobletIds.length];
			for (int i = 0; i < jobletIds.length; ++i) {
				newJobletIds[i] = jobletIds[i];
			}
			newJobletIds[newJobletIds.length - 1] = jobletId;
		}
		joblet.setJobId(jobId);
		mcc.set(getKey(queueKey, jobletId), joblet);
		if (jobletId == 1) {
			// set the work queue counter to at least 1
			createId(queueCounterKey);
		}
		return joblet;
	}

	private List<Joblet> getJoblets(int jobId, String referenceKey)
			throws InvalidJobId {
		log.trace("getJoblets()");
		int[] jobletIds = (int[]) mcc.get(getKey(referenceKey, jobId));
		if (jobletIds == null)
			throw new InvalidJobId();
		else {
			List<Joblet> joblets = new ArrayList<Joblet>(jobletIds.length);
			for (int i = 0; i < jobletIds.length; ++i) {
				Joblet joblet = (Joblet) mcc.get(getKey(JOBLETS, jobletIds[i]));
				joblets.add(joblet);
			}
			return joblets;
		}

	}

	private int getId(String classification) {
		log.trace("getId()");
		return (int) mcc.getCounter(getKey(classification, "counter"));
	}

	private int createId(String classification) {
		log.trace("createId()");
		String key = getKey(classification, "counter");
		return (int) mcc.addOrIncr(key, 1l);
	}

	private String getKey(String classification, int id) {
		return getKey(classification, Integer.toString(id));
	}

	private String getKey(String classification, String id) {
		return String.format("dgrid.%1$s.%2$s", classification, id);
	}
}
