package groovy.joblets

import java.util.Map

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.helpers.SQSHelper;

class SQSSend implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		SQSHelper sqs = (SQSHelper) gridClient.getBean(SQSHelper.NAME);
		String queue = joblet.parameters.get("queue");
		String message = joblet.parameters.get("message");
		assert queue != null, "queue parameter may not be null";
		assert message != null, "message parameter may not be null";
		String msgId = sqs.send(queue, message);

		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, msgId);
	}
}
