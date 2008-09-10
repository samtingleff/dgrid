package groovy.joblets;

import java.util.Map;
import java.io.File;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.ImageScaler;

class ImageResize implements SimpleJoblet {
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

		String format = params.get("format");
		assert format != null, "format parameter may not be null";
		assert ["jpg", "png", "gif"].contains(format), "${format} is not one of jpg, png or gif";

		int width = Integer.parseInt(params.get("width"));
		int height = Integer.parseInt(params.get("height"));
		ImageScaler.scaleImage(source, dest, format, width, height);

		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "");
	}
}
