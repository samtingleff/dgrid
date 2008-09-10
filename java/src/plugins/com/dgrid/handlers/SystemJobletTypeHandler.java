package com.dgrid.handlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.dgrid.api.BaseJobletTypeHandler;
import com.dgrid.api.JobletTypeHandler;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.Execute;
import com.dgrid.util.NullOutputStream;

public class SystemJobletTypeHandler extends BaseJobletTypeHandler implements
		JobletTypeHandler {
	public static final String SAVE_OUTPUT_PARAM = "output";

	public JobletResult execute(Joblet joblet, DGridClient gridClient)
			throws Exception {
		log.trace("execute()");
		long start = System.currentTimeMillis();
		String command = joblet.getContent();
		boolean saveOutput = Boolean.parseBoolean(joblet.getParameters().get(
				SAVE_OUTPUT_PARAM));
		PrintStream ps = null;
		int retval = 0;
		String contents = null;
		if (saveOutput) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			retval = executeCommand(command, ps);
			contents = baos.toString();
			baos.close();
		} else {
			ps = new PrintStream(new NullOutputStream());
			retval = executeCommand(command, ps);
			contents = "";
		}
		ps.close();
		int returnStatus = (retval == 0) ? JOB_STATUS.COMPLETED
				: JOB_STATUS.FAILED;
		return new JobletResult(0, 0l, retval,
				(System.currentTimeMillis() - start), returnStatus, contents,
				joblet);
	}

	private int executeCommand(String command, PrintStream out)
			throws InterruptedException, IOException {
		log.trace("executeCommand()");
		if (log.isDebugEnabled()) {
			log.debug(String.format("Executing command (%1$s)", command));
		}
		int retval = Execute.execute(command, out, out);
		return retval;
	}
}
