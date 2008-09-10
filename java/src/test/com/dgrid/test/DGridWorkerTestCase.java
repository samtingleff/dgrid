package com.dgrid.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dgrid.gen.Constants;
import com.dgrid.gen.Host;
import com.dgrid.gen.JOB_CALLBACK_TYPES;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.handlers.JavaAppJobletTypeHandler;
import com.dgrid.handlers.JavaJobletTypeHandler;
import com.dgrid.handlers.SystemJobletTypeHandler;
import com.dgrid.test.plugins.SimpleJunit3TestCase;
import com.dgrid.test.plugins.SimpleJunit4TestCase;
import com.dgrid.test.resources.SimpleJavaJob;
import com.dgrid.test.resources.SimpleJavaJob2;

public class DGridWorkerTestCase extends BaseTestCase {

	public void testJob() throws Exception {
		String msg1 = "hello world";
		String msg2 = "goodbye world";
		List<Joblet> joblets = new ArrayList<Joblet>(2);
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(JavaJobletTypeHandler.CLASS_NAME_PARAM, SimpleJavaJob.class
				.getName());
		Joblet joblet1 = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 1", params, msg1,
				JOB_STATUS.RECEIVED);
		Joblet joblet2 = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 2", params, msg2,
				JOB_STATUS.RECEIVED);
		joblets.add(joblet1);
		joblets.add(joblet2);

		Job job = new Job(0, 0l, getHostname(), "Test job", joblets,
				JOB_CALLBACK_TYPES.NONE, "", "", JOB_STATUS.RECEIVED);
		int jobId = gridClient.submitJob(job);

		// submit another joblet to same job
		Joblet joblet3 = new Joblet(0, 0l, jobId, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 3", params, msg2,
				JOB_STATUS.RECEIVED);
		gridClient.submitJoblet(joblet3, jobId);

		// work 1
		JobletResult result1 = super.doWork();
		assertEquals(result1.getDetails(), msg1);
		// work 2
		JobletResult result2 = super.doWork();
		assertEquals(result2.getDetails(), msg2);
		// work 3
		JobletResult result3 = super.doWork();
		assertEquals(result3.getDetails(), msg2);

		Job result = gridClient.getJob(jobId);
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testJavaJoblet() throws Exception {
		String msg = "Hello, world";
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(JavaJobletTypeHandler.CLASS_NAME_PARAM, SimpleJavaJob.class
				.getName());
		Map<String, String> hostRequirements = new HashMap<String, String>(0);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 2", params, msg,
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getDetails(), msg);
	}

	public void testJavaJoblet2() throws Exception {
		Map<String, String> params = new HashMap<String, String>(3);
		params.put(JavaAppJobletTypeHandler.CLASS_NAME_PARAM,
				SimpleJavaJob2.class.getName());
		params.put(JavaAppJobletTypeHandler.METHOD_NAME_PARAM, "main");
		params.put(JavaAppJobletTypeHandler.STATIC_PARAM, Boolean
				.toString(true));

		Map<String, String> hostRequirements = new HashMap<String, String>(0);

		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_APP_JOBLET, "Joblet 2", params, "",
				JOB_STATUS.RECEIVED);

		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testSystemJoblet() throws Exception {
		String msg = "hello, world";
		String command = String.format("echo -n %1$s", msg);
		Map<String, String> params = new HashMap<String, String>(2);
		params.put(SystemJobletTypeHandler.SAVE_OUTPUT_PARAM, Boolean
				.toString(true));

		Map<String, String> hostRequirements = new HashMap<String, String>(0);

		Joblet joblet1 = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.SYSTEM_JOBLET, "Simple echo joblet", params, command,
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet1, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getDetails(), msg);

		// again but without wanting standard out
		params.put(SystemJobletTypeHandler.SAVE_OUTPUT_PARAM, Boolean
				.toString(false));
		Joblet joblet2 = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.SYSTEM_JOBLET, "Simple echo joblet", params, null,
				JOB_STATUS.RECEIVED);
		jobletId = gridClient.submitJoblet(joblet1, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// get it back
		JobletResult result2 = gridClient.getJobletResult(jobletId);
		assertEquals(result2.getDetails(), "");
	}

	public void testJunit3Joblet() throws Exception {
		Map<String, String> params = new HashMap<String, String>(1);
		params.put("class", SimpleJunit3TestCase.class.getName());
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1, "junit",
				"Junit3 test case", params, "", JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// should have two new joblets in the queue
		int queueSize = gridClient.getJobletQueueSize();
		assertEquals(queueSize, 2);

		super.doWork();
		super.doWork();

		JobletResult result1 = gridClient.getJobletResult(jobletId);
		Job job = gridClient.getJob(result1.getJoblet().getJobId());
		assertEquals(job.getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testJunit4Joblet() throws Exception {
		Map<String, String> params = new HashMap<String, String>(1);
		params.put("class", SimpleJunit4TestCase.class.getName());
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1, "junit",
				"Junit4 test case", params, "", JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// should have two new joblets in the queue
		int queueSize = gridClient.getJobletQueueSize();
		assertEquals(queueSize, 2);

		super.doWork();
		super.doWork();

		JobletResult result1 = gridClient.getJobletResult(jobletId);
		Job job = gridClient.getJob(result1.getJoblet().getJobId());
		assertEquals(job.getStatus(), JOB_STATUS.FAILED);
	}

	public void testSubmitHostJoblet() throws Exception {
		String msg = "Hello, world";
		Host host = gridClient.getHost();
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(JavaJobletTypeHandler.CLASS_NAME_PARAM, SimpleJavaJob.class
				.getName());
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 2", params, msg,
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitHostJoblet(host.getHostname(), joblet,
				0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result.getDetails(), msg);
	}

	public void testEmailCallback() throws Exception {
		String msg = "Hello, world";
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(JavaJobletTypeHandler.CLASS_NAME_PARAM, SimpleJavaJob.class
				.getName());
		Map<String, String> hostRequirements = new HashMap<String, String>(0);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 2", params, msg,
				JOB_STATUS.RECEIVED);
		List<Joblet> joblets = new ArrayList<Joblet>(1);
		joblets.add(joblet);
		Job job = new Job(0, 0l, getHostname(), "Callback test", joblets,
				JOB_CALLBACK_TYPES.EMAIL, "sam@stevie.samnbree.net",
				"Callback test contents", JOB_STATUS.RECEIVED);
		int jobId = gridClient.submitJob(job);

		assertEquals(gridClient.getJobletQueueSize(), 1);

		super.doWork();

		// our callback should be at the top of the queue
		Joblet callbackJoblet = gridClient.getWork();
		assertEquals(callbackJoblet.getJobletType(), "callback");
		assertEquals(callbackJoblet.getParameters().get("jobId"), Integer
				.toString(jobId));
		gridClient.executeJoblet(callbackJoblet);
	}

	public void testSyncJobService() throws Exception {
		String msg = "Hello, world";
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(JavaJobletTypeHandler.CLASS_NAME_PARAM, SimpleJavaJob.class
				.getName());
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_JOBLET, "Joblet 2", params, msg,
				JOB_STATUS.RECEIVED);
		JobletResult result = gridClient.gridExecute(joblet, 1);
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testShutdownJoblet() throws Exception {
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.AGENT_SHUTDOWN_JOBLET, "Shut down agent",
				new HashMap<String, String>(0), "", JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitHostJoblet(getHostname(), joblet, 0);

		// work it
		Thread.sleep(1);
		super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getDetails(), Integer.toString(0));
	}

}
