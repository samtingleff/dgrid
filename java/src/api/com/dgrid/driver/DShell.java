package com.dgrid.driver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.context.support.AbstractApplicationContext;

import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.ApiCallbackTypes;
import com.dgrid.util.io.InputStreamUtils;

public class DShell extends BaseDgridDriver {
	private Log log = LogFactory.getLog(getClass());

	@Option(name = "--jobid", usage = "append to an existing job (default is no (0 value))")
	private int jobId = 0;

	@Option(name = "--priority", usage = "priority (default 1)")
	private int priority = 1;

	@Option(name = "--type", required = true, usage = "joblet type (required)")
	private String jobletType;

	@Option(name = "--description", usage = "description (default is empty)")
	private String description = "";

	@Option(name = "--content", usage = "joblet contents (default is empty")
	private String content = "";

	@Option(name = "--contentFromFile", usage = "joblet contents from filename (default is empty")
	private String contentFromFile = "";

	@Option(name = "--host", usage = "run on a specific host (default is no)")
	private String host;

	@Option(name = "--callbackType", usage = "completion callback type (valid values are: xmpp email http job)")
	private String callbackType = "none";

	@Option(name = "--callbackAddress", usage = "address for callback (xmpp jid, http url, or email address)")
	private String callbackAddress = "";

	@Option(name = "--callbackContent", usage = "contents of callback")
	private String callbackContent = "";

	@Option(name = "--param", usage = "set a parameter (name:value)")
	private List<String> paramList = new ArrayList<String>();

	@Option(name = "--execute", usage = "execute immediately")
	private boolean execute = false;

	private DGridClient gridClient;

	public static void main(String[] args) throws Exception {
		DShell shell = new DShell();
		int exitValue = shell.execute(args);
		System.exit(exitValue);
	}

	DShell() throws Exception {
		AbstractApplicationContext ctx = getContext();
		gridClient = (DGridClient) ctx.getBean(DGridClient.NAME);
	}

	public int execute(String[] args) throws Exception {
		log.trace("execute()");
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		int returnCode = 0;
		try {
			parser.parseArgument(args);
			String jobletContent = (content.length() > 0) ? content
					: ((contentFromFile.length() > 0) ? InputStreamUtils
							.getFileAsString(new File(contentFromFile)) : "");
			Map<String, String> params = parseMap(paramList);
			Joblet joblet = new Joblet(0, 0l, jobId, 0, getUser(), priority,
					jobletType, description, params, jobletContent,
					JOB_STATUS.RECEIVED);
			String message = null;
			if (execute) {
				JobletResult result = gridClient.gridExecute(joblet, 1);
				message = String.format("Return code: %1$d, status: %2$d",
						result.getReturnCode(), result.getStatus());
				System.out.println(result.getDetails());
				returnCode = result.getReturnCode();
			} else if ((host == null) || (host.length() == 0)) {
				int returnedJobletId = gridClient.submitJoblet(joblet, jobId,
						ApiCallbackTypes.getCallbackType(callbackType),
						callbackAddress, callbackContent);
				returnCode = 0;
				if (jobId != 0) {
					message = String
							.format(
									"Joblet submitted to job %1$d with joblet id (%2$d)",
									jobId, returnedJobletId);
				} else {
					message = String.format("Job submitted with id %1$d",
							returnedJobletId);
				}
			} else {
				int returnedJobletId = gridClient.submitHostJoblet(host,
						joblet, jobId, ApiCallbackTypes
								.getCallbackType(callbackType),
						callbackAddress, callbackContent);
				if (jobId != 0) {
					message = String
							.format(
									"Joblet submitted to host %1$s and job %2$d with joblet id (%3$d)",
									host, jobId, returnedJobletId);
				} else {
					message = String.format(
							"Job submitted to host %1$s with job id %2$d",
							host, returnedJobletId);
				}
				returnCode = 0;
			}
			System.out.println(message);
		} catch (CmdLineException e) {
			System.err.println("Usage: dshell [options...] arguments...");
			parser.printUsage(System.err);
			log.error("Could not parse options:", e);
			returnCode = 1;
		} finally {
		}
		return returnCode;
	}

	private String getUser() {
		return System.getProperty("user.name");
	}

	private Map<String, String> parseMap(List<String> list)
			throws CmdLineException {
		Map<String, String> retval = new HashMap<String, String>(list.size());
		for (String string : list) {
			if (string.matches("[\\w\\:.+]")) {
				throw (new CmdLineException(
						String
								.format(
										"Argument \"%1$s\" does not match required format name:value",
										string)));
			}
			String name = string.substring(0, string.indexOf(':'));
			String value = string.substring((string.indexOf(':') + 1), string
					.length());
			retval.put(name, value);
		}
		return retval;
	}
}
