package com.dgrid.driver;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import com.dgrid.gen.Constants;
import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.service.DGridTransport;
import com.dgrid.util.ApiCallbackTypes;

/**
 * 
 * @author samtingleff
 *
 * Here's how to use from within ant:
 * 
 * 
 *		<dgrid-job
 *				transport="thrift"
 *				apiKey="changeme"
 *				endpoint="localhost"
 *				port="9001"
 *				description="test joblet"
 *				callbackType="email"
 *				callbackAddress="sam@stevie.samnbree.net"
 *				callbackContent="Ant callback">
 *			<classpath>
 *				<pathelement path="${build}"/>
 *				<path refid="compile.classpath"/>
 *			</classpath>
 *			<joblet type="system">
 *				<property name="output" value="true"/>
 *				echo -n hello
 *			</joblet>
 *		</dgrid-job>
 *
 */
public class AntJobTask extends Task {

	private String classpathRef;

	private Path classpath;

	private String transportType = "dummy";

	private String apiKey = "123";

	private String endpoint = "localhost";

	private int port = Constants.DEFAULT_PORT;

	private boolean world = false;

	private String description = "";

	private String callbackType = "";

	private String callbackAddress = "";

	private String callbackContent = "";

	private List<AntJobletTask> antJoblets = new ArrayList<AntJobletTask>();

	public AntJobTask() {
	}

	public void execute() throws BuildException {
		System.out.println("execute()");
		try {
			Path classPath = getClasspath();
			if (classPath == null) {
				String cRef = getClasspathRef();
				if (cRef != null) {
					classPath = (Path) getProject().getReference(cRef);
					if (classPath == null) {
						throw new BuildException("The reference " + cRef
								+ " is not set.", getLocation());
					}
				}
			}
			AntClassLoader acl;
			if (classPath == null) {
				acl = null;
			} else {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				if (cl == null) {
					cl = getClass().getClassLoader();
					if (cl == null) {
						cl = ClassLoader.getSystemClassLoader();
					}
				}
				acl = new AntClassLoader(cl, getProject(), classPath, true);
				acl.setThreadContextLoader();
			}
			Job job = getJob();
			DGridTransport transport = getTransport();
			transport.submitJob(job);
		} catch (Exception e) {
			throw (new BuildException(e));
		}
	}

	public void setClasspathRef(String pRef) {
		if (classpath != null) {
			throw new BuildException(
					"The 'classpathRef' attribute and the nested 'classpath' element are mutually exclusive.",
					getLocation());
		}
		classpathRef = pRef;
	}

	public String getClasspathRef() {
		return classpathRef;
	}

	public void addClasspath(Path pClasspath) {
		if (classpath != null) {
			throw new BuildException(
					"Multiple nested 'classpath' elements are forbidden.",
					getLocation());
		}
		if (classpathRef != null) {
			throw new BuildException(
					"The 'classpathRef' attribute and the nested 'classpath' element are mutually exclusive.",
					getLocation());
		}
		classpath = pClasspath;
	}

	public Path getClasspath() {
		return classpath;
	}

	public void addConfiguredJoblet(AntJobletTask joblet) {
		antJoblets.add(joblet);
	}

	public void setWorld(boolean world) {
		this.world = world;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCallbackType(String callbackType) {
		this.callbackType = callbackType;
	}

	public void setCallbackAddress(String callbackAddress) {
		this.callbackAddress = callbackAddress;
	}

	public void setCallbackContent(String callbackContent) {
		this.callbackContent = callbackContent;
	}

	public void setTransport(String type) {
		this.transportType = type;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private DGridTransport getTransport() {
		DGridTransport transport = DGridTransportFactory
				.getTransport(transportType);
		transport.setApiKey(apiKey);
		transport.setEndpoint(endpoint);
		transport.setPort(port);
		return transport;
	}

	private Job getJob() {
		List<Joblet> joblets = new ArrayList<Joblet>(antJoblets.size());
		for (AntJobletTask antJoblet : antJoblets) {
			joblets.add(antJoblet.getJoblet());
		}
		Job job = new Job(0, 0l, "", description, joblets, ApiCallbackTypes
				.getCallbackType(callbackType), callbackAddress,
				callbackContent, JOB_STATUS.RECEIVED);
		return job;
	}
}
