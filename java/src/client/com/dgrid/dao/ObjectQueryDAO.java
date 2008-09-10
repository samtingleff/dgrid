package com.dgrid.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;

public interface ObjectQueryDAO {
	public static final String NAME = "objectQueryDAO";

	public Criteria createCriteria(Class cls);

	public Query createQuery(String hql);
}
