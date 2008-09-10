package com.dgrid.service.impl;

import com.dgrid.helpers.EC2Helper;
import com.dgrid.service.DGridSystemsAdapter;

public class DGridSystemsAdapterEC2 extends DGridSystemsAdapterLinux implements
		DGridSystemsAdapter {

	private EC2Helper ec2Helper;

	public void setEc2Helper(EC2Helper helper) {
		this.ec2Helper = helper;
	}

	public void restart() {
		log.trace("restart()");
		try {
			String instanceid = ec2Helper.getInstanceMetadata("instance-id");
			ec2Helper.rebootInstances(new String[] { instanceid });
		} catch (Exception e) {
			log.error("Exception in restart(), will try super.restart()", e);
			super.restart();
		}
	}

	public void shutdown() {
		log.trace("shutdown()");
		halt();
	}

	public void halt() {
		log.trace("halt()");
		try {
			String instanceid = ec2Helper.getInstanceMetadata("instance-id");
			ec2Helper.terminateInstances(new String[] { instanceid });
		} catch (Exception e) {
			log.error("Exception in halt(), will try super.halt()", e);
			super.halt();
		}
	}
}
