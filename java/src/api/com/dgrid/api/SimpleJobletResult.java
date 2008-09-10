package com.dgrid.api;

public class SimpleJobletResult implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2073559878442044000L;

	private int returnCode;

	private int status;

	private String details;

	public SimpleJobletResult() {
	}

	public SimpleJobletResult(int returnCode, int status, String details) {
		this.returnCode = returnCode;
		this.status = status;
		this.details = details;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

}
