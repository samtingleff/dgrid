package com.dgrid.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.dgrid.api.JobletTypeHandler;
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
import com.dgrid.gen.NoHostAvailable;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.plugin.PluginManager;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridPluginManager;
import com.dgrid.service.DGridTransport;
import com.dgrid.util.StackTraceUtil;

public class DGridClientImpl implements DGridClient, ApplicationContextAware {
	protected Log log = LogFactory.getLog(getClass());

	protected DGridPluginManager pluginManager;

	protected DGridTransport transport;

	protected ApplicationContext ctx;

	public DGridClientImpl() {
		log.trace("DGridClientImpl()");
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = (DGridPluginManager) pluginManager;
	}

	public void setTransport(DGridTransport transport) {
		this.transport = transport;
	}

	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	public Object getBean(String name) {
		return ctx.getBean(name);
	}

	public void setApiKey(String apiKey) {
		transport.setApiKey(apiKey);
	}

	public void setEndpoint(String endpoint) {
		transport.setEndpoint(endpoint);
	}

	public void setPort(int port) {
		transport.setPort(port);
	}

	public JobletResult executeJoblet(Joblet joblet) {
		log.trace("executeJoblet()");
		try {
			JobletResult result = execute(joblet);
			completeJoblet(joblet.getId(), result, "");
			return result;
		} catch (Exception e) {
			log.error("Exception in executeJoblet()", e);
			throw new RuntimeException(e);
		}
	}

	public JobletResult execute(Joblet joblet) {
		log.trace("execute()");
		long start = System.currentTimeMillis();
		JobletResult result = null;
		try {
			String type = joblet.getJobletType();
			JobletTypeHandler handler = pluginManager
					.getJobletTypeHandler(type);
			if (log.isDebugEnabled()) {
				log.debug(String.format("Joblet type: %1$s", type));
				log.debug(String.format("Handler: %1$s", handler));
				log.debug("Executing...");
			}
			result = handler.execute(joblet, this);
			if (log.isDebugEnabled()) {
				log
						.debug(String
								.format(
										"Joblet executed with return code (%1$s) and status (%2$d)",
										result.getReturnCode(), result
												.getStatus()));
			}
		} catch (Throwable e) {
			log.error("Error executing joblet", e);
			result = new JobletResult(0, 0l, 1,
					(System.currentTimeMillis() - start), JOB_STATUS.FAILED,
					StackTraceUtil.getStackTrace(e), joblet);
		}
		if (log.isDebugEnabled()) {
			log.debug("Joblet completed");
		}
		return result;
	}

	public JobletResult gridExecute(Joblet joblet, int retries)
			throws InvalidApiKey, TransportException, NoHostAvailable {
		log.trace("gridExecute()");
		return transport.gridExecute(joblet, retries);
	}

	public void completeJoblet(int jobletId, JobletResult result, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		transport.completeJoblet(jobletId, result, message);
	}

	public Host getHost() throws TransportException, InvalidApiKey, InvalidHost {
		return transport.getHost();
	}

	public Host getHostByName(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		return transport.getHostByName(hostname);
	}

	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
			InvalidJobId {
		return transport.getJob(jobId);
	}

	public JobletResult getJobletResult(int jobletId)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		return transport.getJobletResult(jobletId);
	}

	public List<JobletResult> getResults(int jobId) throws TransportException,
			InvalidApiKey, InvalidJobId {
		return transport.getResults(jobId);
	}

	public String getHostSetting(int hostid, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost {
		return transport.getHostSetting(hostid, name, defaultValue);
	}

	public String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey {
		return transport.getSetting(name, defaultValue);
	}

	public Joblet getWork() throws TransportException, InvalidApiKey,
			InvalidHost, NoWorkAvailable {
		return transport.getWork();
	}

	public void log(int jobletId, int jobletStatus, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		transport.log(jobletId, jobletStatus, message);
	}

	public Host registerHost(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		return transport.registerHost(hostname);
	}

	public void releaseJoblet(int jobletId) throws InvalidApiKey,
			TransportException, InvalidJobletId {
		transport.releaseJoblet(jobletId);
	}

	public void setHostFacts(int hostid, Map<String, String> facts)
			throws TransportException, InvalidApiKey, InvalidHost {
		transport.setHostFacts(hostid, facts);
	}

	public int submitHostJob(String hostname, Job job)
			throws TransportException, InvalidHost, InvalidApiKey {
		Host host = getHostByName(hostname);
		for (Joblet j : job.getJoblets()) {
			j.setHostId(host.getId());
		}
		return transport.submitJob(job);
	}

	public int submitHostJoblet(String hostname, Joblet joblet, int jobId,
			int callbackType, String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId, InvalidHost {
		Host host = getHostByName(hostname);
		joblet.setHostId(host.getId());
		return transport.submitJoblet(joblet, jobId, callbackType,
				callbackAddress, callbackContent);
	}

	public int submitHostJoblet(String hostname, Joblet joblet, int jobId)
			throws TransportException, InvalidApiKey, InvalidJobId, InvalidHost {
		return this.submitHostJoblet(hostname, joblet, jobId,
				JOB_CALLBACK_TYPES.NONE, "", "");
	}

	public int submitJob(Job job) throws TransportException, InvalidApiKey {
		return transport.submitJob(job);
	}

	public int submitJoblet(Joblet joblet, int jobId, int callbackType,
			String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId {
		return transport.submitJoblet(joblet, jobId, callbackType,
				callbackAddress, callbackContent);
	}

	public int submitJoblet(Joblet joblet, int jobId)
			throws TransportException, InvalidApiKey, InvalidJobId {
		return this
				.submitJoblet(joblet, jobId, JOB_CALLBACK_TYPES.NONE, "", "");
	}

	public int getJobletQueueSize() throws TransportException, InvalidApiKey {
		return transport.getJobletQueueSize();
	}
}