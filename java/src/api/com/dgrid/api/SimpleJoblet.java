package com.dgrid.api;

import com.dgrid.gen.Joblet;
import com.dgrid.service.DGridClient;

public interface SimpleJoblet {

	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception;
}
