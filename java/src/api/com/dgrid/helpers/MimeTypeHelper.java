package com.dgrid.helpers;

import java.io.File;

public interface MimeTypeHelper {
	public static final String NAME = "mimeTypeHelper";

	public String getContentType(String filename);

	public String getContentType(File file);
}
