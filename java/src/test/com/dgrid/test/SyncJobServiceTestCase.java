package com.dgrid.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.dgrid.gen.Constants;
import com.dgrid.gen.HostState;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.SyncJobService;
import com.dgrid.util.io.HostnameDiscovery;
import com.facebook.thrift.protocol.TBinaryProtocol;
import com.facebook.thrift.protocol.TProtocol;
import com.facebook.thrift.transport.TSocket;
import com.facebook.thrift.transport.TTransport;

public class SyncJobServiceTestCase extends TestCase {

	private String apiKey = "123";

	private TTransport transport;

	private SyncJobService.Client syncJobService;

	public void setUp() throws Exception {
		// cannot get server to work with TFramedTransport
		// TSocket socket = new TSocket("localhost",
		// Constants.SYNC_DEFAULT_PORT);
		// this.transport = new TFramedTransport(socket);
		this.transport = new TSocket("localhost", Constants.SYNC_DEFAULT_PORT);
		TProtocol protocol = new TBinaryProtocol(transport);
		this.syncJobService = new SyncJobService.Client(protocol);
		transport.open();

	}

	public void tearDown() {
		transport.close();
	}

	public void testStatus() throws Exception {
		HostState state = syncJobService.status(apiKey);
		assertNotNull(state);
	}

	public void testExecute() throws Exception {
		Map<String, String> emptyMap = new HashMap<String, String>(0);
		Joblet joblet = new Joblet(0, 0l, 0, 0,
				HostnameDiscovery.getHostname(), 1, Constants.SYSTEM_JOBLET,
				"Test", emptyMap, "echo -n hello", JOB_STATUS.RECEIVED);
		JobletResult result = syncJobService.execute(apiKey, joblet);
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
	}
}
