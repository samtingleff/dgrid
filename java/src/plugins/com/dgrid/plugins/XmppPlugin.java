package com.dgrid.plugins;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Host;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.plugin.Plugin;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridPluginContext;
import com.dgrid.service.DGridSystemsAdapter;
import com.dgrid.service.DGridSystemsAdapterFactory;
import com.dgrid.service.XmppConnection;
import com.dgrid.threads.DGridTaskListener;
import com.dgrid.util.RandomCodeGenerator;

public class XmppPlugin extends BaseDGridPlugin implements Plugin,
		DGridTaskListener {

	private XmppConnection xmpp;

	private DGridClient gridClient;

	private boolean verbose = true;

	public String getDescription() {
		return "Adds xmmp/jabber capabilities";
	}

	public boolean enabledByDefault() {
		return false;
	}

	public void init() {
		log.trace("init()");
		doInit();
	}

	public void start() {
		log.trace("start()");
		try {
			doConnect();
		} catch (XMPPException e) {
			log
					.error(
							"XMPPException in start(), will try to create account",
							e);
			try {
				xmpp.createAccount();
				doConnect();
			} catch (XMPPException e1) {
				log
						.error(
								"XMPPException calling createAccount(), xmpp plugin will be disabled",
								e1);
				((DGridPluginContext) super.context)
						.removeExecutionListener(this);
			}
		}
	}

	public void stop() {
		log.trace("stop()");
		xmpp.disconnect();
		((DGridPluginContext) super.context).removeExecutionListener(this);
	}

	public String getJid() {
		return xmpp.getJid();
	}

	public void sendTextMessage(String to, String subject, String body) {
		xmpp.sendTextMessage(to, subject, body);
	}

	public XmppConnection getXmppConnection() {
		return xmpp;
	}

	public void beforeExecute(Thread t, Runnable r, int activeThreadCount,
			int maxThreadCount) {
		log.trace("beforeExecute()");
		Presence.Mode mode = (activeThreadCount >= maxThreadCount) ? Presence.Mode.dnd
				: Presence.Mode.available;
		updatePresence(Presence.Type.available, mode, String.format(
				"Working: (%1$d/%2$d: %3$.3f)", activeThreadCount,
				maxThreadCount, getSystemLoadAverage()));
	}

	public void afterExecute(Runnable r, Throwable t, int activeThreadCount,
			int maxThreadCount) {
		log.trace("afterExecute()");
		updatePresence(Presence.Type.available, Presence.Mode.available, String
				.format("Completed task: (%1$d/%2$d: %3$.3f)",
						activeThreadCount, maxThreadCount,
						getSystemLoadAverage()));
	}

	public void updatePresence(Presence.Type type, Presence.Mode mode,
			String status) {
		if (verbose) {
			Presence p = new Presence(type);
			p.setMode(mode);
			p.setStatus(status);
			xmpp.sendPacket(p);
		}
	}

	private double getSystemLoadAverage() {
		log.trace("getSystemLoadAverage()");
		DGridSystemsAdapterFactory factory = (DGridSystemsAdapterFactory) super.context
				.getBean(DGridSystemsAdapterFactory.NAME);
		DGridSystemsAdapter adapter = factory.getSystemsAdapter();
		return adapter.getSystemLoadAverage();
	}

	private void doInit() {
		log.trace("doInit()");
		DGridPluginContext dgridContext = (DGridPluginContext) super.context;
		gridClient = dgridContext.getGridClient();
		try {
			Host host = gridClient.getHost();
			xmpp = (XmppConnection) dgridContext.getBean(XmppConnection.NAME);
			xmpp.setServer(getSetting("xmpp.server.name", "localhost"));
			xmpp.setServiceName(getSetting("xmpp.server.serviceName",
					"localhost"));
			xmpp.setPort(Integer
					.parseInt(getSetting("xmpp.server.port", "5222")));
			xmpp.setUsername(getHostSetting(host, "xmpp.username", host
					.getHostname()));
			xmpp.setPassword(getHostSetting(host, "xmpp.password",
					RandomCodeGenerator.getLetterCode(10)));
			verbose = Boolean.parseBoolean(getSetting("xmpp.client.verbose",
					Boolean.toString(true)));
		} catch (Exception e) {
			log.warn("Exception in init(), xmpp plugin will be disabled", e);
		}
	}

	private void doConnect() throws XMPPException {
		log.trace("doConnect()");
		xmpp.connect();
		((DGridPluginContext) super.context).addExecutionListener(this);
		try {
			setHostFacts();
		} catch (Exception e) {
			log.warn("Exception calling setHostFacts()", e);
		}
	}

	private void setHostFacts() throws TransportException, InvalidApiKey,
			InvalidHost {
		Host host = gridClient.getHost();
		Map<String, String> facts = new HashMap<String, String>(1);
		facts.put("xmpp.jid", getJid());
		gridClient.setHostFacts(host.getId(), facts);
	}

	private String getHostSetting(Host host, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost {
		log.trace("getHostSetting()");
		return gridClient.getHostSetting(host.getId(), name, defaultValue);
	}

	private String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey {
		log.trace("getSetting()");
		return gridClient.getSetting(name, defaultValue);
	}
}
