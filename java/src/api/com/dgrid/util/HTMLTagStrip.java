package com.dgrid.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLTagStrip {
	private static final String regex = "\\<.*?\\>";

	private static final Pattern p = Pattern.compile(regex);

	public static String strip(String htmlString) {
		Matcher m = p.matcher(htmlString);
		String s = m.replaceAll("");
		return s;
	}
}
