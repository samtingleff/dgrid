package com.dgrid.helpers.impl;

import java.io.IOException;
import java.util.Map;

import com.dgrid.helpers.HttpClientHelper;
import com.dgrid.util.webclient.HttpGetClient;
import com.dgrid.util.webclient.HttpResponse;
import com.dgrid.util.webclient.jakarta.JakartaCommonsHttpGetClient;

public class HttpClientHelperImpl implements HttpClientHelper {
	private HttpGetClient client = new JakartaCommonsHttpGetClient();

	public void setUserAgent(String agent) {
		client.setUserAgent(agent);
	}

	public void setMaxRedirects(int max) {
		client.setMaxRedirects(max);
	}

	public void setAllowCircularRedirects(boolean allow) {
		client.setAllowCircularRedirects(allow);
	}

	public void setProxyHost(String host) {
		if ((host != null) && (host.length() > 0))
			client.setProxyHost(host);
	}

	public void setProxyPort(int port) {
		if (port != 0)
			client.setProxyPort(port);
	}

	public HttpResponse getPage(String url) throws IOException {
		HttpResponse response = client.getPage(url);
		return response;
	}

	public HttpResponse getPage(String url, Map<String, String> params)
			throws IOException {
		HttpResponse response = client.getPage(url, params);
		return response;
	}

	public HttpResponse getPage(String url, Map<String, String> params,
			String username, String password) throws IOException {
		HttpResponse response = client.getPage(url, params, username, password);
		return response;
	}

	public HttpResponse postPage(String url, byte[] input, String contentType)
			throws IOException {
		HttpResponse response = client.postPage(url, input, contentType);
		return response;
	}

	public HttpResponse postPage(String url, Map<String, String> params)
			throws IOException {
		HttpResponse response = client.postPage(url, params);
		return response;
	}

	public HttpResponse postPage(String url, Map<String, String> params,
			String username, String password) throws IOException {
		HttpResponse response = client
				.postPage(url, params, username, password);
		return response;
	}

	public HttpResponse multipartPost(String url, String formFields[],
			String filenames[], byte[][] data, Map<String, String> params)
			throws IOException {
		HttpResponse response = client.multipartPost(url, formFields,
				filenames, data, params);
		return response;
	}
}
