package groovy.joblets;

import java.io.File;
import java.util.Map;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.io.OutputStreamUtils;

class FileCopy implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		Map<String, String> params = joblet.parameters;
		String sourceFilename = params.get("source");
		String destFilename = params.get("dest");
		assert sourceFilename != null, "source parameter may not be null";
		assert destFilename != null, "dest parameter may not be null";

		File source = new File(sourceFilename);
		File dest = new File(destFilename);
		assert source.canRead(), "Cannot read from ${sourceFilename}";

		boolean overwrite = Boolean.parseBoolean(params.get("overwrite"));

		if (dest.exists()) {
			assert overwrite, "Cannot overwrite ${filename}. Set overwrite to true.";
		}
		assert dest.canWrite(), "Cannot write to ${filename}";
		OutputStreamUtils.copyFile(source, dest);
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "");
	}
}
