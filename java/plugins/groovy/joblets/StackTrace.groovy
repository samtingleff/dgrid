package groovy.joblets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.dgrid.api.SimpleJoblet;
import com.dgrid.api.SimpleJobletResult;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;

class StackTrace implements SimpleJoblet {
	public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
		Set<Map.Entry<Thread, StackTraceElement[]>> entries = stacks.entrySet();
		Iterator<Map.Entry<Thread, StackTraceElement[]>> iter = entries.iterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			Map.Entry<Thread, StackTraceElement[]> entry = iter.next();
			Thread t = entry.getKey();
			StackTraceElement[] stackElement = entry.getValue();
			sb.append("Thread #${t.getId()}: (name=${t.getName()}) (priority=${t.getPriority()}) (state=${t.getState()})\n");
			for (int i = 0; i < stackElement.length; ++i) {
				sb.append('\t');
				sb.append(stackElement[i].toString());
				sb.append('\n');
			}
		}
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, sb.toString());
	}
}
