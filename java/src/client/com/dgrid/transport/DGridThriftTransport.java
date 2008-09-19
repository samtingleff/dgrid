package com.dgrid.transport;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Constants;
import com.dgrid.gen.Host;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.InvalidJobletId;
import com.dgrid.gen.Job;
import com.dgrid.gen.JobService;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.NoHostAvailable;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.service.DGridSyncJobService;
import com.dgrid.service.DGridTransport;
import com.dgrid.util.io.HostnameDiscovery;
import com.facebook.thrift.TException;
import com.facebook.thrift.protocol.TBinaryProtocol;
import com.facebook.thrift.protocol.TProtocol;
import com.facebook.thrift.transport.TSocket;
import com.facebook.thrift.transport.TTransport;
import com.facebook.thrift.transport.TTransportException;

public class DGridThriftTransport implements DGridTransport {
	public static final String NAME = "thrift";

	private static final int RETRIES = 3;

	private Log log = LogFactory.getLog(getClass());

	private DGridSyncJobService syncJobService;

	private String hostname;

	private String server = "localhost";

	private int port = Constants.DEFAULT_PORT;

	private String apiKey = "123";

	private int hostid = 0;

	public void setSyncJobService(DGridSyncJobService service) {
		this.syncJobService = service;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setEndpoint(String endpoint) {
		this.server = endpoint;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void init() {
		this.hostname = HostnameDiscovery.getHostname();
	}

	public Host getHostByName(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		log.trace("getHostByName()");
		TConnection conn = null;
		try {
			conn = connect();
			Host host = getHost(conn, hostname);
			return host;
		} catch (TException e) {
			log
					.error(String.format("TException in getHost(%1$s)",
							hostname), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public Host registerHost(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		log.trace("registerHost()");
		TConnection conn = null;
		try {
			conn = connect();
			Host host = conn.jobService.registerHost(apiKey, hostname);
			return host;
		} catch (TException e) {
			log.error(String.format("TException in registerHost(%1$s)",
					hostname), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public void setHostFacts(int hostid, Map<String, String> facts)
			throws TransportException, InvalidApiKey, InvalidHost {
		log.trace("setHostFacts()");
		TConnection conn = null;
		try {
			conn = connect();
			conn.jobService.setHostFacts(apiKey, hostid, facts);
		} catch (TException e) {
			log.error(
					String.format("TException in setHostFacts(%1$s)", hostid),
					e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public String getHostSetting(int hostid, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost {
		log.trace("getHostSetting()");
		TConnection conn = null;
		try {
			conn = connect();
			String value = conn.jobService.getHostSetting(apiKey, hostid, name,
					defaultValue);
			return value;
		} catch (TException e) {
			log.error(String.format("TException in getSetting(%1$s, %2$s)",
					name, defaultValue), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey {
		log.trace("getSetting()");
		TConnection conn = null;
		try {
			conn = connect();
			String value = conn.jobService.getSetting(apiKey, name,
					defaultValue);
			return value;
		} catch (TException e) {
			log.error(String.format("TException in getSetting(%1$s, %2$s)",
					name, defaultValue), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public void log(int jobletId, int jobletStatus, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		log.trace("log()");
		TConnection conn = null;
		try {
			conn = connect();
			conn.jobService.log(apiKey, this.getHostIdCached(conn), jobletId,
					jobletStatus, message);
		} catch (TException e) {
			log.error(String.format("TException in log(%1$s, %2$s)", jobletId,
					message), e);
			throw (new TransportException(e));
		} catch (InvalidHost e) {
			log.error(String.format("InvalidHost in log(%1$s, %2$s)", jobletId,
					message), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public JobletResult gridExecute(Joblet joblet, int retries)
			throws InvalidApiKey, TransportException, NoHostAvailable {
		log.trace("gridExecute()");
		TConnection conn = null;
		try {
			conn = connect();
			Host host = conn.jobService.getSyncJobServiceHost(apiKey);
			JobletResult result = null;
			Exception e = null;
			for (int i = 0; i < retries; ++i) {
				try {
					result = syncJobService.gridExecute(host.getHostname(),
							joblet);
					break;
				} catch (Exception e1) {
					log.warn("Exception in gridExecute()", e1);
					if (i < retries)
						log.warn("Retrying...");
					e = e1;
				}
			}
			if (result == null) {
				if (e == null)
					throw (new TransportException(
							String
									.format(
											"Max retries of %1$d exceeded, no valid result returned",
											retries)));
				else
					throw (new TransportException(e));
			}
			return result;
		} catch (TException e) {
			log.error("TException in execute()", e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public int submitJob(Job job) throws TransportException, InvalidApiKey {
		log.trace("submitJob()");
		TConnection conn = null;
		try {
			conn = connect();
			int jobid = conn.jobService.submitJob(apiKey, job);
			return jobid;
		} catch (TException e) {
			log.error("TException in submitJob()", e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public int submitJoblet(Joblet joblet, int jobId, int callbackType,
			String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId {
		log.trace("submitJoblet()");
		TConnection conn = null;
		try {
			conn = connect();
			int jobletId = conn.jobService.submitJoblet(apiKey, joblet, jobId,
					callbackType, callbackAddress, callbackContent);
			return jobletId;
		} catch (TException e) {
			log.error("TException in submitJoblet()", e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public void completeJoblet(int jobletId, JobletResult result,
			String logMessage) throws TransportException, InvalidApiKey,
			InvalidJobletId {
		log.trace("completeJoblet()");
		TConnection conn = null;
		try {
			conn = connect();
			conn.jobService.completeJoblet(apiKey, this.getHostIdCached(conn),
					jobletId, result, logMessage);
		} catch (TException e) {
			log.error("TException in completeJoblet()", e);
			throw (new TransportException(e));
		} catch (InvalidHost e) {
			log.error("InvalidHost in completeJoblet()", e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public void releaseJoblet(int jobletId) throws InvalidApiKey,
			TransportException, InvalidJobletId {
		log.trace("releaseJoblet()");
		TConnection conn = null;
		try {
			conn = connect();
			conn.jobService.releaseJoblet(apiKey, this.getHostIdCached(conn),
					jobletId);
		} catch (TException e) {
			log.error(String.format("TException in releaseJoblet(%1$d)",
					jobletId), e);
			throw (new TransportException(e));
		} catch (InvalidHost e) {
			log.error(String.format("InvalidHost in releaseJoblet(%1$d)",
					jobletId), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
			InvalidJobId {
		log.trace("getJob()");
		TConnection conn = null;
		try {
			conn = connect();
			Job job = conn.jobService.getJob(apiKey, jobId);
			return job;
		} catch (TException e) {
			log.error(String.format("TException in getJob(%1$d)", jobId), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public JobletResult getJobletResult(int jobletId)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		log.trace("getJobletResult()");
		TConnection conn = null;
		try {
			conn = connect();
			JobletResult result = conn.jobService.getJobletResult(apiKey,
					jobletId);
			return result;
		} catch (TException e) {
			log.error(String.format("TException in getJobletResult(%1$d)",
					jobletId), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public List<JobletResult> getResults(int jobId) throws TransportException,
			InvalidApiKey, InvalidJobId {
		log.trace("getResults()");
		TConnection conn = null;
		try {
			conn = connect();
			List<JobletResult> results = conn.jobService.getResults(apiKey,
					jobId);
			return results;
		} catch (TException e) {
			log
					.error(String.format("TException in getResults(%1$d)",
							jobId), e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public Joblet getWork() throws TransportException, InvalidApiKey,
			InvalidHost, NoWorkAvailable {
		log.trace("getWork()");
		Joblet joblet = null;
		TConnection conn = null;
		try {
			conn = connect();
			Host host = getHost(conn, this.hostname);
			joblet = conn.jobService.getWork(apiKey, host.getId());
			return joblet;
		} catch (TException e) {
			log.error("TException in getWork()", e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	public int getJobletQueueSize() throws TransportException, InvalidApiKey {
		log.trace("getJobletQueueSize()");
		TConnection conn = null;
		try {
			conn = connect();
			int size = conn.jobService.getJobletQueueSize(apiKey);
			return size;
		} catch (TException e) {
			log.error("TException in getJobletQueueSize()", e);
			throw (new TransportException(e));
		} finally {
			disconnect(conn);
		}
	}

	private int getHostIdCached(TConnection conn) throws InvalidApiKey,
			TException, InvalidHost {
		int retval = 0;
		if (this.hostid != 0)
			retval = this.hostid;
		else {
			Host h = getHost(conn, this.hostname);
			retval = h.getId();
		}
		return retval;
	}

	private Host getHost(TConnection conn, String hostname)
			throws InvalidApiKey, TException, InvalidHost {
		log.trace("getHost()");
		TTransportException e = null;
		for (int i = 0; i < RETRIES; ++i) {
			try {
				Host host = conn.jobService.getHost(apiKey, hostname);
				return host;
			} catch (TTransportException tte) {
				e = tte;
				if (i < RETRIES)
					conn = connect();
			}
		}
		throw e;
	}

	private TConnection connect() throws TTransportException {
		log.trace("connect()");
		TSocket socket = new TSocket(server, port);
		TProtocol protocol = new TBinaryProtocol(socket);
		JobService.Iface jobService = new JobService.Client(protocol);
		socket.open();
		return new TConnection(socket, jobService);
		/*
		 * TTransport transport = new TFramedTransport(socket); TProtocol
		 * protocol = new TBinaryProtocol(transport); JobService.Iface
		 * jobService = new JobService.Client(protocol); transport.open();
		 * return new TConnection(transport, jobService);
		 */
	}

	private void disconnect(TConnection connection) {
		log.trace("disconnect()");
		try {
			connection.transport.close();
		} catch (Exception e) {
			log.warn("Exception calling transport.close()", e);
		}
	}

	private static class TConnection {
		public TTransport transport;

		public JobService.Iface jobService;

		public TConnection(TTransport transport, JobService.Iface jobService) {
			this.transport = transport;
			this.jobService = jobService;
		}
	}

}
