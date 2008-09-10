package com.dgrid.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.service.DGridSystemsAdapter;
import com.dgrid.service.DGridSystemsAdapterFactory;

public class DGridSystemsAdapterFactoryImpl implements
		DGridSystemsAdapterFactory {
	private Log log = LogFactory.getLog(getClass());

	private DGridSystemsAdapter adapter;

	public void setSystemsAdapter(DGridSystemsAdapter adapter) {
		this.adapter = adapter;
	}

	public DGridSystemsAdapter getSystemsAdapter() {
		log.trace("getSystemsAdapter()");
		return adapter;
	}
}
