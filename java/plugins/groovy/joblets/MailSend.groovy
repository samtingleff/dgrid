package groovy.joblets;

import java.util.Map;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.helpers.MailSender;

class MailSend implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		String from = joblet.parameters.get("from");
		String to = joblet.parameters.get("to");
		String subject = joblet.parameters.get("subject");
		String contents = joblet.content;
		assert from != null, "from parameter may not be null";
		assert to != null, "to parameter may not be null";
		assert subject != null, "subject parameter may not be null";
		assert contents != null, "joblet content may not be null";
		MailSender mail = (MailSender) gridClient.getBean(MailSender.NAME);
		mail.send(from, to, subject, contents);
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "Sent");
	}
}
