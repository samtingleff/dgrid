package com.dgrid.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.handlers.GroovyMapReduceTypeHandler;
import com.dgrid.handlers.JavaMapReduceTypeHandler;
import com.dgrid.test.mr.SimpleMapReduceJob;

public class MapReduceTestCase extends BaseTestCase {

	public void testJavaMapReduce() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(JavaMapReduceTypeHandler.CLASS_NAME_PARAM,
				SimpleMapReduceJob.class.getName());

		int[] ints = new int[16];
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int correct = 0;
		for (int i = 0; i < ints.length; ++i) {
			int v = r.nextInt(100);
			correct += v;
			sb.append(v);
			sb.append(',');
		}
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.JAVA_MR_JOB, "Sample m/r job", params, sb.toString(),
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0).getId();

		// work it
		Thread.sleep(1);

		JobletResult result = null;
		try {
			while (true) {
				result = super.doWork();
			}
		} catch (NoWorkAvailable e) {
		}

		// get it back
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result.getDetails(), Integer.toString(correct));
	}

	public void testGroovyMapReduce() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(GroovyMapReduceTypeHandler.SCRIPT_PARAM,
				"MapReduceTest.groovy");
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_MR_JOB, "Sample groovy m/r job", params, "",
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitJoblet(joblet, 0).getId();

		JobletResult result = null;
		try {
			while (true) {
				result = super.doWork();
			}
		} catch (NoWorkAvailable e) {
		}
		// get it back
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
		assertEquals(result.getDetails(), Integer.toString(12));
	}
}
