package com.dgrid.handlers;

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import com.dgrid.api.GenericScriptTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.service.DGridClient;

public class JavascriptJobletTypeHandler extends GenericScriptTypeHandler
		implements JobletTypeHandler {

	public JavascriptJobletTypeHandler(File root) {
		super(root);
	}

	@Override
	protected SimpleJoblet compile(String code) throws Exception {
		log.trace("execute()");
		return new JavascriptSimpleJoblet(code);
	}

	private static class JavascriptSimpleJoblet implements SimpleJoblet {
		private String code;

		private JavascriptSimpleJoblet(String code) {
			this.code = code;
		}

		public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
				throws Exception {
			Context cx = ContextFactory.getGlobal().enterContext();
			Scriptable scope = cx.initStandardObjects();
			Object evalReturn = cx.evaluateString(scope, code, "", 1, null);
			Object fObj = scope.get("execute", scope);
			SimpleJobletResult sjr = null;
			if (!(fObj instanceof Function)) {
				String retval = (evalReturn != null) ? evalReturn.toString()
						: "";
				sjr = new SimpleJobletResult(0, JOB_STATUS.COMPLETED, retval);
			} else {
				Object wrappedJoblet = Context.javaToJS(joblet, scope);
				Object wrappedGridClient = Context.javaToJS(gridClient, scope);
				Object[] functionArgs = { wrappedJoblet, wrappedGridClient };
				Function f = (Function) fObj;
				Object result = f.call(cx, scope, scope, functionArgs);
				sjr = (SimpleJobletResult) Context.jsToJava(result,
						SimpleJobletResult.class);
			}
			return sjr;

		}
	}
}
