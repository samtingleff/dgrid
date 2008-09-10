package com.dgrid.handlers;

import java.lang.reflect.Method;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

public class JavaAppJobletTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	public static final String CLASS_NAME_PARAM = "class";

	public static final String METHOD_NAME_PARAM = "method";

	public static final String STATIC_PARAM = "static";

	private static final Class[] defaultMethodArgClasses = new Class[] {};

	private static final Class[] mainMethodArgClasses = new Class[] { String[].class };

	private static final Object[] defaultMethodArgs = new Object[] {};

	private static final Object[] mainMethodArgs = new Object[] { new String[] {} };

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		log.trace("execute()");
		long start = System.currentTimeMillis();
		String className = joblet.getParameters().get(CLASS_NAME_PARAM);
		String methodName = joblet.getParameters().get(METHOD_NAME_PARAM);
		boolean isStatic = Boolean.parseBoolean(joblet.getParameters().get(
				STATIC_PARAM));
		Class[] methodArgClasses = null;
		Object[] methodArgs = null;

		if (("main".equals(methodName)) && (isStatic == true)) {
			methodArgClasses = mainMethodArgClasses;
			methodArgs = mainMethodArgs;
		} else {
			methodArgClasses = defaultMethodArgClasses;
			methodArgs = defaultMethodArgs;
		}
		Class cls = Class.forName(className);
		Method method = cls.getMethod(methodName, methodArgClasses);
		Object instance = null;
		if (!isStatic)
			instance = cls.newInstance();

		Object retval = method.invoke(instance, methodArgs);
		JobletResult result = null;
		String s = (retval != null) ? retval.toString() : "";
		result = new JobletResult(0, 0l, 0,
				(System.currentTimeMillis() - start), JOB_STATUS.COMPLETED, s,
				joblet);
		return result;
	}

}
