package com.dgrid.test;

import java.util.HashMap;
import java.util.Map;

import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.handlers.GroovyJobletTypeHandler;

public class SciptJobletsTestCase extends BaseTestCase {
	public void testGroovyJoblet() throws Exception {
		String testValue = "testValue";
		String code = "SimpleGroovyJoblet.groovy";
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("testKey", testValue);
		params.put(GroovyJobletTypeHandler.SCRIPT_PARAM, code);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Sample groovy joblet", params, "",
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		JobletResult result1 = super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result.getDetails(), testValue);
	}

	public void testGroovyJobletCodeAsData() throws Exception {
		String testValue = "testValue";
		// test sending code as data
		String code = "package groovy.getJoblet()s;\n"
				+ "import com.dgrid.api.SimpleJoblet;\n"
				+ "import com.dgrid.api.SimpleJobletResult;\n"
				+ "import com.dgrid.gen.Joblet;\n"
				+ "import com.dgrid.gen.JOB_STATUS;\n"
				+ "import com.dgrid.gen.JobletResult;\n"
				+ "import com.dgrid.service.DGridClient;\n"
				+ "class MySimpleJoblet implements SimpleJoblet {\n"
				+ "public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient) {\n"
				+ "		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, joblet.parameters.get(\"testKey\"));\n"
				+ "	}\n" + "}";

		Map<String, String> params = new HashMap<String, String>(2);
		params.put("testKey", testValue);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Sample groovy joblet", params, code,
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		JobletResult result2 = super.doWork();

		// get it back
		JobletResult result3 = gridClient.getJobletResult(jobletId);
		assertEquals(result3.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result3.getDetails(), testValue);
	}

	public void testGroovyCodeAsData() throws Exception {
		// test sending code as data
		String code = "def add = { x,y -> x + y }\n def a = add(1, 2);\nassert a == 3; \nreturn a;";
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Sample groovy joblet",
				new HashMap<String, String>(0), code, JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		JobletResult result2 = super.doWork();

		// get it back
		JobletResult result3 = gridClient.getJobletResult(jobletId);
		assertEquals(result3.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result3.getDetails(), Integer.toString(3));

		// test that we can compile without execution
		Map<String, String> params = new HashMap<String, String>(1);
		params.put("execute", "false");
		joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Sample groovy joblet", params, code,
				JOB_STATUS.RECEIVED);
		jobletId = gridClient.submitJoblet(joblet, 0);
		JobletResult result4 = super.doWork();

		// get it back
		JobletResult result5 = gridClient.getJobletResult(jobletId);
		assertEquals(result5.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result5.getDetails(), "");

		// send a syntax error
		joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Sample groovy joblet", params, code
						+ "jasdfsdfasdf {} (([[[", JOB_STATUS.RECEIVED);
		jobletId = gridClient.submitJoblet(joblet, 0);
		JobletResult result6 = super.doWork();

		// get it back
		JobletResult result7 = gridClient.getJobletResult(jobletId);
		assertEquals(result7.getJoblet().getStatus(), JOB_STATUS.FAILED);
	}

	public void testSimpleJavascriptJoblet() throws Exception {
		String testValue = "testValue";
		String code = "testjoblet.js";
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("testKey", testValue);
		params.put(GroovyJobletTypeHandler.SCRIPT_PARAM, code);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVASCRIPT_JOBLET, "Sample javascript joblet",
				params, "", JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		JobletResult result1 = super.doWork();

		// get it back
		JobletResult result = gridClient.getJobletResult(jobletId);
		assertEquals(result.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result.getDetails(), testValue);

		Job job = gridClient.getJob(result.getJoblet().getJobId());
		assertEquals(job.getStatus(), JOB_STATUS.COMPLETED);

	}

	public void testSimpleJavascriptJobletAsData() throws Exception {
		String testValue = "hello, world";
		// test sending code as data
		String code = "grid = Packages.com.dgrid.api;\n"
				+ "thrift = Packages.com.dgrid.gen;\n"
				+ "function execute(joblet, gridClient) {\n"
				+ "	return grid.SimpleJobletResult(0, thrift.JOB_STATUS.COMPLETED,\n"
				+ "			joblet.parameters.get('testKey'));\n" + "}";

		Map<String, String> params = new HashMap<String, String>(2);
		params.put("testKey", testValue);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVASCRIPT_JOBLET, "Sample javascript joblet",
				params, code, JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		JobletResult result3 = super.doWork();

		// get it back
		result3 = gridClient.getJobletResult(jobletId);
		assertEquals(result3.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result3.getDetails(), testValue);
	}

	public void testSimpleJavascriptCodeAsData() throws Exception {
		// test sending code as data
		String code = "function add(x, y) { return x + y; }\nz = add(1,2);";

		Map<String, String> params = new HashMap<String, String>(1);
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVASCRIPT_JOBLET, "Sample javascript joblet",
				params, code, JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0);

		// work it
		Thread.sleep(1);
		JobletResult result3 = super.doWork();

		// get it back
		result3 = gridClient.getJobletResult(jobletId);
		assertEquals(result3.getJoblet().getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result3.getDetails(), Double.toString(3.0d));
	}
}
