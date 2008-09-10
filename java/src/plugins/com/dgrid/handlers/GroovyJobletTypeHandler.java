package com.dgrid.handlers;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.io.File;

import com.dgrid.api.GenericScriptTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.service.DGridClient;

public class GroovyJobletTypeHandler extends GenericScriptTypeHandler implements
		JobletTypeHandler {
	public static final String SCRIPT_PARAM = "script";

	private GroovyClassLoader loader;

	private GroovyShell shell = new GroovyShell();

	public GroovyJobletTypeHandler(File dir) {
		super(dir);
		loader = new GroovyClassLoader(getClass().getClassLoader());
	}

	@Override
	protected SimpleJoblet compile(String code) throws Exception {
		log.trace("compile()");
		Class clz = loader.parseClass(code);
		Object obj = clz.newInstance();
		if (obj instanceof SimpleJoblet) {
			return (SimpleJoblet) obj;
		} else {
			return new GroovyShellSimpleJoblet(shell, code);
		}
	}

	private static class GroovyShellSimpleJoblet implements SimpleJoblet {
		private GroovyShell shell;

		private String code;

		public GroovyShellSimpleJoblet(GroovyShell shell, String code) {
			this.shell = shell;
			this.code = code;
		}

		public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
				throws Exception {
			Object obj = shell.evaluate(code);
			String retval = (obj != null) ? obj.toString() : "";
			return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, retval);
		}

	}
}
