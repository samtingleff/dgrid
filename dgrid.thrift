namespace java com.dgrid.gen

const i32 DEFAULT_PORT = 9001
const i32 SYNC_DEFAULT_PORT = 9002

const string DEFAULT_GROUP = "default"
const string ALL_GROUP = "all"

const string SYSTEM_JOBLET = "system"
const string JAVA_JOBLET = "joblet"
const string JAVA_APP_JOBLET = "java"
const string JAVA_MR_JOB = "mr.java"
const string JAVA_MR_REDUCER = "mr.java.reducer"
const string GROOVY_JOBLET = "groovy"
const string GROOVY_MR_JOB = "mr.groovy"
const string GROOVY_MR_REDUCER = "mr.groovy.reducer"
const string JAVASCRIPT_JOBLET = "javascript"
const string AGENT_SHUTDOWN_JOBLET = "agent.shutdown"
const string AGENT_RESTART_JOBLET = "agent.restart"
const string XMPP_SEND = "xmpp.send"
const string CALLBACK_JOBLET = "callback"

enum JOB_STATUS {
  SAVED = 1,
  RECEIVED = 2,
  QUEUED = 3,
  PROCESSING = 4,
  COMPLETED = 5,
  FAILED = 6
}

enum JOB_CALLBACK_TYPES {
  NONE = 1,
  XMPP = 2,
  HTTP = 3,
  EMAIL = 4,
  JOB = 5
}

exception InvalidApiKey {
}

exception InvalidHost {
}

exception InvalidJobId {
}

exception InvalidJobletId {
}

exception NoWorkAvailable {
}

exception NoHostAvailable {
}

struct Host {
  1: i32 id,
  2: string hostname,
  3: map<string, string> facts
}

struct HostState {
  1: i64 vmUptime,
  2: double loadAverage,
  3: i64 freeMemory,
  4: i64 usedMemory,
  5: i32 activeThreads,
  6: i32 activeTasks
}

struct Joblet {
  1: i32 id,
  2: i64 timeCreated,
  3: i32 jobId,
  4: i32 hostId,
  5: string submitter,
  6: i32 priority,
  7: string jobletType,
  8: string description,
  9: map<string, string> parameters,
  10: string content,
  11: JOB_STATUS status,
}

struct Job {
  1: i32 id,
  2: i64 timeCreated,
  3: string submitter,
  4: string description,
  5: list<Joblet> joblets,
  6: JOB_CALLBACK_TYPES callbackType,
  7: string callbackAddress,
  8: string callbackContent,
  9: JOB_STATUS status
}

struct JobletResult {
  1: i32 id,
  2: i64 timeCreated,
  3: i32 returnCode,
  4: i64 walltime,
  5: JOB_STATUS status,
  6: string details,
  7: Joblet joblet
}

service JobService {

  Host registerHost(1:string apiKey, 2:string hostname)
    throws (1:InvalidApiKey apiKeyException),

  Host getHost(1:string apiKey, 2:string hostname)
    throws (1:InvalidApiKey apiKeyException, 2:InvalidHost hostException),

  void setHostFacts(1:string apiKey, 2:i32 hostid, 3:map<string, string> facts)
    throws (1:InvalidApiKey apiKeyException, 2:InvalidHost hostException),

  string getSetting(1:string apiKey, 2:string name, 3:string defaultValue)
    throws (1:InvalidApiKey apiKeyException),

  string getHostSetting(1:string apiKey, 2:i32 hostid, 3:string name, 4:string defaultValue)
    throws (1:InvalidApiKey apiKeyException, 2:InvalidHost hostException),

  // find a host to execute a joblet immediately
  Host getSyncJobServiceHost(1:string apiKey)
    throws (1:InvalidApiKey apiKeyException, 2:NoHostAvailable hostException),

  // submit a job
  i32 submitJob(1:string apiKey, 2:Job job)
    throws (1:InvalidApiKey apiKeyException),

  // shortcut to submit a single joblet, or append a joblet to an existing job
  i32 submitJoblet(
    1:string apiKey, 2:Joblet joblet, 3:i32 jobId,
    4:JOB_CALLBACK_TYPES callbackType, 5:string callbackAddress,
    6:string callbackContent)
    throws (1:InvalidApiKey apiKeyException, 2:InvalidJobId jobIdException),

  // get a job
  Job getJob(1:string apiKey, 2:i32 jobId)
    throws (1:InvalidApiKey apiKeyException,
      2:InvalidJobId jobIdException),

  // get the result of a joblet
  JobletResult getJobletResult(1:string apiKey, 2:i32 jobletId)
    throws (1:InvalidApiKey apiKeyException,
      2:InvalidJobletId jobletIdException),

  // get all results of a job
  list<JobletResult> getResults(1:string apiKey, 2:i32 jobId)
    throws (1:InvalidApiKey apiKeyException,
      2:InvalidJobId jobletIdException),

  // get work for a host
  Joblet getWork(1:string apiKey, 2:i32 hostid)
    throws (1:InvalidApiKey apiKeyException,
      2:NoWorkAvailable noWorkException,
      3:InvalidHost invalidHostException),

  // complete a joblet
  void completeJoblet(
    1:string apiKey, 2:i32 hostid, 3:i32 jobletId,
    4:JobletResult result, 5:string logMessage)
    throws (1:InvalidApiKey apiKeyException,
      2:InvalidJobletId jobletIdException),

  // un-complete a joblet
  void releaseJoblet(1:string apiKey, 2:i32 hostid,
    3:i32 jobletId)
    throws (1:InvalidApiKey apiKeyException,
      2:InvalidJobletId jobletIdException),

  // get queue size
  i32 getJobletQueueSize(1:string apiKey)
    throws (1:InvalidApiKey apiKeyException),

  // log something
  void log(1:string apiKey, 2:i32 hostid, 3:i32 jobletId,
    4:JOB_STATUS status, 5:string message)
    throws (1:InvalidApiKey apiKeyException,
      2:InvalidJobletId jobletIdException)
}

service SyncJobService {
  // return host status
  HostState status(1:string apiKey)
    throws (1:InvalidApiKey apiKeyException),

  // execute a joblet immediately
  JobletResult execute(1:string apiKey, 2:Joblet joblet)
    throws (1:InvalidApiKey apiKeyException)
}
