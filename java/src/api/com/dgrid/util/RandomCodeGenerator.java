package com.dgrid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RandomCodeGenerator {
	private static Log log = LogFactory.getLog(RandomCodeGenerator.class);

	private static SecureRandom secureRandom;

	public static String getCode(int length) {
		byte[] buffer = new byte[length];
		secureRandom.nextBytes(buffer);
		byte[] result = Base64.encodeBase64(buffer);
		String s = new String(result, 0, length);
		return (s);
	}

	public static String getLetterCode(int length) {
		int min = (int) 'a';
		int max = (int) 'z';
		int minCapital = (int) 'A';
		int maxCapital = (int) 'Z';
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; ++i) {
			int maxint = (26 * 2) + 10;
			int c = secureRandom.nextInt(maxint);
			if (c < 10) {
				sb.append(c);
			} else if (c < (26 + 10)) {
				c = min + c - 10;
				sb.append((char) c);
			} else {
				c = minCapital + ((c - 10) / 2);
				sb.append((char) c);
			}
		}
		return (sb.toString());
	}

	static {
		try {
			secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
			File urandom = new File("/dev/urandom");
			if (urandom.exists()) {
				byte[] salt = new byte[8192];
				FileInputStream fis = new FileInputStream(urandom);
				fis.read(salt);
				fis.close();
				secureRandom.setSeed(salt);
			}
		} catch (NoSuchAlgorithmException e) {
			log.fatal("No such algorithm: ", e);
		} catch (NoSuchProviderException e) {
			log.fatal("No such provider: ", e);
		} catch (IOException e) {
			log.fatal("IOException: ", e);
		}

	}
}
