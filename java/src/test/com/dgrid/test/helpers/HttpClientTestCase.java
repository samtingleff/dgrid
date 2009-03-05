package com.dgrid.test.helpers;

import com.dgrid.util.webclient.HttpGetClient;
import com.dgrid.util.webclient.HttpResponse;
import com.dgrid.util.webclient.jakarta.JakartaCommonsHttpGetClient;

import junit.framework.TestCase;

public class HttpClientTestCase extends TestCase {
	public void testAuthenticatedGetRequest() throws Exception {
		HttpGetClient httpClient = new JakartaCommonsHttpGetClient();
		// only public http basic authenticating server I could find!
		String url = "http://test.webdav.org/auth-basic/";
		String username = "user1";
		String password = "user1";
		HttpResponse response = httpClient.getPage(url, null, username,
				password);
		assertEquals(response.getResponseCode(), 404);
		response.releaseConnection();
	}
}
