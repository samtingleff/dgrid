package com.dgrid.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.dgrid.dao.ObjectQueryDAO;

public class ObjectQueryHibernateDAO extends HibernateDaoSupport implements
		ObjectQueryDAO {

	public Criteria createCriteria(Class cls) {
		Session session = super.getSession();
		Criteria crit = session.createCriteria(cls);
		return (crit);
	}

	public Query createQuery(String hql) {
		Session session = super.getSession();
		Query query = session.createQuery(hql);
		return (query);
	}

}
