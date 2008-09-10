package com.dgrid.util;

import org.apache.commons.codec.binary.Base64;

public class Base64Encoder {
	public static byte[] base64Encode(byte[] bytes) {
		byte[] result = Base64.encodeBase64(bytes);
		return result;
	}

	public static byte[] base64Decode(byte[] bytes) {
		byte[] result = Base64.decodeBase64(bytes);
		return result;
	}

	public static String base64Encode(String value) {
		byte[] bytes = value.getBytes();
		byte[] result = Base64.encodeBase64(bytes);
		String retval = new String(result);
		return (retval);
	}

	public static String base64Decode(String value) {
		byte[] result = Base64.decodeBase64(value.getBytes());
		String retval = new String(result);
		return (retval);
	}
}
