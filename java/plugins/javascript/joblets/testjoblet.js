grid = Packages.com.dgrid.api;
thrift = Packages.com.dgrid.gen;
plugins = Packages.com.dgrid.plugin;

function execute(joblet, gridClient) {
	pluginMgr = gridClient.getBean(plugins.PluginManager.NAME);
	/*
	xmpp = pluginMgr.getPlugin("XmppPlugin");
	xmpp.sendTextMessage("sam@jabber.samnbree.net", "", "Hey dude!");
	*/
	return grid.SimpleJobletResult(0, thrift.JOB_STATUS.COMPLETED,
			joblet.parameters.get("testKey"));
}
