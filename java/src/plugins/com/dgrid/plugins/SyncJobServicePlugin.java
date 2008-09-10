package com.dgrid.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.gen.Constants;
import com.dgrid.gen.HostState;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.SyncJobService;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.plugin.Plugin;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridExecutorService;
import com.dgrid.service.DGridPluginManager;
import com.dgrid.service.DGridSystemsAdapter;
import com.dgrid.service.DGridSystemsAdapterFactory;
import com.facebook.thrift.TException;
import com.facebook.thrift.protocol.TBinaryProtocol;
import com.facebook.thrift.protocol.TProtocolFactory;
import com.facebook.thrift.server.TServer;
import com.facebook.thrift.server.TThreadPoolServer;
import com.facebook.thrift.transport.TServerSocket;
import com.facebook.thrift.transport.TServerTransport;
import com.facebook.thrift.transport.TTransportException;
import com.facebook.thrift.transport.TTransportFactory;

public class SyncJobServicePlugin extends BaseDGridPlugin implements Plugin {

	private TServer server;

	public String getDescription() {
		return "Support for synchronous map-reduce style jobs";
	}

	public void start() {
		log.trace("start()");
		try {
			DGridClient gridClient = (DGridClient) super.context
					.getBean(DGridClient.NAME);
			String apiKey = ((DGridPluginManager) super.pluginManager)
					.getProperty("transport.apiKey");
			SyncJobServiceHandler handler = new SyncJobServiceHandler(
					gridClient, apiKey);
			SyncJobService.Processor processor = new SyncJobService.Processor(
					handler);

			TServerTransport serverTransport = new TServerSocket(
					Constants.SYNC_DEFAULT_PORT);
			TTransportFactory tfactory = new TTransportFactory();
			TProtocolFactory pfactory = new TBinaryProtocol.Factory();
			this.server = new TThreadPoolServer(processor, serverTransport,
					tfactory, pfactory);
			// server = new TSimpleServer(processor, serverTransport);
			Thread t = new Thread(new Runnable() {
				public void run() {
					server.serve();
				}
			}, "SyncJobService");
			t.start();
		} catch (TTransportException e) {
			log
					.error(
							"TTransportException in start(). sync job service will be disabled",
							e);
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		log.trace("stop()");
		try {
			server.stop();
		} catch (NullPointerException e) {
		}
	}

	private static class SyncJobServiceHandler implements SyncJobService.Iface {
		private Log log = LogFactory.getLog(getClass());

		private DGridClient gridClient;

		private String apiKey;

		SyncJobServiceHandler(DGridClient gridClient, String apiKey) {
			this.gridClient = gridClient;
			this.apiKey = apiKey;
		}

		public HostState status(String apiKey) throws TException, InvalidApiKey {
			log.trace("status()");
			validateApiKey(apiKey);
			DGridSystemsAdapterFactory factory = (DGridSystemsAdapterFactory) gridClient
					.getBean(DGridSystemsAdapterFactory.NAME);
			DGridSystemsAdapter systemsAdapter = factory.getSystemsAdapter();
			DGridExecutorService executor = (DGridExecutorService) gridClient
					.getBean(DGridExecutorService.NAME);
			HostState state = new HostState(systemsAdapter.getVmUptime(),
					systemsAdapter.getSystemLoadAverage(), systemsAdapter
							.getFreeMemory(), systemsAdapter.getUsedMemory(),
					systemsAdapter.getActiveThreadCount(), executor
							.getActiveCount());
			return state;
		}

		public JobletResult execute(String apiKey, Joblet joblet)
				throws InvalidApiKey, TException {
			log.trace("execute()");
			validateApiKey(apiKey);
			return gridClient.execute(joblet);
		}

		private void validateApiKey(String apiKey) throws InvalidApiKey {
			log.trace("validateApiKey()");
			if (!this.apiKey.equals(apiKey))
				throw new InvalidApiKey();
		}
	}
}
