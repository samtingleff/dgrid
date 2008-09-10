package groovy.joblets;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.helpers.HttpClientHelper;
import com.dgrid.helpers.ForkJoinHelper;
import com.dgrid.util.webclient.HttpResponse;

class HttpLoadTest implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		Map<String, String> params = joblet.parameters;
		String threadString = params.get("threads");
		String urlString = joblet.content;

		assert threadString != null, "threads param may not be null";
		assert urlString != null, "content may not be null";

		int threadCount = Integer.parseInt(threadString);
		String[] urls = urlString.split("\\s");
		HttpClientHelper httpClient = (HttpClientHelper) gridClient.getBean(HttpClientHelper.NAME);
		ForkJoinHelper fjh = (ForkJoinHelper) gridClient.getBean(ForkJoinHelper.NAME);
		List<Callable> tasks = new LinkedList<Callable>();
		for (int i = 0; i < threadCount; ++i) {
			def c = [
			         call: {
				long start = System.currentTimeMillis();
				for(int j = 0; j < urls.length; ++j) {
					HttpResponse response = httpClient.getPage(urls[j]);
					int responseCode = response.getResponseCode();
					if (responseCode != HttpResponse.OK_200) {
						gridClient.log(joblet.id, JOB_STATUS.PROCESSING, "Got non-ok http response ${responseCode}");
					}
					response.releaseConnection();
				}
				return new Long(System.currentTimeMillis() - start);
			}
			] as Callable;
			tasks.add(c);
		}
		List results = fjh.executeConcurrently(tasks, threadCount);
		long totalTime = 0;
		results.each {
			totalTime += it.longValue();
		};
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, Long.toString(totalTime));
	}
}
