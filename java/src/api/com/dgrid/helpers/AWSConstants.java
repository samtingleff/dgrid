package com.dgrid.helpers;

public class AWSConstants {

	public static final String AWS_ACCESS_KEY_SETTING = "aws.accessKey";

	public static final String AWS_SECRET_KEY_SETTING = "aws.secretKey";

	public static final String AWS_SECURE = "aws.secure";

	public static enum EC2InstanceType {
		DEFAULT, LARGE, MEDIUM_HCPU, XLARGE, XLARGE_HCPU
	}
}
