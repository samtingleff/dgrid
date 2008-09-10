package com.dgrid.dao.model;

import java.io.Serializable;

import com.dgrid.gen.Joblet;

public class JobletLogEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6462210911210007157L;

	private int id;

	private Joblet joblet;

	private String message;

	public JobletLogEntry() {
	}

	public JobletLogEntry(int id, Joblet joblet, String message) {
		this.id = id;
		this.joblet = joblet;
		this.message = message;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Joblet getJoblet() {
		return joblet;
	}

	public void setJoblet(Joblet joblet) {
		this.joblet = joblet;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
