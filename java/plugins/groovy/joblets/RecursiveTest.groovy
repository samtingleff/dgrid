package groovy.joblets

import java.io.*;
import java.util.Map;
import java.util.List;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.helpers.ForkJoinHelper;

class RecursiveTest implements SimpleJoblet {

	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		String content = joblet.content;
		int[] nums = stringToInts(content);
		int result = 0;
		if (nums.length <= 10) {
			result = solveSequentially(nums);
		} else {
			int midpoint = nums.length / 2;
			int rightLimit = ((nums.length % 2) == 0) ? midpoint : (midpoint + (nums.length % 2));
			int[] leftArray = new int[midpoint];
			int[] rightArray = new int[rightLimit];
			System.arraycopy(nums, 0, leftArray, 0, midpoint);
			System.arraycopy(nums, midpoint, rightArray, 0, rightLimit);
			String leftContents = intsToString(leftArray);
			String rightContents = intsToString(rightArray);
			ForkJoinHelper fjh = (ForkJoinHelper) gridClient.getBean(ForkJoinHelper.NAME);
			List joblets = new ArrayList<Joblet>(2);
			joblets.add(new Joblet(0, 0l, joblet.jobId, 0, joblet.submitter, joblet.priority,
					joblet.jobletType, joblet.description, joblet.parameters,
					leftContents, joblet.status));
			joblets.add(new Joblet(0, 0l, joblet.jobId, 0, joblet.submitter, joblet.priority,
					joblet.jobletType, joblet.description, joblet.parameters,
					rightContents, joblet.status));
			List results = fjh.gridExecuteConcurrently(joblets, 2, 1);
			result = Integer.parseInt(results.get(0).details) +
				Integer.parseInt(results.get(1).details);
		}
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, Integer.toString(result));
	}

	private int solveSequentially(int[] nums) {
		return reduce({ x, y -> return x + y; }, nums, 0);
	}

	private String intsToString(int[] nums) {
		return nums.toList().join(',');
	}

	private int[] stringToInts(String str) {
		String[] strings = str.split(",");
		int[] ints = new int[strings.length];
		map({return Integer.parseInt(it)}, strings, ints, 0, strings.length);
		return ints;
	}

	private void map(fn, source, dest, offset, limit) {
		for (int i = offset; i < (offset + limit); i++) {
			dest[i] = fn(source[i]);
		}
	}

	private reduce(fn, a, init) {
		def s = init;
		for (int i = 0; i < a.length; i++) {
			s = fn(s, a[i] );
		}
		return s;
	}
}
