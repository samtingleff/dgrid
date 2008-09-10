package com.dgrid.service;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

public interface XmppConnection {
	public static final String NAME = "xmppConnection";

	public void setPassword(String password);

	public int getPort();

	public void setPort(int port);

	public String getServer();

	public void setServer(String server);

	public String getServiceName();

	public void setServiceName(String serviceName);

	public String getUsername();

	public void setUsername(String username);

	public void connect() throws XMPPException;

	public void createAccount() throws XMPPException;

	public void disconnect();

	public String getJid();

	public void sendTextMessage(String to, String subject, String body);

	public void sendMessage(Message msg);

	public void sendPacket(Packet packet);

	public MultiUserChat createRoom(String name, String nickname)
			throws XMPPException;

	public MultiUserChat joinRoom(String room, String nickname)
			throws XMPPException;

	public void addPacketListener(PacketListener listener);

	public void addPacketListener(PacketListener listener, PacketFilter filter);

	public XMPPConnection getSmackXMPPConnection();

	public Roster getRoster();

	public void addRosterListener(RosterListener listener);

}