package com.dgrid.groovy.plugins

import com.dgrid.plugin.BaseDGridPlugin

class GroovyTest extends BaseDGridPlugin {
	def String getDescription() {
		"Groovy test plugin";
	}

	def void start() {
		log.trace("start()")
	}

	def void stop() {
		log.trace("stop()")
	}

	public boolean enabledByDefault() {
		return false;
	}
}