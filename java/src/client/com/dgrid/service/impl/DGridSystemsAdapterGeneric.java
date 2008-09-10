package com.dgrid.service.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.service.DGridSystemsAdapter;

public abstract class DGridSystemsAdapterGeneric implements DGridSystemsAdapter {

	protected Log log = LogFactory.getLog(getClass());

	public DGridSystemsAdapterGeneric() {
		super();
	}

	public long getVmUptime() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		return runtime.getUptime();
	}

	public double getSystemLoadAverage() {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		return os.getSystemLoadAverage();
	}

	public long getFreeMemory() {
		Runtime r = Runtime.getRuntime();
		// an approximation to the total amount of memory currently available
		// for future allocated objects, measured in bytes.
		long free = r.freeMemory();
		// the maximum amount of memory that the virtual machine will attempt to
		// use, measured in bytes
		long max = r.maxMemory();
		// the total amount of memory currently available for current and future
		// objects, measured in bytes.
		long t = r.totalMemory();

		return free;
	}

	public long getUsedMemory() {
		Runtime r = Runtime.getRuntime();
		return r.totalMemory() - r.freeMemory();
	}

	public int getActiveThreadCount() {
		ThreadMXBean mx = ManagementFactory.getThreadMXBean();
		return mx.getThreadCount();
	}

}