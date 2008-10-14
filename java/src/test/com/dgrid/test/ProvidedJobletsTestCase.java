package com.dgrid.test;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.util.io.InputStreamUtils;
import com.dgrid.util.io.OutputStreamUtils;

public class ProvidedJobletsTestCase extends BaseTestCase {

	public void testS3Joblets() throws Exception {
		String bucket = String.format("0a9gvwm7w60mmfqyqp82-test");
		String key1 = "test_file.txt";
		String key2 = "/prefix/test_file.txt";
		String content = String.format("Test Content beatch (%1$d)", System
				.currentTimeMillis());
		File file = File.createTempFile("s3test", ".txt");
		File file2 = File.createTempFile("s3test2", ".txt");
		file2.deleteOnExit();
		OutputStreamUtils.writeStringToFile(content, file);
		OutputStreamUtils.writeStringToFile(content, file2);

		// put some content
		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "S3Put.groovy");
		params.put("bucket", bucket);
		params.put("key", key1);
		params.put("contentType", "text/plain");
		params.put("public", Boolean.toString(true));
		params.put("file", file.getAbsolutePath());
		params.put("delete", Boolean.toString(true));
		Joblet joblet1 = new Joblet(0, 0l, 0, 0, getHostname(), 1, "groovy",
				"Some description", params, content, JOB_STATUS.RECEIVED);
		int jobletid1 = gridClient.submitJoblet(joblet1, 0);

		// more content
		params = new HashMap<String, String>();
		params.put("script", "S3Put.groovy");
		params.put("bucket", bucket);
		params.put("key", key2);
		params.put("contentType", "text/plain");
		params.put("public", Boolean.toString(true));
		params.put("file", file2.getAbsolutePath());
		params.put("delete", Boolean.toString(false));
		Joblet joblet2 = new Joblet(0, 0l, 0, 0, getHostname(), 1, "groovy",
				"Some description", params, content, JOB_STATUS.RECEIVED);
		int jobletid2 = gridClient.submitJoblet(joblet2, 0);

		JobletResult result1 = super.doWork();
		// work second s3 put
		JobletResult result2 = super.doWork();
	
		String url = result1.getDetails();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		int returnCode = client.executeMethod(method);
		assertEquals(returnCode, 200);
		String response = method.getResponseBodyAsString();
		assertEquals(response, content);
		// file should be deleted
		assertFalse(file.exists());

		// try an s3get joblet
		params = new HashMap<String, String>();
		params.put("script", "S3Get.groovy");
		params.put("bucket", bucket);
		params.put("key", key1);
		params.put("file", file2.getAbsolutePath());
		Joblet joblet3 = new Joblet(0, 0l, 0, 0, getHostname(), 1, "groovy",
				"Some description", params, content, JOB_STATUS.RECEIVED);
		int jobletid3 = gridClient.submitJoblet(joblet3, 0);
		JobletResult result3 = super.doWork();
		String content2 = InputStreamUtils.getFileAsString(file2);
		assertEquals(content2, content);

		// delete first key
		params = new HashMap<String, String>();
		params.put("script", "S3Delete.groovy");
		params.put("bucket", bucket);
		params.put("key", key1);
		Joblet joblet4 = new Joblet(0, 0l, 0, 0, getHostname(), 1, "groovy",
				"Some description", params, content, JOB_STATUS.RECEIVED);
		int jobletid4 = gridClient.submitJoblet(joblet4, 0);
		JobletResult result4 = super.doWork();
		assertEquals(result4.getStatus(), JOB_STATUS.COMPLETED);
		
