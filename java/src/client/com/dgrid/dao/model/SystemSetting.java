package com.dgrid.dao.model;

import java.io.Serializable;

public class SystemSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 362144288572873016L;

	private int id;

	private long timeCreated;

	private String name;

	private String value;

	private String description;

	public SystemSetting() {
	}

	public SystemSetting(int id, long timeCreated, String name, String value,
			String description) {
		this.id = id;
		this.timeCreated = timeCreated;
		this.name = name;
		this.value = value;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
