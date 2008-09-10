package com.dgrid.helpers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.dgrid.errors.EC2Exception;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.helpers.AWSConstants.EC2InstanceType;
import com.xerox.amazonws.ec2.Jec2;

public interface EC2Helper {
	public static final String NAME = "ec2Helper";

	public String getInstanceUserdata() throws IOException;

	public Map<String, String> getInstanceMetadata() throws IOException;

	public String getInstanceMetadata(String name) throws IOException;

	public void runInstances(String imageId, int minCount, int maxCount,
			List<String> groupSet, String userData, String keyName,
			EC2InstanceType type) throws TransportException, InvalidApiKey,
			EC2Exception;

	public void rebootInstances(String[] instanceIds)
			throws TransportException, InvalidApiKey, EC2Exception;

	public void terminateInstances(String[] instanceIds)
			throws TransportException, InvalidApiKey, EC2Exception;

	public Jec2 getJec2() throws TransportException, InvalidApiKey;
}
