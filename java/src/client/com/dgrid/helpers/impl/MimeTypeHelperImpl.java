package com.dgrid.helpers.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

import com.dgrid.helpers.MimeTypeHelper;

public class MimeTypeHelperImpl implements MimeTypeHelper {
	private MimetypesFileTypeMap types;

	public void init() throws IOException {
		InputStream mimeTypes = getClass().getResourceAsStream(
				"/com/dgrid/helpers/impl/mime.types");
		types = new MimetypesFileTypeMap(mimeTypes);
		mimeTypes.close();
	}

	public String getContentType(String filename) {
		return types.getContentType(filename);
	}

	public String getContentType(File file) {
		return types.getContentType(file);
	}

}
