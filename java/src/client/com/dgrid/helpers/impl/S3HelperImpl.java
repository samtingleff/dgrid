package com.dgrid.helpers.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import com.dgrid.errors.AWSException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.helpers.AWSConstants;
import com.dgrid.helpers.S3Helper;
import com.dgrid.service.DGridClient;

public class S3HelperImpl implements S3Helper {

	private static final int BUFFSIZE = 2048;

	private Log log = LogFactory.getLog(getClass());

	private DGridClient gridClient;

	public void setGridClient(DGridClient gridClient) {
		this.gridClient = gridClient;
	}

	public String[] listBuckets() throws TransportException, IOException,
			AWSException {
		log.trace("listBuckets()");
		try {
			S3Service s3Service = getS3Service();
			S3Bucket[] s3buckets = s3Service.listAllBuckets();
			String[] buckets = new String[s3buckets.length];
			for (int i = 0; i < s3buckets.length; ++i) {
				buckets[i] = s3buckets[i].getName();
			}
			return buckets;
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} finally {
		}
	}

	public void createBucket(String bucket) throws TransportException,
			IOException, AWSException {
		log.trace("createBucket()");
		try {
			S3Service s3Service = getS3Service();
			s3Service.createBucket(bucket);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} finally {
		}

	}

	public void deleteBucket(String bucket) throws TransportException,
			IOException, AWSException {
		log.trace("deleteBucket()");
		try {
			S3Service s3Service = getS3Service();
			s3Service.deleteBucket(bucket);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} finally {
		}
	}

	public String[] listKeys(String bucket) throws TransportException,
			IOException, AWSException {
		log.trace("listKeys()");
		try {
			S3Service s3Service = getS3Service();
			S3Bucket s3bucket = new S3Bucket(bucket);
			S3Object[] s3objects = s3Service.listObjects(s3bucket);
			String[] keys = new String[s3objects.length];
			for (int i = 0; i < s3objects.length; ++i) {
				keys[i] = s3objects[i].getKey();
			}
			return keys;
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} catch (TransportException e) {
			throw new AWSException(e);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} finally {
		}
	}

	public String[] listKeys(String bucket, String prefix, String delimiter)
			throws TransportException, IOException, AWSException {
		log.trace("listKeys()");
		try {
			S3Service s3Service = getS3Service();
			S3Bucket s3bucket = new S3Bucket(bucket);
			S3Object[] s3objects = s3Service.listObjects(s3bucket, prefix,
					delimiter);
			String[] keys = new String[s3objects.length];
			for (int i = 0; i < s3objects.length; ++i) {
				keys[i] = s3objects[i].getKey();
			}
			return keys;
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} catch (TransportException e) {
			throw new AWSException(e);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} finally {
		}
	}

	public void get(String bucket, String key, File dest) throws IOException,
			AWSException {
		log.trace("get()");
		try {
			S3Service s3Service = getS3Service();
			S3Bucket s3bucket = new S3Bucket(bucket);
			S3Object s3object = s3Service.getObject(s3bucket, key);
			InputStream is = s3object.getDataInputStream();
			FileOutputStream fos = new FileOutputStream(dest);
			int read = 0;
			byte[] buffer = new byte[BUFFSIZE];
			while ((read = is.read(buffer)) > 0) {
				fos.write(buffer, 0, read);
			}
			fos.close();
			s3object.closeDataInputStream();
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} catch (TransportException e) {
			throw new AWSException(e);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} finally {
		}
	}

	public void get(String bucket, String key, String filename)
			throws IOException, AWSException {
		log.trace("get()");
		this.get(bucket, key, new File(filename));
	}

	public String put(File file, String bucket, String key, String contentType,
			boolean isPublic) throws IOException, AWSException,
			TransportException {
		log.trace("put()");
		return this.put(file, bucket, key, contentType, null, isPublic);
	}

	public String put(File file, String bucket, String key, String contentType,
			Map<String, Object> metadata, boolean isPublic) throws IOException,
			AWSException, TransportException {
		log.trace("put()");
		try {
			S3Bucket s3bucket = new S3Bucket(bucket);
			S3Object fileObject = new S3Object(s3bucket, key);
			if (isPublic)
				fileObject.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
			else
				fileObject.setAcl(AccessControlList.REST_CANNED_PRIVATE);
			fileObject.setContentType(contentType);
			fileObject.setDataInputFile(file);
			fileObject.setContentLength(file.length());
			if (metadata != null)
				fileObject.addAllMetadata(metadata);
			S3Service s3Service = getS3Service();
			s3Service.putObject(bucket, fileObject);
			fileObject.closeDataInputStream();
			return getUrl(fileObject);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		} finally {
		}
	}

	public String put(String filename, String bucket, String key,
			String contentType, boolean isPublic) throws IOException,
			AWSException, TransportException {
		log.trace("put()");
		return this.put(new File(filename), bucket, key, contentType, null,
				isPublic);
	}

	public void delete(String bucket, String key) throws TransportException,
			IOException, AWSException {
		log.trace("delete()");
		try {
			S3Bucket s3bucket = new S3Bucket(bucket);
			S3Service s3Service = getS3Service();
			s3Service.deleteObject(s3bucket, key);
		} catch (InvalidApiKey e) {
			throw new AWSException(e);
		} catch (S3ServiceException e) {
			throw new AWSException(e);
		}
	}

	public S3Service getS3Service() throws TransportException, InvalidApiKey,
			S3ServiceException {
		log.trace("getS3Service()");
		String awsAccessKey = gridClient.getSetting(
				AWSConstants.AWS_ACCESS_KEY_SETTING, "");
		String awsSecretKey = gridClient.getSetting(
				AWSConstants.AWS_SECRET_KEY_SETTING, "");
		AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey,
				awsSecretKey);
		S3Service s3Service = new RestS3Service(awsCredentials);
		return s3Service;
	}

	private String getUrl(S3Object s3object) {
		return String.format("http://s3.amazonaws.com/%1$s/%2$s", s3object
				.getBucketName(), s3object.getKey());
	}
}
