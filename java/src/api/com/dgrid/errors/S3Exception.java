package com.dgrid.errors;

public class S3Exception extends AWSException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3814200790849997838L;

	public S3Exception(Throwable root) {
		super(root);
	}

}
