package com.dgrid.helpers;

import com.dgrid.errors.SDBException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.xerox.amazonws.sdb.SimpleDB;

public interface SDBHelper
{
	public static final String NAME = "sdbHelper";

	public SimpleDB getSimpleDB() throws TransportException, InvalidApiKey, SDBException;;
}