		// delete second key
		params = new HashMap<String, String>();
		params.put("script", "S3DeleteKeys.groovy");
		params.put("bucket", bucket);
		params.put("prefix", "/prefix/");
		params.put("delimiter", null);
		Joblet joblet5 = new Joblet(0, 0l, 0, 0, getHostname(), 1, "groovy",
				"Some description", params, content, JOB_STATUS.RECEIVED);
		int jobletid5 = gridClient.submitJoblet(joblet5, 0);
		JobletResult result5 = super.doWork();
		assertEquals(result4.getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testFileJoblet() throws Exception {
		File sourceFile = File.createTempFile("test-file1", ".txt");
		File destFile = File.createTempFile("test-file2", ".txt");
		sourceFile.deleteOnExit();
		destFile.deleteOnExit();
		String content = String.format("Test Content beatch (%1$d)", System
				.currentTimeMillis());
		OutputStreamUtils.writeStringToFile(content, sourceFile);

		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "FileCopy.groovy");
		params.put("source", sourceFile.getAbsolutePath());
		params.put("dest", destFile.getAbsolutePath());
		params.put("overwrite", Boolean.toString(true));
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, content,
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);

		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);

		String fileContents = InputStreamUtils.getFileAsString(destFile);
		assertEquals(fileContents, content);
	}

	public void testImageResize() throws Exception {
		File source = File.createTempFile("test-file1", ".jpg");
		File dest = File.createTempFile("test-file2", ".jpg");
		source.deleteOnExit();
		dest.deleteOnExit();
		InputStream is = getClass().getResourceAsStream(
				"/com/dgrid/test/resources/test-image.jpg");
		OutputStreamUtils.writeStreamToFile(is, source);
		is.close();

		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "ImageResize.groovy");
		params.put("source", source.getAbsolutePath());
		params.put("dest", dest.getAbsolutePath());
		params.put("format", "png");
		params.put("width", Integer.toString(1024));
		params.put("height", Integer.toString(768));
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, "",
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);
		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testHttpGet() throws Exception {
		File dest = File.createTempFile("test-file2", ".html");
		dest.deleteOnExit();

		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "HttpGet.groovy");
		params.put("url", "http://www.sap.com/");
		params.put("file", dest.getAbsolutePath());
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, "",
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);
		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
		String contents = InputStreamUtils.getFileAsString(dest);
		assertTrue(contents.contains("SAP"));
	}

	public void testHttpLoadTest() throws Exception {
		int threadCount = 2;
		String threads = Integer.toString(threadCount);
		String urls = "http://www.oracle.com/\n" + "http://www.sap.com/\n"
				+ "http://www.cornell.edu/";
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("threads", threads);
		params.put("script", "HttpLoadTest.groovy");
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, urls,
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);
		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
		long totalDuration = Long.parseLong(result.getDetails());
		assertTrue(totalDuration > 0);
	}

	public void testRecursiveExecute() throws Exception {
		int[] nums = new int[47];
		Random r = new Random();
		int correctSum = 0;
		for (int i = 0; i < nums.length; ++i) {
			nums[i] = r.nextInt(1000);
			correctSum += nums[i];
		}
		String content = join(nums);
		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "RecursiveTest.groovy");
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, content,
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);

		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);

		assertEquals(Integer.parseInt(result.getDetails()), correctSum);
	}

	public void testStackTrace() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "StackTrace.groovy");
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, "",
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);

		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);
	}

	public void testMapReduce() throws Exception {
		int[] nums = new int[42];
		Random r = new Random();
		int correctSum = 0;
		for (int i = 0; i < nums.length; ++i) {
			nums[i] = r.nextInt(1000);
			correctSum += nums[i];
		}
		String content = join(nums);
		Map<String, String> params = new HashMap<String, String>();
		params.put("script", "MapReduceTest.groovy");
		Joblet joblet = new Joblet(0, 0l, 0, 0, getHostname(), 1,
				Constants.GROOVY_JOBLET, "Some description", params, content,
				JOB_STATUS.RECEIVED);
		int jobletid = gridClient.submitJoblet(joblet, 0);

		JobletResult result = super.doWork();
		assertEquals(result.getStatus(), JOB_STATUS.COMPLETED);

		assertEquals(Integer.parseInt(result.getDetails()), correctSum);
	}

	private String join(int[] nums) {
		StringBuffer sb = new StringBuffer();
		for (int i : nums) {
			sb.append(i);
			sb.append(',');
		}
		return sb.toString();
	}
}
