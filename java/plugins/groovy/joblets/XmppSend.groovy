package groovy.joblets

import java.util.Map

import com.dgrid.api.SimpleJoblet
import com.dgrid.api.SimpleJobletResult
import com.dgrid.gen.Joblet
import com.dgrid.gen.JOB_STATUS
import com.dgrid.gen.JobletResult
import com.dgrid.service.DGridClient
import com.dgrid.service.XmppConnection

class XmppSend implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		XmppConnection xmpp = (XmppConnection) gridClient.getBean(XmppConnection.NAME);
		xmpp.sendTextMessage(joblet.parameters.get("to"),
				joblet.parameters.get("subject"), joblet.content);
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, "Sent");
	}
}
