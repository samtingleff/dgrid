package com.dgrid.test.helpers;

import com.dgrid.helpers.HttpClientHelper;
import com.dgrid.test.BaseTestCase;
import com.dgrid.util.webclient.HttpResponse;

public class HttpClientHelperTestCase extends BaseTestCase {

	public void testGetURI() throws Exception {
		String url = "http://is.gd/Ffh";
		HttpClientHelper httpClient = (HttpClientHelper) super
				.getBean(HttpClientHelper.NAME);
		HttpResponse response = httpClient.getPage(url);
		String uri = response.getURI();
		assertEquals(uri,
				"http://blog.seattlepi.nwsource.com/microsoft/archives/141821.asp");
	}
}
