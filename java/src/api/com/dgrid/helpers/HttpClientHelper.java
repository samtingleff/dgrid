package com.dgrid.helpers;

import java.io.IOException;
import java.util.Map;

import com.dgrid.util.webclient.HttpResponse;

public interface HttpClientHelper {
	public static final String NAME = "httpClientHelper";

	public HttpResponse getPage(String url) throws IOException;

	public HttpResponse getPage(String url, Map<String, String> params)
			throws IOException;

	public HttpResponse getPage(String url, Map<String, String> params,
			String username, String password) throws IOException;

	public HttpResponse postPage(String url, Map<String, String> params)
			throws IOException;

	public HttpResponse postPage(String url, Map<String, String> params,
			String username, String password) throws IOException;

	public HttpResponse postPage(String url, byte[] input, String contentType)
			throws IOException;

	public HttpResponse multipartPost(String url, String formFields[],
			String filenames[], byte[][] data, Map<String, String> params)
			throws IOException;
}
