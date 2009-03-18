package com.dgrid.util.webclient;

import java.io.IOException;
import java.io.InputStream;

public interface HttpResponse {
	public static final int OK_200 = 200;

	public static final int REDIRECT_302 = 302;

	public static final int BAD_REQUEST_400 = 400;

	public static final int UNAUTHORIZED_401 = 401;

	public static final int FORBIDDEN_403 = 403;

	public static final int NOT_FOUND_404 = 404;

	public static final int INTERNAL_SERVER_ERROR_500 = 500;

	public static final int NOT_IMPLEMENTED_501 = 501;

	public int getResponseCode();

	public String getURI();

	public String getContentType();

	public String getContentCharset();

	public String getResponseBodyAsString() throws IOException;

	public InputStream getResponseBodyAsStream() throws IOException;

	public void releaseConnection();
}
