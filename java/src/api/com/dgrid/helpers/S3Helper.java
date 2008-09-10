package com.dgrid.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;

import com.dgrid.errors.AWSException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;

public interface S3Helper {
	public static final String NAME = "s3Helper";

	public String[] listBuckets() throws TransportException, IOException,
			AWSException;

	public void createBucket(String bucket) throws TransportException,
			IOException, AWSException;

	public void deleteBucket(String bucket) throws TransportException,
			IOException, AWSException;

	public String[] listKeys(String bucket) throws TransportException,
			IOException, AWSException;

	public String put(File file, String bucket, String key, String contentType,
			boolean isPublic) throws TransportException, IOException,
			AWSException;

	public String put(File file, String bucket, String key, String contentType,
			Map<String, Object> metadata, boolean isPublic) throws IOException,
			AWSException, TransportException;

	public String put(String filename, String bucket, String key,
			String contentType, boolean isPublic) throws TransportException,
			IOException, AWSException;

	public void get(String bucket, String key, File dest)
			throws TransportException, IOException, AWSException;

	public void get(String bucket, String key, String filename)
			throws TransportException, IOException, AWSException;

	public void delete(String bucket, String key) throws TransportException,
			IOException, AWSException;

	public S3Service getS3Service() throws TransportException, InvalidApiKey,
			S3ServiceException;
}
