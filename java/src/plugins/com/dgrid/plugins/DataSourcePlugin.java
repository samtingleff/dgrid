package com.dgrid.plugins;

import java.sql.Connection;
import java.sql.DriverManager;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.plugin.BaseDGridPlugin;
import com.dgrid.service.DGridClient;
import com.dgrid.service.DGridPluginContext;

public class DataSourcePlugin extends BaseDGridPlugin {
	private String jdbcDriverClass;

	private String jdbcUrl;

	private String jdbcUsername;

	private String jdbcPassword;

	public String getDescription() {
		return "Provides for data sources";
	}

	public boolean enabledByDefault() {
		return false;
	}

	public void start() {
		log.trace("start()");
		try {
			DGridClient gridClient = ((DGridPluginContext) super.context)
					.getGridClient();
			jdbcDriverClass = gridClient.getSetting("dataSource.driverClass",
					"com.mysql.jdbc.Driver");
			jdbcUrl = gridClient.getSetting("dataSource.url",
					"jdbc:mysql://192.168.1.116/e20");
			jdbcUsername = gridClient.getSetting("dataSource.username", "e20");
			jdbcPassword = gridClient.getSetting("dataSource.password",
					"phpsux");
		} catch (TransportException e) {
			log
					.warn(
							"TransportException in start(). DataSourcePlugin will be disabled",
							e);
			throw (new RuntimeException(e));
		} catch (InvalidApiKey e) {
			log
					.warn(
							"TransportException in start(). DataSourcePlugin will be disabled",
							e);
			throw (new RuntimeException(e));
		} finally {
		}
	}

	public void stop() {
		log.trace("stop()");
	}

	public Connection getConnection() throws Exception {
		return getConnection(jdbcDriverClass, this.jdbcUrl, this.jdbcUsername,
				this.jdbcPassword);
	}

	public Connection getNamedConnection(String name) throws Exception {
		DGridClient gridClient = ((DGridPluginContext) super.context)
				.getGridClient();
		String driverClass = gridClient.getSetting("dataSource." + name
				+ ".driverClass", "com.mysql.jdbc.Driver");
		String url = gridClient.getSetting("dataSource." + name + ".url",
				"jdbc:mysql://192.168.1.116/e20");
		String username = gridClient.getSetting("dataSource." + name
				+ ".username", "e20");
		String password = gridClient.getSetting("dataSource." + name
				+ ".password", "phpsux");
		return getConnection(driverClass, url, username, password);
	}

	public Connection getConnection(String driverClass, String url,
			String username, String password) throws Exception {
		Class.forName(driverClass);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	public void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (Exception e) {
			log.warn("Exception calling close() on jdbc connection", e);
		}
	}
}
