package groovy.joblets;

import java.io.File;
import java.util.Map;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.helpers.S3Helper;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

class S3Delete implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		Map<String, String> params = joblet.parameters;
		String bucket = params.get("bucket");
		String key = params.get("key");

		assert bucket != null, "Bucket may not be null";
		assert key != null, "Key may not be null";

		S3Helper s3 = (S3Helper) gridClient.getBean(S3Helper.NAME);
		s3.delete(bucket, key);
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "");
	}
}
