package com.dgrid.threads;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidJobletId;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.io.HostnameDiscovery;

public class DGridRunnable implements Callable<JobletResult> {
	private Log log = LogFactory.getLog(getClass());

	private DGridClient client;

	private Joblet joblet;

	public DGridRunnable(DGridClient client, Joblet joblet) {
		this.client = client;
		this.joblet = joblet;
	}

	public Joblet getJoblet() {
		return joblet;
	}

	public JobletResult call() throws Exception {
		log.trace("call()");
		try {
			client.log(joblet.getId(), JOB_STATUS.PROCESSING, String.format(
					"Joblet %1$d started on %2$s", joblet.getId(),
					HostnameDiscovery.getHostname()));
		} catch (TransportException e) {
			log.warn("TransportException calling client.log()", e);
		} catch (InvalidApiKey e) {
			log.warn("InvalidApiKey calling client.log()", e);
		} catch (InvalidJobletId e) {
			log.warn("InvalidJobletId calling client.log()", e);
		} catch (Exception e) {
			log.warn("Exception calling client.log()", e);
		}
		JobletResult result = client.executeJoblet(joblet);
		return result;
	}

}
