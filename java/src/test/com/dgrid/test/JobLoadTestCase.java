package com.dgrid.test;

import java.util.HashMap;
import java.util.Map;

import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;

public class JobLoadTestCase extends BaseTestCase {

	public void testSendLotsOfJoblets() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "XmppSend.groovy");
		params.put("to", "sam@jabber.samnbree.net");
		for (int i = 0; i < 10; ++i) {
			Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1, "groovy",
					"Send xmpp msg", params, String.format("Hi Sam! (# %1$d)",
							i), JOB_STATUS.RECEIVED);
			gridClient.submitJoblet(joblet, 0);
		}
		for (int i = 0; i < 10; ++i) {
			super.doWork();
		}
	}
}
