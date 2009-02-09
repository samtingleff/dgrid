package com.dgrid.errors;

public class SDBException extends AWSException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1747576172854739263L;

	public SDBException(Throwable root)
	{
		super(root);
	}

}
