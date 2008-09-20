package com.dgrid.dao.hibernate;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.dgrid.dao.GenericDAO;

public class GenericHibernateDAO extends HibernateDaoSupport implements
		GenericDAO {

	private Log log = LogFactory.getLog(getClass());

	public Object create(Object object) {
		log.trace("create()");
		Session session = super.getSession();
		session.save(object);
		return (object);
	}

	public Object read(Class cls, Serializable id)
			throws ObjectRetrievalFailureException {
		log.trace("read()");
		Session session = super.getSession();
		Object object = session.get(cls, id);
		if (object == null) {
			throw (new ObjectRetrievalFailureException(cls, id));
		} else {
			return (object);
		}
	}

	public Object update(Object object) {
		log.trace("update()");
		Session session = super.getSession();
		session.update(object);
		return (object);
	}

	public Object delete(Class cls, Serializable id)
			throws ObjectRetrievalFailureException {
		log.trace("delete()");
		Session session = super.getSession();
		Object object = session.get(cls, id);
		if (object == null) {
			throw (new ObjectRetrievalFailureException(cls, id));
		} else {
			session.delete(object);
			return (object);
		}
	}

	public int count(Class cls) {
		log.trace("count()");
		Session session = super.getSession();
		Criteria crit = session.createCriteria(cls);
		crit.setProjection(Projections.rowCount());
		int rows = ((Integer) crit.uniqueResult()).intValue();
		return rows;
	}

	public List list(Class cls, int offset, int max, String orderProperty,
			boolean asc) {
		log.trace("list()");
		Session session = super.getSession();
		Criteria crit = session.createCriteria(cls);
		if (offset > 0)
			crit.setFirstResult(offset);
		if (max > 0)
			crit.setMaxResults(max);
		if (orderProperty != null) {
			if (asc)
				crit.addOrder(Order.asc(orderProperty));
			else
				crit.addOrder(Order.desc(orderProperty));
		}
		List results = crit.list();
		return (results);
	}
}
