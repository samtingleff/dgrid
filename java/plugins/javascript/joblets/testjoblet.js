grid = Packages.com.dgrid.api;
thrift = Packages.com.dgrid.gen;
plugins = Packages.com.dgrid.plugin;

function execute(joblet, gridClient) {
	var x = parseInt(joblet.parameters.get('x'));
	var y = parseInt(joblet.parameters.get('y'));
	var result = x + y;
	return grid.SimpleJobletResult(0, thrift.JOB_STATUS.COMPLETED,
			result.toString());
}
