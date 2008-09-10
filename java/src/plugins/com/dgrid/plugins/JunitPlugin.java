package com.dgrid.plugins;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import junit.framework.TestCase;

import com.dgrid.api.JobletTypeHandler;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridPluginManager;
import com.dgrid.util.StackTraceUtil;

public class JunitPlugin extends BaseDGridPlugin implements JobletTypeHandler {

	private JUnitCore junitCore = new JUnitCore();

	public String getDescription() {
		return "Support for distributed junit test cases";
	}

	public void start() {
		log.trace("start()");
		DGridPluginManager pluginManager = (DGridPluginManager) super.pluginManager;
		pluginManager.setJobletTypeHandler("junit", this);
	}

	public void stop() {
		log.trace("stop()");
	}

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Throwable {
		log.trace("execute()");
		long start = System.currentTimeMillis();
		String className = joblet.getParameters().get("class");
		String methodName = joblet.getParameters().get("method");
		Class cls = Class.forName(className);
		JobletResult result = null;
		if (methodName != null) {
			result = executeTestMethod(cls, methodName, gridClient, joblet,
					start);
		} else if (inheritsFromTestCase(cls)) {
			result = executeJunit3TestCases(cls, gridClient, joblet, start);
		} else {
			result = executeJunit4TestCases(cls, gridClient, joblet, start);
		}
		return result;
	}

	private JobletResult executeJunit3TestCases(Class cls,
			DGridClient gridClient, Joblet joblet, long start)
			throws TransportException, InvalidApiKey, InvalidJobId {
		Method[] methods = cls.getMethods();
		for (Method method : methods) {
			if ((Modifier.isPublic(method.getModifiers()))
					&& (method.getReturnType().equals(Void.TYPE))) {
				String methodName = method.getName();
				if (methodName.startsWith("test")) {
					Map<String, String> params = new HashMap<String, String>(2);
					params.put("class", cls.getName());
					params.put("method", methodName);
					Joblet joblet2 = new Joblet(0, 0l, 0, 0, joblet
							.getSubmitter(), 1, "junit", methodName, params,
							"", JOB_STATUS.RECEIVED);
					gridClient.submitJoblet(joblet2, joblet.getJobId());
				}
			}
		}
		return new JobletResult(0, 0l, 0, (System.currentTimeMillis() - start),
				JOB_STATUS.COMPLETED, "", joblet);
	}

	private JobletResult executeJunit4TestCases(Class cls,
			DGridClient gridClient, Joblet joblet, long start) throws Exception {
		Method[] methods = cls.getDeclaredMethods();
		for (Method method : methods) {
			Annotation testAnnotation = method.getAnnotation(Test.class);
			if (testAnnotation != null) {
				String methodName = method.getName();
				Map<String, String> params = new HashMap<String, String>(2);
				params.put("class", cls.getName());
				params.put("method", methodName);
				Joblet joblet2 = new Joblet(0, 0l, 0, 0, joblet.getSubmitter(),
						1, "junit", methodName, params, "", JOB_STATUS.RECEIVED);
				gridClient.submitJoblet(joblet2, joblet.getJobId());
			}
		}
		return new JobletResult(0, 0l, 0, (System.currentTimeMillis() - start),
				JOB_STATUS.COMPLETED, "", joblet);
	}

	private JobletResult executeTestMethod(Class cls, String methodName,
			DGridClient gridClient, Joblet joblet, long start) throws Throwable {
		log.trace("executeTestMethod()");
		Method m = cls.getMethod(methodName, new Class[] {});
		Object instance = cls.newInstance();
		Request request = Request.method(cls, methodName);
		Result result = junitCore.run(request);
		boolean success = result.wasSuccessful();
		if (success) {
			return new JobletResult(0, 0l, 0,
					(System.currentTimeMillis() - start), JOB_STATUS.COMPLETED,
					"", joblet);
		} else {
			List<Failure> failures = result.getFailures();
			Failure failure = failures.get(0);
			return new JobletResult(0, 0l, 1,
					(System.currentTimeMillis() - start), JOB_STATUS.FAILED,
					StackTraceUtil.getStackTrace(failure.getException()),
					joblet);
		}
	}

	private boolean inheritsFromTestCase(Class cls) {
		Class superClass = cls.getSuperclass();
		if (superClass == null)
			return false;
		else {
			if (superClass.equals(TestCase.class))
				return true;
			else
				return inheritsFromTestCase(superClass);
		}
	}
}
