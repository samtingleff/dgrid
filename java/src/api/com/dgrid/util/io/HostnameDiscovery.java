package com.dgrid.util.io;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameDiscovery {

	public static String getHostname() {
		try {
			InetAddress localMachine = InetAddress.getLocalHost();
			String hostname = localMachine.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			throw (new RuntimeException(e));
		}
	}

	public static InetAddress getInetAddress() {
		try {
			InetAddress local = InetAddress.getLocalHost();
			return local;
		} catch (UnknownHostException e) {
			throw (new RuntimeException(e));
		}
	}
}
