package groovy.mr;

import java.io.*;
import java.util.Map;
import java.util.List;

import com.dgrid.api.MapReduceJob;
import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Constants;
import com.dgrid.gen.Host;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.helpers.ForkJoinHelper;

class MapReduceTest implements MapReduceJob {
	public List<Joblet> getMappers(Joblet joblet, DGridClient gridClient) {
		def joblets = [];
		for (int i = 0; i < 4; ++i) {
			joblets << new Joblet(0, 0l, joblet.jobId, 0, joblet.submitter,
					joblet.priority, Constants.SYSTEM_JOBLET, "",
					["output":"true"], "expr 1 + 2", JOB_STATUS.RECEIVED);
		}
		return joblets;
	}

	public SimpleJobletResult reduce(List<JobletResult> results) {
		int retval = 0;
		results.each { jr ->
			int value = Integer.parseInt(jr.details.trim());
			retval += value;
		};
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, Integer.toString(retval));
	}
}
