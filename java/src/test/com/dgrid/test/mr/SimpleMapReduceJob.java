package com.dgrid.test.mr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dgrid.api.MapReduceJob;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class SimpleMapReduceJob implements MapReduceJob {

	public List<Joblet> getMappers(Joblet joblet, DGridClient gridClient) {
		String mapper = "grid = Packages.com.dgrid.api;\n"
				+ "thrift = Packages.com.dgrid.gen;\n"
				+ "function execute(joblet, gridClient) {\n"
				+ " var ints_array = joblet.parameters.get('ints').split(',');\n"
				+ " var z = 0;\n"
				+ " for (key in ints_array) {\n"
				+ "  z += parseInt(ints_array[key]);\n"
				+ " }\n"
				+ "	return grid.SimpleJobletResult(0, thrift.JOB_STATUS.COMPLETED,\n"
				+ "			z);\n" + "}";

		List<Joblet> joblets = new LinkedList<Joblet>();
		// get an array of ints from joblet parameter
		String intArrayString = joblet.getContent();
		int[] ints = parseIntArray(intArrayString);
		int[][] splitInts = partition(ints);
		for (int i = 0; i < splitInts.length; ++i) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ints", joinIntArray(splitInts[i]));
			Joblet j = new Joblet(0, 0l, joblet.getJobId(), 0, joblet
					.getSubmitter(), joblet.getPriority(),
					Constants.JAVASCRIPT_JOBLET, "", params, mapper,
					JOB_STATUS.RECEIVED);
			joblets.add(j);
		}
		return joblets;
	}

	public SimpleJobletResult reduce(List<JobletResult> results,
			Map<String, String> parameters, DGridClient gridClient) {
		int retval = 0;
		for (JobletResult jr : results) {
			int v = Integer.parseInt(jr.getDetails());
			retval += v;
		}
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, Integer
				.toString(retval));
	}

	private int[] parseIntArray(String intArrayString) {
		String[] strings = intArrayString.split(",");
		int[] ints = new int[strings.length];
		for (int i = 0; i < ints.length; ++i) {
			ints[i] = Integer.parseInt(strings[i]);
		}
		return ints;
	}

	private int[][] partition(int[] ints) {
		int size = ints.length / 4;
		int[][] retval = new int[size][4];
		int pos = 0;
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < 4; ++j) {
				retval[i][j] = ints[pos];
				++pos;
			}
		}
		return retval;
	}

	private String joinIntArray(int[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(array[i]);
			sb.append(',');
		}
		return sb.toString();
	}
}
