package com.dgrid.helpers.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.SDBException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.helpers.AWSConstants;
import com.dgrid.helpers.SDBHelper;
import com.dgrid.service.DGridClient;
import com.xerox.amazonws.sdb.SimpleDB;

public class SDBHelperImpl implements SDBHelper
{
	private Log log = LogFactory.getLog(getClass());

	private DGridClient gridClient;

	public void setGridClient(DGridClient gridClient)
	{
		this.gridClient = gridClient;
	}

	public SimpleDB getSimpleDB() throws TransportException, InvalidApiKey, SDBException
	{
		log.trace("getSimpleDB()");
		String awsAccessKey = gridClient.getSetting(AWSConstants.AWS_ACCESS_KEY_SETTING, "");
		String awsSecretKey = gridClient.getSetting(AWSConstants.AWS_SECRET_KEY_SETTING, "");
		return new SimpleDB(awsAccessKey, awsSecretKey);
	}

}
