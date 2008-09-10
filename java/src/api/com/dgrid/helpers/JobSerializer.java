package com.dgrid.helpers;

import com.dgrid.gen.Job;

public interface JobSerializer {
	public static final String XML = "xmlJobSerializer";

	public String serialize(Job job);

	public Job deserialize(String text);
}
