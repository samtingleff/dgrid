package com.dgrid.service.impl;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.dgrid.service.XmppConnection;

public class SmackXMPPConnectionImpl implements XmppConnection {
	private Log log = LogFactory.getLog(getClass());

	private String server;

	private String serviceName;

	private int port = 5222;

	private String username;

	private String password;

	private XMPPConnection conn;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#setPort(int)
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public String getServer() {
		return server;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#setServer(java.lang.String)
	 */
	public void setServer(String server) {
		this.server = server;
	}

	public String getServiceName() {
		return serviceName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#setServiceName(java.lang.String)
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getUsername() {
		return username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#connect()
	 */
	public void connect() throws XMPPException {
		log.trace("connect()");
		ConnectionConfiguration conf = new ConnectionConfiguration(server,
				port, serviceName);
		conn = new XMPPConnection(conf);
		conn.connect();
		conn.login(username, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#createAccount()
	 */
	public void createAccount() throws XMPPException {
		log.trace("createAccount()");
		ConnectionConfiguration conf = new ConnectionConfiguration(server,
				port, serviceName);
		conn = new XMPPConnection(conf);
		conn.connect();
		AccountManager mgr = conn.getAccountManager();
		mgr.createAccount(username, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#disconnect()
	 */
	public void disconnect() {
		log.trace("disconnect()");
		conn.disconnect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#getJid()
	 */
	public String getJid() {
		if ((conn == null) || (conn.isConnected() == false))
			return null;
		else {
			String user = conn.getUser();
			return user.substring(0, user.indexOf((int) '/'));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#sendMessage(org.jivesoftware.smack.packet.Message)
	 */
	public void sendMessage(Message msg) {
		this.sendPacket(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#sendPacket(org.jivesoftware.smack.packet.Packet)
	 */
	public void sendPacket(Packet packet) {
		try {
			conn.sendPacket(packet);
		} catch (IllegalStateException e) {
			// thrown when disconnected
			try {
				conn.connect();
				conn.sendPacket(packet);
			} catch (XMPPException e1) {
				log.warn("Could not reconnect inside sendPacket()", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#sendTextMessage(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendTextMessage(String to, String subject, String body) {
		Message msg = new Message(to, Message.Type.normal);
		msg.setSubject(subject);
		msg.setBody(body);
		this.sendMessage(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#addPacketListener(org.jivesoftware.smack.PacketListener)
	 */
	public void addPacketListener(PacketListener listener) {
		conn.addPacketListener(listener, new MessageTypeFilter(
				Message.Type.normal));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#addPacketListener(org.jivesoftware.smack.PacketListener,
	 *      org.jivesoftware.smack.filter.PacketFilter)
	 */
	public void addPacketListener(PacketListener listener, PacketFilter filter) {
		conn.addPacketListener(listener, filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#createRoom(java.lang.String,
	 *      java.lang.String)
	 */
	public MultiUserChat createRoom(String name, String nickname)
			throws XMPPException {
		log.trace("createRoom()");
		try {
			MultiUserChat muc = new MultiUserChat(conn, name + "@conference."
					+ server);
			muc.create(nickname);
			Form form = muc.getConfigurationForm();
			Form submitForm = form.createAnswerForm();
			for (Iterator<FormField> fields = form.getFields(); fields
					.hasNext();) {
				FormField field = fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// Sets the default value as the answer
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			// this is the default - change to true for persistent room
			submitForm.setAnswer("muc#roomconfig_persistentroom", false);
			muc.sendConfigurationForm(submitForm);
			// or just use this line
			// muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
			return muc;
		} catch (XMPPException e) {
			log
					.info("XMPPException creating room. The room may exist, calling joinRoom() instead...");
			MultiUserChat muc = joinRoom(name, nickname);
			return muc;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#joinRoom(java.lang.String,
	 *      java.lang.String)
	 */
	public MultiUserChat joinRoom(String room, String nickname)
			throws XMPPException {
		log.trace("joinRoom()");
		MultiUserChat muc = new MultiUserChat(conn, room + "@conference."
				+ server);
		DiscussionHistory history = new DiscussionHistory();
		history.setMaxChars(0);
		muc.join(nickname, null, history, 5000);
		// muc.join(nickname);
		return muc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#getSmackXMPPConnection()
	 */

	public XMPPConnection getSmackXMPPConnection() {
		return conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#getRoster()
	 */
	public Roster getRoster() {
		return conn.getRoster();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dgrid.xmpp.XmppConnection#addRosterListener(org.jivesoftware.smack.RosterListener)
	 */
	public void addRosterListener(RosterListener listener) {
		conn.getRoster().addRosterListener(listener);
	}

	public void finalize() throws Throwable {
		log.trace("finalize()");
		this.disconnect();
	}
}
