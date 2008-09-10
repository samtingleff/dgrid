package com.dgrid.handlers;

import groovy.lang.GroovyClassLoader;

import java.io.File;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.api.MapReduceJob;
import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.io.InputStreamUtils;

public class GroovyMapReduceTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	public static final String SCRIPT_PARAM = "script";

	private File dir;

	private JavaMapReduceTypeHandler delegate;

	private GroovyClassLoader loader;

	public GroovyMapReduceTypeHandler(File dir) {
		this.dir = dir;
		this.loader = new GroovyClassLoader(getClass().getClassLoader());
		this.delegate = new JavaMapReduceTypeHandler();
	}

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Throwable {
		String script = joblet.getParameters().get(SCRIPT_PARAM);
		MapReduceJob mrj = null;
		String code = null;
		if (script == null) {
			// get from joblet contents
			code = joblet.getContent();
		} else {
			File file = new File(this.dir, script);
			code = InputStreamUtils.getFileAsString(file);
		}
		mrj = compile(code);
		JobletResult jr = null;
		if (joblet.getJobletType().equals(Constants.GROOVY_MR_JOB)) {
			jr = delegate.map(mrj, joblet, gridClient);
			Joblet reducer = new Joblet(0, 0l, joblet.getJobId(), 0, joblet
					.getSubmitter(), joblet.getPriority(),
					Constants.GROOVY_MR_REDUCER, "", joblet.getParameters(),
					joblet.getContent(), JOB_STATUS.SAVED);
			gridClient.submitJoblet(reducer, joblet.getJobId());
		} else if (joblet.getJobletType().equals(Constants.GROOVY_MR_REDUCER))
			jr = delegate.reduce(Constants.GROOVY_MR_JOB, mrj, joblet,
					gridClient);
		else
			throw new UnsupportedOperationException(String.format(
					"Unknown joblet type (%1$s)", joblet.getJobletType()));
		return jr;
	}

	protected MapReduceJob compile(String code) throws Exception {
		log.trace("compile()");
		Class clz = loader.parseClass(code);
		Object obj = clz.newInstance();
		MapReduceJob mrj = (MapReduceJob) obj;
		return mrj;
	}
}
