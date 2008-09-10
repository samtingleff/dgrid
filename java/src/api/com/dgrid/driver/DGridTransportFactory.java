package com.dgrid.driver;

import com.dgrid.service.DGridTransport;
import com.dgrid.transport.DGridDummyTransport;
import com.dgrid.transport.DGridHibernateTransport;
import com.dgrid.transport.DGridThriftTransport;

public class DGridTransportFactory {
	private static final String THRIFT = "thrift";

	private static final String HIBERNATE = "hibernate";

	private static final String DUMMY = "dummy";

	private DGridTransportFactory() {
	}

	public static DGridTransport getTransport(String type) {
		if (THRIFT.equals(type)) {
			return new DGridThriftTransport();
		} else if (HIBERNATE.equals(type)) {
			return new DGridHibernateTransport();
		} else if (DUMMY.equals(type)) {
			return new DGridDummyTransport();
		} else {
			throw new RuntimeException(String.format(
					"Unknown transport type %1$s", type));
		}
	}
}
