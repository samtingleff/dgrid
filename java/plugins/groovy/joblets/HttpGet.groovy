package groovy.joblets

import java.util.Map;
import java.io.InputStream;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.helpers.HttpClientHelper;
import com.dgrid.util.webclient.HttpResponse;
import com.dgrid.util.io.OutputStreamUtils;

class HttpGet implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		Map<String, String> params = joblet.parameters;
		String url = params.get("url");
		String filename = params.get("file");
		
		assert url != null, "url param may not be null";
		assert filename != null, "file param may not be null";

		File file = new File(filename);
		HttpClientHelper httpClient = (HttpClientHelper) gridClient.getBean(HttpClientHelper.NAME);
		HttpResponse httpResponse = httpClient.getPage(url);
		InputStream is = httpResponse.getResponseBodyAsStream();
		OutputStreamUtils.writeStreamToFile(is, file);
		is.close();
		httpResponse.releaseConnection();
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "");
	}
}
