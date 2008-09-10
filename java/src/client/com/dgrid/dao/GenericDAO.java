package com.dgrid.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericDAO {
	public static final String NAME = "genericDAO";

	public Object create(Object object);

	public Object read(Class cls, Serializable id);

	public Object update(Object object);

	public Object delete(Class cls, Serializable id);

	public List list(Class cls, int offset, int max, String orderProperty,
			boolean asc);
}