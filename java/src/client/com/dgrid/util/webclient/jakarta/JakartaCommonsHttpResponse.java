package com.dgrid.util.webclient.jakarta;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;

import com.dgrid.util.webclient.HttpResponse;

public class JakartaCommonsHttpResponse implements HttpResponse {

	private HttpMethod method;

	private int responseCode;

	private String contentType;

	private String contentCharset;

	private long contentLength;

	public JakartaCommonsHttpResponse(HttpMethod method, int responseCode,
			String contentType, String charset, long contentLength) {
		this.method = method;
		this.responseCode = responseCode;
		this.contentType = contentType;
		this.contentCharset = charset;
		this.contentLength = contentLength;
	}

	public int getResponseCode() {
		return (responseCode);
	}

	public String getURI() {
		try {
			return method.getURI().toString();
		} catch (URIException e) {
			throw new RuntimeException(e);
		}
	}

	public String getContentType() {
		return (contentType);
	}

	public String getContentCharset() {
		return contentCharset;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getResponseBodyAsString() throws IOException {
		return (method.getResponseBodyAsString());
	}

	public InputStream getResponseBodyAsStream() throws IOException {
		return (method.getResponseBodyAsStream());
	}

	public void releaseConnection() {
		method.releaseConnection();
	}

}
