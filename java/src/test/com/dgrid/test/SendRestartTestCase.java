package com.dgrid.test;

import java.util.HashMap;
import java.util.Map;

import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;

public class SendRestartTestCase extends BaseTestCase {

	public void testRestart() throws Exception {
		Map<String, String> params = new HashMap<String, String>(0);

		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.AGENT_RESTART_JOBLET, "Shutdown agent", params, "",
				JOB_STATUS.RECEIVED);
		int jobletId = gridClient.submitHostJoblet(getHostname(), joblet, 0);
		super.doWork();
	}
}
