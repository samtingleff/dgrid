package com.dgrid.util;

import java.util.HashMap;
import java.util.Map;

import com.dgrid.gen.JOB_CALLBACK_TYPES;

public class ApiCallbackTypes {
	private static final Map<String, Integer> types;

	private static final Map<Integer, String> reverseTypes;
	static {
		types = new HashMap<String, Integer>();
		types.put("xmpp", JOB_CALLBACK_TYPES.XMPP);
		types.put("http", JOB_CALLBACK_TYPES.HTTP);
		types.put("email", JOB_CALLBACK_TYPES.EMAIL);
		types.put("job", JOB_CALLBACK_TYPES.JOB);
		types.put("none", JOB_CALLBACK_TYPES.NONE);

		reverseTypes = new HashMap<Integer, String>();
		reverseTypes.put(new Integer(0), "none");
		reverseTypes.put(new Integer(JOB_CALLBACK_TYPES.XMPP), "xmpp");
		reverseTypes.put(new Integer(JOB_CALLBACK_TYPES.HTTP), "http");
		reverseTypes.put(new Integer(JOB_CALLBACK_TYPES.EMAIL), "email");
		reverseTypes.put(new Integer(JOB_CALLBACK_TYPES.JOB), "job");
		reverseTypes.put(new Integer(JOB_CALLBACK_TYPES.NONE), "none");
	}

	public static int getCallbackType(String type) {
		try {
			Integer t = types.get(type);
			if (t == null)
				return JOB_CALLBACK_TYPES.NONE;
			else
				return t.intValue();
		} catch (Exception e) {
			return JOB_CALLBACK_TYPES.NONE;
		}
	}

	public static String getStringCallbackType(int type) {
		String value = reverseTypes.get(new Integer(type));
		if (value == null)
			value = "none";
		return value;
	}
}
