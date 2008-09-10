package com.dgrid.service;

public interface DGridSystemsAdapter {
	public static final String NAME = "systemsAdapter";

	public long getVmUptime();

	public double getSystemLoadAverage();

	public long getFreeMemory();

	public long getUsedMemory();

	public int getActiveThreadCount();

	public void restart();

	public void shutdown();

	public void halt();
}
