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

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Constants;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.service.DGridClient;
import com.dgrid.util.ApiCallbackTypes;
import com.dgrid.util.io.InputStreamUtils;

public class DShell extends BaseDgridDriver {
	private static Log log = LogFactory.getLog(DShell.class);

	@Option(name = "--ps", usage = "show active process list")
	private boolean ps = false;

	@Option(name = "--submit", usage = "submit a new joblet")
	private boolean submit = true;

	@Option(name = "--jobid", usage = "append to an existing job (default is no (0 value))")
	private int jobId = 0;

	@Option(name = "--priority", usage = "priority (default 1)")
	private int priority = 1;

	@Option(name = "--type", usage = "joblet type (required)")
	private String jobletType = "system";

	@Option(name = "--description", usage = "description (default is empty)")
	private String description = "";

	@Option(name = "--content", usage = "joblet contents (default is empty")
	private String content = "";

	@Option(name = "--contentFromFile", usage = "joblet contents from filename (default is empty")
	private String contentFromFile = "";

	@Option(name = "--host", usage = "run on a specific host (default is no)")
	private String host;

	@Option(name = "--callbackType", usage = "completion callback type (valid values are: xmpp|email|http|job)")
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
		CmdLineParser parser = new CmdLineParser(shell);
		parser.setUsageWidth(80);
		int exitValue = 0;
		try {
			parser.parseArgument(args);
			if (shell.ps) {
				exitValue = shell.ps();
			} else if (shell.submit) {
				exitValue = shell.submit();
			} else {
				throw new CmdLineException(
						"One of either --ps or --submit must be provided");
			}

		} catch (CmdLineException e) {
			System.err.println("Usage: dshell [options...] arguments...");
			parser.printUsage(System.err);
			log.error("Could not parse options:", e);
			exitValue = 1;
		}

		System.exit(exitValue);
	}

	DShell() throws Exception {
		AbstractApplicationContext ctx = getContext();
		gridClient = (DGridClient) ctx.getBean(DGridClient.NAME);
	}

	public int submit() throws Exception {
		log.trace("submit()");
		int returnCode = 0;
		try {
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
				Joblet j = gridClient.submitJoblet(joblet, jobId,
						ApiCallbackTypes.getCallbackType(callbackType),
						callbackAddress, callbackContent);
				message = String.format(
						"Joblet submitted to job %1$d with joblet id (%2$d)", j
								.getJobId(), j.getId());
				returnCode = 0;
			} else {
				Joblet j = gridClient.submitHostJoblet(host, joblet, jobId,
						ApiCallbackTypes.getCallbackType(callbackType),
						callbackAddress, callbackContent);
				message = String
						.format(
								"Joblet submitted to host %1$s and job %2$d with joblet id (%3$d)",
								host, j.getJobId(), j.getId());
				returnCode = 0;
			}
			System.out.println(message);
		} finally {
		}
		return returnCode;
	}

	private int ps() throws TransportException, InvalidApiKey, InvalidHost {
		log.trace("ps()");
		int returnCode = 0;
		List<Joblet> joblets = gridClient.listActiveJoblets(getUser(), 0, 100);
		System.out
				.println("Job\tJoblet\tType\tScript\tUser\tHost\tStatus\tAge\tDescription");
		for (Joblet j : joblets) {
			String hostLabel = (j.getHostId() == 0) ? "n/a" : gridClient
					.getHost(j.getHostId()).getHostname();
			String scriptLabel = ((j.getJobletType().contains("groovy")) || (j
					.getJobletType().contains("javascript"))) ? j
					.getParameters().get("script") : "n/a";
			System.out.format(
					"%1$d\t%2$d\t%3$s\t%4$s\t%5$s\t%6$s\t%7$s\t%8$s\n", j
							.getJobId(), j.getId(), j.getJobletType(),
					scriptLabel, j.getSubmitter(), hostLabel,
					getJobletStatusString(j.getStatus()), getJobletAge(j
							.getTimeCreated()), j.getDescription());
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

	private String getJobletAge(long birth) {
		long millis = System.currentTimeMillis() - birth;
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		return String.format("%1$dd %2$d:%3$d", days, hours, minutes);
	}

	private String getJobletStatusString(int jobletStatus) {
		switch (jobletStatus) {
		case JOB_STATUS.COMPLETED:
			return "completed";
		case JOB_STATUS.FAILED:
			return "failed";
		case JOB_STATUS.PROCESSING:
			return "working";
		case JOB_STATUS.QUEUED:
			return "queued";
		case JOB_STATUS.RECEIVED:
			return "received";
		case JOB_STATUS.SAVED:
			return "saved";
		default:
			return "n/a";
		}

	}
}
