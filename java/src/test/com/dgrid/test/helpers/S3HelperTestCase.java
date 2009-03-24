package com.dgrid.test.helpers;

import java.io.File;

import com.dgrid.helpers.S3Helper;
import com.dgrid.test.BaseTestCase;
import com.dgrid.util.io.InputStreamUtils;

public class S3HelperTestCase extends BaseTestCase {
	public void testS3Helper() throws Exception {
		String bucket = "backups.othersonline.com";
		String prefix = "gamma/db-backups/";
		S3Helper s3 = (S3Helper) super.getBean(S3Helper.NAME);
		String[] keys = s3.listKeys(bucket, prefix, null);
		assertNotNull(keys);
		assertTrue(keys.length > 0);
	}

	public void testPutString() throws Exception {
		S3Helper s3 = (S3Helper) super.getBean(S3Helper.NAME);
		String content = "Hello world";
		String bucket = "data.othersonline.com";
		String key = "test/test-content.txt";
		String contentType = "text/plain";
		String url = s3.putString(content, bucket, key, contentType, false);

		String retrieved = s3.getString(bucket, key);
		assertEquals(retrieved, content);

		File tempFile = File.createTempFile("test-file", ".txt");
		s3.get(bucket, key, tempFile);
		String s = InputStreamUtils.getFileAsString(tempFile);
		tempFile.delete();
		assertEquals(s, content);
		s3.delete(bucket, key);
	}
}
