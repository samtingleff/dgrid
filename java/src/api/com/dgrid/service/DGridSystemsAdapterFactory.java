package com.dgrid.service;

public interface DGridSystemsAdapterFactory {
	public static final String NAME = "systemsAdapterFactory";

	public DGridSystemsAdapter getSystemsAdapter();
}
