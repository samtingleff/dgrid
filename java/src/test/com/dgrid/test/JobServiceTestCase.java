package com.dgrid.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.dgrid.gen.Constants;
import com.dgrid.gen.Host;
import com.dgrid.gen.JOB_CALLBACK_TYPES;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.JobService;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.util.io.HostnameDiscovery;
import com.facebook.thrift.protocol.TBinaryProtocol;
import com.facebook.thrift.protocol.TProtocol;
import com.facebook.thrift.transport.TFramedTransport;
import com.facebook.thrift.transport.TSocket;
import com.facebook.thrift.transport.TTransport;

public class JobServiceTestCase extends TestCase {
	private static final String apiKey = "changeme";

	private static final String submitter = "sam";

	private String hostname = HostnameDiscovery.getHostname();

	private JobService.Iface jobService;

	private TTransport transport;

	private Host host;

	public void setUp() throws Exception {
		TSocket socket = new TSocket("localhost", Constants.DEFAULT_PORT);
		this.transport = new TFramedTransport(socket);
		TProtocol protocol = new TBinaryProtocol(transport);
		this.jobService = new JobService.Client(protocol);
		transport.open();
		this.host = jobService.registerHost(apiKey, HostnameDiscovery
				.getHostname());
	}

	public void tearDown() throws Exception {
		transport.close();
	}

	public void testHostFacts() throws Exception {
		Host host1 = jobService.registerHost(apiKey, hostname);
		assertNotNull(host1.getId());
		Host host2 = jobService.getHostByName(apiKey, hostname);
		assertEquals(host1.getId(), host2.getId());

		// set some facts
		Map<String, String> facts = new HashMap<String, String>();
		facts.put("test.fact.1", "test.value.1");
		facts.put("test.fact.2", "test.value.2");
		facts.put("test.fact.3", "test.value.3");
		jobService.setHostFacts(apiKey, host2.getId(), facts);

		Host host3 = jobService.getHostByName(apiKey, hostname);
		Map<String, String> hostFacts = host3.getFacts();
		assertTrue(hostFacts.size() >= 3);
		for (String key : facts.keySet()) {
			assertEquals(facts.get(key), hostFacts.get(key));
		}
	}

	public void testSettings() throws Exception {
		String setting = "abc.123";
		String defaultValue = "xyz";
		String x = jobService.getSetting(apiKey, setting, defaultValue);
		assertEquals(x, defaultValue);
		x = jobService.getSetting(apiKey, setting, "what up gangsta");
		assertEquals(x, defaultValue);

		Host host = jobService.registerHost(apiKey, hostname);
		x = jobService.getHostSetting(apiKey, host.getId(), setting,
				defaultValue);
		assertEquals(x, defaultValue);
		x = jobService.getHostSetting(apiKey, host.getId(), setting,
				"what up gangsta");
		assertEquals(x, defaultValue);
	}

	public void testSubmitJoblet() throws Exception {
		Joblet joblet = createJoblet();
		int jobletId = jobService.submitJoblet(apiKey, joblet, 0,
				JOB_CALLBACK_TYPES.NONE, null, null).getId();

		joblet = jobService.getWork(apiKey, host.getId());
		assertEquals(joblet.getId(), jobletId);
		assertEquals(joblet.getStatus(), JOB_STATUS.QUEUED);

		JobletResult result = new JobletResult(0, 0l, 0, 1,
				JOB_STATUS.COMPLETED, "Details", joblet);
		jobService.completeJoblet(apiKey, host.getId(), jobletId, result,
				"Completed");
	}

	public void testSubmitJob() throws Exception {
		List<Joblet> joblets = new ArrayList<Joblet>(1);
		Joblet joblet1 = createJoblet();
		joblets.add(joblet1);
		Job job = new Job(0, 0l, submitter, "My first job", joblets,
				JOB_CALLBACK_TYPES.NONE, "", "", JOB_STATUS.RECEIVED);
		int jobId = jobService.submitJob(apiKey, job).getId();

		// sleep to guarantee second job comes after first
		Thread.sleep(500);

		// submit a single joblet to same job id
		Joblet joblet2 = createJoblet();
		jobService.submitJoblet(apiKey, joblet2, jobId,
				JOB_CALLBACK_TYPES.NONE, "", "");

		// get work
		Joblet work1 = jobService.getWork(apiKey, host.getId());

		// log that we have it
		jobService.log(apiKey, host.getId(), work1.getId(),
				JOB_STATUS.PROCESSING, String.format(
						"Processing joblet %1$d on %2$s", work1.getId(), host
								.getHostname()));

		// complete it
		JobletResult result = new JobletResult(0, 0l, 1, 100,
				JOB_STATUS.COMPLETED, work1.getContent(), work1);
		jobService.completeJoblet(apiKey, host.getId(), work1.getId(), result,
				"Completed dude");

		// complete it again
		Joblet work2 = jobService.getWork(apiKey, host.getId());

		// log that we have it
		jobService.log(apiKey, host.getId(), work2.getId(),
				JOB_STATUS.PROCESSING, String.format(
						"Processing joblet %1$d on %2$s", work2.getId(), host
								.getHostname()));

		result = new JobletResult(0, 0l, 1, 100, JOB_STATUS.COMPLETED, work2
				.getContent(), work2);
		jobService.completeJoblet(apiKey, host.getId(), work2.getId(), result,
				"Completed dude");

		// get last job back
		JobletResult completedJobletResult = jobService.getJobletResult(apiKey,
				work2.getId());

		// assert equals
		assertEquals(completedJobletResult.getDetails(), joblet1.getContent());

		// get queue size
		int queueSize = jobService.getJobletQueueSize(apiKey);
		assertEquals(queueSize, 0);
	}

	private Joblet createJoblet() {
		Joblet joblet = new Joblet();
		joblet.setDescription("Echo joblet");
		joblet.setContent("Hello world");
		joblet.setJobletType("java");
		joblet.setParameters(new HashMap<String, String>());
		joblet.getParameters().put("class", getClass().getName());
		joblet.setStatus(JOB_STATUS.RECEIVED);
		joblet.setSubmitter(submitter);
		return joblet;
	}

}
