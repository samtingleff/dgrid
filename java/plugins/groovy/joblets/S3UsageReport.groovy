package groovy.joblets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.helpers.S3Helper;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

class S3UsageReport implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		Map<String, String> params = joblet.parameters;
		String bucket = params.get("bucket");
		String prefix = params.get("prefix");
		String delimiter = params.get("delimiter");
		String output = params.get("file");

		assert output != null, "file may not be null";

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		S3Helper s3Helper = gridClient.getBean(S3Helper.NAME);
		S3Service s3 = s3Helper.getS3Service();
		def s3buckets = null;

		if (bucket != null) {
			s3buckets = [new S3Bucket(bucket)];
		} else {
			s3buckets = s3.listAllBuckets();
		}
		FileOutputStream fis = new FileOutputStream(new File(output));
		PrintWriter pw = new PrintWriter(fis);
		pw.println("bucket,key,bytes,etag,last-modified");
		s3buckets.each { currentBucket ->
			S3Object[] objects = null;
			if (prefix != null) {
				objects = s3.listObjects(currentBucket, prefix, delimiter);
			} else {
				objects = s3.listObjects(currentBucket);
			}
			for (int j = 0; j < objects.length; ++j) {
				S3Object obj = objects[j];
				String key = obj.getKey();
				long length = obj.getContentLength();
				String etag = obj.getETag();
				Date date = obj.getLastModifiedDate();
				String dateString = df.format(date);
				pw.println(currentBucket.getName() + "," + key + "," + length + "," + etag + "," + dateString);
			}
		}
		pw.close();
		fis.close();
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, params.get("testKey"));
	}
}
