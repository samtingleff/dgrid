package com.dgrid.test.helpers;

import java.io.File;
import java.io.IOException;

import com.dgrid.helpers.MimeTypeHelper;
import com.dgrid.test.BaseTestCase;

public class MimeTypeHelperTestCase extends BaseTestCase
{
	public void testMimeTypeHelper() throws IOException
	{
		MimeTypeHelper helper = (MimeTypeHelper) super.getBean(MimeTypeHelper.NAME);
		File textFile = File.createTempFile("test-file", ".txt");
		textFile.deleteOnExit();
		assertEquals("text/plain", helper.getContentType(textFile));
		assertEquals("text/plain", helper.getContentType(textFile.getAbsolutePath()));
	}
}
