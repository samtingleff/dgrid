package com.dgrid.service.impl;

import java.io.IOException;

import com.dgrid.service.DGridSystemsAdapter;
import com.dgrid.util.Execute;

public class DGridSystemsAdapterLinux extends DGridSystemsAdapterGeneric
		implements DGridSystemsAdapter {
	public void restart() {
		log.trace("restart()");
		try {
			Execute.execute("sudo /sbin/reboot");
		} catch (IOException e) {
			log.error("IOException calling /sbin/reboot", e);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			log.error("InterruptedException calling /sbin/reboot", e);
			throw new RuntimeException(e);
		} finally {
		}
	}

	public void shutdown() {
		log.trace("shutdown()");
		try {
			Execute.execute("sudo /sbin/shutdown -h now");
		} catch (IOException e) {
			log.error("IOException calling /sbin/shutdown", e);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			log.error("InterruptedException calling /sbin/shutdown", e);
			throw new RuntimeException(e);
		} finally {
		}
	}

	public void halt() {
		log.trace("halt()");
		try {
			Execute.execute("sudo /sbin/halt");
		} catch (IOException e) {
			log.error("IOException calling /sbin/halt", e);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			log.error("InterruptedException calling /sbin/halt", e);
			throw new RuntimeException(e);
		} finally {
		}
	}

}
