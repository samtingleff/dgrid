package com.dgrid.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.gen.Constants;
import com.dgrid.gen.HostState;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.SyncJobService;
import com.dgrid.service.DGridSyncJobService;
import com.facebook.thrift.TException;
import com.facebook.thrift.protocol.TBinaryProtocol;
import com.facebook.thrift.protocol.TProtocol;
import com.facebook.thrift.transport.TSocket;
import com.facebook.thrift.transport.TTransport;
import com.facebook.thrift.transport.TTransportException;

public class DGridSyncJobServiceImpl implements DGridSyncJobService {
	private Log log = LogFactory.getLog(getClass());

	private String apiKey;

	public DGridSyncJobServiceImpl() {
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.service.impl.DGridSyncJobService#gridExecute(
	 *      java.lang.String, com.dgrid.gen.Joblet)
	 */
	public JobletResult gridExecute(String hostname, Joblet joblet)
			throws InvalidApiKey, TException {
		log.trace("gridExecute()");
		TConnection conn = null;
		try {
			conn = connect(hostname);
			JobletResult result = conn.service.execute(apiKey, joblet);
			return result;
		} finally {
			disconnect(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.service.impl.DGridSyncJobService#status(java.lang.String)
	 */
	public HostState status(String hostname) throws InvalidApiKey, TException {
		log.trace("status()");
		TConnection conn = null;
		try {
			conn = connect(hostname);
			HostState state = conn.service.status(apiKey);
			return state;
		} finally {
			disconnect(conn);
		}
	}

	private TConnection connect(String hostname) throws TTransportException {
		log.trace("connect()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("Connecting to %1$s", hostname));
		}
		TTransport transport = new TSocket(hostname,
				Constants.SYNC_DEFAULT_PORT);
		TProtocol protocol = new TBinaryProtocol(transport);
		SyncJobService.Iface syncJobService = new SyncJobService.Client(
				protocol);
		TConnection conn = new TConnection(transport, syncJobService);
		transport.open();
		return conn;
	}

	private void disconnect(TConnection conn) {
		log.trace("disconnect()");
		try {
			if (conn != null)
				conn.transport.close();
		} catch (Exception e) {
			log.warn("Exception calling transport.close()", e);
		}
	}

	private static class TConnection {
		TTransport transport;

		SyncJobService.Iface service;

		TConnection(TTransport transport, SyncJobService.Iface service) {
			this.transport = transport;
			this.service = service;
		}
	}
}
