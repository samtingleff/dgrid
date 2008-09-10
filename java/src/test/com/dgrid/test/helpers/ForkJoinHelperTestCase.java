package com.dgrid.test.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.handlers.SystemJobletTypeHandler;
import com.dgrid.helpers.ForkJoinHelper;
import com.dgrid.test.BaseTestCase;
import com.dgrid.util.io.HostnameDiscovery;

@SuppressWarnings("unchecked")
public class ForkJoinHelperTestCase extends BaseTestCase {

	public void testFJHelper() throws Exception {
		ForkJoinHelper fj = (ForkJoinHelper) super.getBean(ForkJoinHelper.NAME);
		Random random = new Random();
		List<Callable> tasks = new ArrayList<Callable>();
		int correctValue = 0;
		for (int i = 0; i < 11; ++i) {
			int[] data = generateRandomData(random, 1000, 10000);
			correctValue += sum(data);
			tasks.add(new Sum(data));
		}
		List results = fj.executeConcurrently(tasks, 10);
		int sum = sumObjects(results);
		assertEquals(correctValue, sum);
	}

	public void testFJHelperJoblets() throws Exception {
		ForkJoinHelper fj = (ForkJoinHelper) super.getBean(ForkJoinHelper.NAME);
		List<Joblet> joblets = new ArrayList<Joblet>(1);
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(SystemJobletTypeHandler.SAVE_OUTPUT_PARAM, Boolean
				.toString(true));
		Joblet joblet = new Joblet(0, 0l, 0, 0,
				HostnameDiscovery.getHostname(), 1, Constants.SYSTEM_JOBLET,
				"Concurrency test", params, "expr 1 + 1", JOB_STATUS.RECEIVED);
		joblets.add(joblet);
		List results = fj.gridExecuteConcurrently(joblets, 10, 1);
		for (Object object : results) {
			assertTrue(object instanceof JobletResult);
			JobletResult jr = (JobletResult) object;
			assertEquals(jr.getStatus(), JOB_STATUS.COMPLETED);
			assertEquals(jr.getDetails().trim(), "2");
		}
	}

	private int sum(int[] values) {
		int retval = 0;
		for (int i : values) {
			retval += i;
		}
		return retval;
	}

	private int sumObjects(List values) {
		int retval = 0;
		for (Object object : values) {
			retval += ((Integer) object).intValue();
		}
		return retval;
	}

	private int[] generateRandomData(Random random, int length, int max) {
		int[] i = new int[length];
		for (int x = 0; x < i.length; ++x) {
			i[x] = random.nextInt(max);
		}
		return i;
	}

	private static class Sum implements Callable<Integer> {
		private int[] data;

		public Sum(int[] data) {
			this.data = data;
		}

		public Integer call() throws Exception {
			int retval = 0;
			for (int i : data) {
				retval += i;
			}
			return new Integer(retval);
		}

	}
}
