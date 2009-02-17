package com.dgrid.util.webclient;

import java.io.IOException;
import java.util.Map;

public interface HttpGetClient {

	public void setUserAgent(String agent);

	public void setMaxRedirects(int max);

	public void setAllowCircularRedirects(boolean allow);

	public void setProxyHost(String host);

	public void setProxyPort(int port);

	public HttpResponse getPage(String url) throws IOException;

	public HttpResponse getPage(String url, Map<String, String> params)
			throws IOException;

	public HttpResponse postPage(String url, Map<String, String> params)
			throws IOException;

	public HttpResponse postPage(String url, byte[] input, String contentType)
			throws IOException;

	public HttpResponse multipartPost(String url, String formFields[],
			String filenames[], byte[][] data, Map<String, String> params)
			throws IOException;
}
