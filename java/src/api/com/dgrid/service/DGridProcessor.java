package com.dgrid.service;

public interface DGridProcessor extends Runnable {
	public static final String NAME = "dgridProcessor";

	public void init() throws Exception;

	public void run();

	public void stop();
}
