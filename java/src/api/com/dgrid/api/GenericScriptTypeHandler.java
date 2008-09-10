package com.dgrid.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;

import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.io.InputStreamUtils;

public abstract class GenericScriptTypeHandler extends BaseJobletTypeHandler {
	public static final String SCRIPT_PARAM = "script";

	private File dir;

	private Hashtable<String, CacheEntry> cache = new Hashtable<String, CacheEntry>();

	public GenericScriptTypeHandler(File dir) {
		this.dir = dir;
	}

	protected abstract SimpleJoblet compile(String code) throws Exception;

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Throwable {
		log.trace("execute()");
		long start = System.currentTimeMillis();
		try {
			SimpleJoblet sj = instantiate(joblet);
			JobletResult jr = null;
			if (enableExecution(joblet)) {
				SimpleJobletResult sjr = sj.execute(joblet, gridClient);
				jr = new JobletResult(0, 0l, sjr.getReturnCode(), (System
						.currentTimeMillis() - start), sjr.getStatus(), sjr
						.getDetails(), joblet);
			} else {
				jr = new JobletResult(0, 0l, 0,
						(System.currentTimeMillis() - start),
						JOB_STATUS.COMPLETED, "", joblet);
			}
			return jr;
		} catch (NullPointerException npe) {
			log.error("NPE: script param: ("
					+ joblet.getParameters().get("script") + ")");
			throw (npe);
		}
	}

	protected SimpleJoblet instantiate(Joblet joblet) throws Exception {
		log.trace("instantiate()");
		SimpleJoblet sj = null;
		String script = joblet.getParameters().get(SCRIPT_PARAM);
		if (script == null) {
			// get from joblet contents
			String code = joblet.getContent();
			sj = compile(code);
		} else {
			File file = new File(this.dir, script);
			sj = readFromFile(script, file);
		}
		return sj;
	}

	private SimpleJoblet readFromFile(String script, File file)
			throws Exception {
		log.trace("readFromFile()");
		if (!file.exists())
			throw new FileNotFoundException(String.format(
					"The file %1$s does not exist!", file.getAbsolutePath()));
		SimpleJoblet sj = null;
		// first check cache
		CacheEntry ce = cache.get(script);
		if (ce != null) {
			// check file timestamp
			if (file.lastModified() > ce.time) {
				ce = null;
			}
		}
		if (ce == null) {
			// read from disk
			String code = InputStreamUtils.getFileAsString(file);
			sj = compile(code);
			cache.put(script, new CacheEntry(sj));
		} else {
			sj = ce.joblet;
		}
		return sj;
	}

	protected boolean enableExecution(Joblet joblet) {
		log.trace("enableExecution()");
		String s = joblet.getParameters().get("execute");
		if ((s == null) || (Boolean.parseBoolean(s)))
			return true;
		else
			return false;
	}

	private static class CacheEntry {
		long time = System.currentTimeMillis();

		SimpleJoblet joblet;

		CacheEntry(SimpleJoblet joblet) {
			this.joblet = joblet;
		}
	}
}
