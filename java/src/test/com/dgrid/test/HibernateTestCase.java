package com.dgrid.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dgrid.dao.GenericDAO;
import com.dgrid.gen.Host;
import com.dgrid.gen.JOB_CALLBACK_TYPES;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;

public class HibernateTestCase extends BaseTestCase {

	public void testHost() throws Exception {
		GenericDAO dao = (GenericDAO) super.getBean(GenericDAO.NAME);
		Host host = new Host();
		host.setHostname("host1");
		host = (Host) dao.create(host);
		assertTrue(host.getId() > 0);

		Map<String, String> params1 = new HashMap<String, String>();
		params1.put("testkey", "testvalue");
		params1.put("testkey2", "testvaluebeatch");
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("testkey", "testvalue2");
		Joblet j1 = new Joblet(0, 0l, 0, host.getId(), "testuser", 1, "test",
				"description", params1, "this is content", 0);
		Joblet j2 = new Joblet(0, 0l, 0, host.getId(), "testuser", 1, "test",
				"description", params2, "this is content (2)", 0);
		j1 = (Joblet) dao.create(j1);
		j2 = (Joblet) dao.create(j2);

		List<Joblet> joblets = new LinkedList<Joblet>();
		joblets.add(j1);
		joblets.add(j2);
		Job job = new Job(0, 0l, "testuser", "job desc.", joblets,
				JOB_CALLBACK_TYPES.NONE, null, null, JOB_STATUS.RECEIVED);
		job = (Job) dao.create(job);
		j1 = (Joblet) dao.read(Joblet.class, j1.getId());
		j2 = (Joblet) dao.read(Joblet.class, j2.getId());

		j2.setStatus(JOB_STATUS.COMPLETED);
		JobletResult jr = new JobletResult(0, 0l, 0, 100, JOB_STATUS.COMPLETED,
				null, j2);
		dao.update(j2);
		dao.create(jr);

		assertNotNull(j1);
		assertEquals(j1.getParametersSize(), 2);
		assertEquals(j1.getParameters().get("testkey"), "testvalue");
		assertEquals(j1.getParameters().get("testkey2"), "testvaluebeatch");
		assertEquals(j2.getParametersSize(), 1);
		assertEquals(j2.getParameters().get("testkey"), "testvalue2");
		dao.delete(Host.class, host.getId());
		dao.delete(JobletResult.class, jr.getId());
		dao.delete(Joblet.class, j1.getId());
		dao.delete(Joblet.class, j2.getId());
		dao.delete(Job.class, job.getId());
	}
}
