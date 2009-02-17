package com.dgrid.util.webclient.jakarta;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.util.webclient.HttpGetClient;
import com.dgrid.util.webclient.HttpResponse;

public class JakartaCommonsHttpGetClient implements HttpGetClient {
	private Log log = LogFactory.getLog(getClass());

	private HostConfiguration hostConfiguration = new HostConfiguration();

	private MultiThreadedHttpConnectionManager connectionManager;

	private HttpClient client;

	private String proxyHost;

	private int proxyPort;

	public JakartaCommonsHttpGetClient() {
		connectionManager = new MultiThreadedHttpConnectionManager();
		client = new HttpClient(connectionManager);
	}

	public void setTimeout(int millis) {
		connectionManager.getParams().setConnectionTimeout(millis);
	}

	public void setUserAgent(String agent) {
		client.getParams().setParameter("http.useragent", agent);
	}

	public void setMaxRedirects(int max) {
		client.getParams().setParameter("http.protocol.max-redirects", max);
	}

	public void setAllowCircularRedirects(boolean allow) {
		client.getParams().setParameter(
				"http.protocol.allow-circular-redirects", allow);
	}

	public void setProxyHost(String host) {
		this.proxyHost = host;
		initProxy();
	}

	public void setProxyPort(int port) {
		this.proxyPort = port;
		initProxy();
	}

	public HttpResponse getPage(String url) throws IOException {
		log.trace("getPage()");
		GetMethod method = new GetMethod(url);
		HttpResponse response = executeMethod(method);
		return (response);
	}

	public HttpResponse getPage(String url, Map<String, String> params)
			throws IOException {
		log.trace("getPage()");
		GetMethod method = new GetMethod(url);
		if (params != null) {
			NameValuePair[] nvp = new NameValuePair[params.size()];
			int i = 0;
			for (Map.Entry<String, String> e : params.entrySet()) {
				nvp[i] = new NameValuePair(e.getKey(), e.getValue());
				++i;
			}
			method.setQueryString(nvp);
		}
		HttpResponse response = executeMethod(method);
		return (response);
	}

	public HttpResponse postPage(String url, Map<String, String> params)
			throws IOException {
		log.trace("postPage()");
		NameValuePair[] data = new NameValuePair[params.size()];
		Set<Map.Entry<String, String>> entries = params.entrySet();
		Iterator<Map.Entry<String, String>> iter = entries.iterator();
		int i = 0;
		while (iter.hasNext()) {
			Map.Entry<String, String> e = iter.next();
			data[i] = new NameValuePair(e.getKey(), e.getValue());
			++i;
		}
		PostMethod method = new PostMethod(url);
		method.setRequestBody(data);
		HttpResponse response = executeMethod(method);
		return (response);
	}

	public HttpResponse postPage(String url, byte[] input, String contentType)
			throws IOException {
		log.trace("postPage()");
		RequestEntity entity = new ByteArrayRequestEntity(input, contentType);
		HttpResponse response = doPostPage(url, entity, null);
		return response;
	}

	public HttpResponse multipartPost(String url, String formFields[],
			String filenames[], byte[][] data, Map<String, String> params)
			throws IOException {
		log.trace("multipartPost()");
		Part[] parts = new Part[formFields.length];
		for (int i = 0; i < formFields.length; ++i) {
			parts[i] = new FilePart(formFields[i], new ByteArrayPartSource(
					filenames[i], data[i]));
		}
		MultipartRequestEntity entity = new MultipartRequestEntity(parts,
				new HttpMethodParams());
		HttpResponse response = doPostPage(url, entity, params);
		return response;
	}

	private HttpResponse doPostPage(String url, RequestEntity entity,
			Map<String, String> params) throws HttpException, IOException {
		log.info("posting to url: " + url);
		PostMethod method = new PostMethod(url);
		if (params != null) {
			Set<Map.Entry<String, String>> entries = params.entrySet();
			Iterator<Map.Entry<String, String>> keys = entries.iterator();
			while (keys.hasNext()) {
				Map.Entry<String, String> entry = keys.next();
				method.addParameter(entry.getKey(), entry.getValue());
				if (log.isDebugEnabled())
					log.debug("set string part (" + entry.getKey() + ") = ("
							+ entry.getValue() + ")");
			}
		}
		method.setRequestEntity(entity);
		HttpResponse response = executeMethod(method);
		return (response);
	}

	private HttpResponse executeMethod(HttpMethod method) throws HttpException,
			IOException {
		log.trace("executeMethod()");
		method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		log.info("fetching url: " + method.getURI());
		int result = client.executeMethod(method);
		Header contentTypeHeader = method.getResponseHeader("Content-Type");
		String contentType = contentTypeHeader.getValue();
		if (log.isDebugEnabled()) {
			log.debug("HTTP response: " + result);
			log.debug("Content type: " + contentType);
			log.debug("Query string: " + method.getQueryString());
		}
		HttpResponse response = new JakartaCommonsHttpResponse(method, result,
				contentType);
		return (response);
	}

	private void initProxy() {
		if ((proxyHost != null) && (proxyPort != 0)) {
			hostConfiguration.setProxy(proxyHost, proxyPort);
			client.setHostConfiguration(hostConfiguration);
		}
	}
}
