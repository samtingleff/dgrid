package com.dgrid.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.taskdefs.Property;

import com.dgrid.gen.JOB_STATUS;
import com.dgrid.gen.Joblet;

public class AntJobletTask {
	private int priority = 1;

	private String type = null;

	private String description = "";

	private List<Property> properties = new ArrayList<Property>();

	private String content;

	public Joblet getJoblet() {
		return new Joblet(0, 0l, 0, 0, "", priority, type, description,
				getParameters(), content, JOB_STATUS.RECEIVED);
	}

	public void addConfiguredProperty(Property property) {
		properties.add(property);
	}

	public void addText(String content) {
		this.content = content;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private Map<String, String> getParameters() {
		Map<String, String> params = new HashMap<String, String>(properties
				.size());
		for (Property prop : properties) {
			params.put(prop.getName(), prop.getValue());
		}
		return params;
	}
}
