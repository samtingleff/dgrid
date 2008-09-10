package com.dgrid.test;

import java.util.List;

import com.dgrid.gen.JOB_CALLBACK_TYPES;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.helpers.JobSerializer;
import com.dgrid.util.io.InputStreamUtils;

public class SQSInputBridgeTestCase extends BaseTestCase {

	public void testXmlConverter() throws Exception {
		String xml = InputStreamUtils.getInputStreamAsString(getClass()
				.getResourceAsStream("/com/dgrid/test/resources/test-job.xml"));
		JobSerializer xmlSerializer = (JobSerializer) super
				.getBean(JobSerializer.XML);
		Job job = xmlSerializer.deserialize(xml);
		assertEquals(job.getDescription(), "test joblet");
		assertEquals(job.getCallbackType(), JOB_CALLBACK_TYPES.EMAIL);
		List<Joblet> joblets = job.getJoblets();
		assertEquals(joblets.size(), 1);
		Joblet joblet = joblets.get(0);
		assertEquals(joblet.getPriority(), 1);
	}
}
