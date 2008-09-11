# dgrid #

## Introduction ##

dgrid is a Java-based worker agent with a plugin system for extensibility. If you have long-running jobs spawned from a web page, an ant task, cron, or whatever... just send it to dgrid and let it run on your compute grid.

dgrid has a serverless mode, where jobs are stored in a relational database, and a client-server mode using a thrift-based protocol. TODO: write that server.

Jobs consist of one or more joblets. Joblets may be system executables (bash/perl/whatever), Java classes, Junit test cases, Groovy classes or Javascript. There are a number of provided joblets for things like uploading/downloading files to/from Amazon S3, sending SQS messages, sending email and sending xmpp messages. These are network-dependent and time consuming tasks that do not need to occur while responding to an http request. See the plugins/groovy/joblets directory for several samples.

## Example ##

Let's look at plugins/groovy/joblets/S3PutJoblet.groovy. First notice that it implements com.dgrid.api.SimpleJoblet, which Java-based joblets should implement as well. This interface defines one method:

  public SimpleJobletResult execute(Joblet joblet, DGridClient gridClient) throws Exception;

Here's the implementation:

		// parameters are name/value pairs passed in
		Map<String, String> params = joblet.parameters;

		// this joblet requires four parameters and has two optional parameters
		String bucket = params.get("bucket");
		String key = params.get("key");
		String contentType = params.get("contentType");
		String filename = params.get("file");

		// these are optional and assumed false by Boolean.parseBoolean() behavior
		boolean isPublic = Boolean.parseBoolean(params.get("public"));
		boolean deleteAfterPut = Boolean.parseBoolean(params.get("delete"));

		File file = new File(filename);

		// validate required params
		assert bucket != null, "Bucket may not be null";
		assert key != null, "Key may not be null";
		assert contentType != null, "ContentType may not be null";
		assert file.canRead(), "Cannot read file ${filename}";

		// use the S3Helper class to do actual work
		S3Helper s3 = (S3Helper) gridClient.getBean(S3Helper.NAME);
		String url = s3.put(file, bucket, key, contentType, isPublic);
		if (deleteAfterPut) {
			file.delete();
		}
		// return a result with 1) a return code to be used for anything you like,
		//  2) a status code indicating success or failure, and 3) a plain text
		//  field also to be used for anything (in this case a url for the file on S3.
		return new SimpleJobletResult(0, JOB_STATUS.COMPLETED, url);

We can insert this joblet into the system using dshell.jar:

  java -jar dshell.jar --type groovy --param script:S3PutJoblet.groovy --param bucket:mybucket --param key:motd.txt --param contentType:text/plain --param file:/etc/motd --param public:true

