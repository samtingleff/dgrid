package com.dgrid.transport;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.Host;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.InvalidJobId;
import com.dgrid.gen.InvalidJobletId;
import com.dgrid.gen.Job;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.gen.NoHostAvailable;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.service.DGridTransport;
import com.dgrid.util.Base64Encoder;
import com.dgrid.util.Encryption;
import com.dgrid.util.io.InputStreamUtils;
import com.dgrid.util.io.OutputStreamUtils;

public class DGridEncryptedTransport implements DGridTransport {

	private DGridTransport delegate;

	private String desKeyPath;

	private String privateKeyPath;

	private String publicKeyPath;

	private KeyPair kp;

	private Key des;

	public void setDelegate(DGridTransport delegate) {
		this.delegate = delegate;
	}

	public void setApiKey(String apiKey) {
		delegate.setApiKey(apiKey);
	}

	public void setEndpoint(String endpoint) {
		delegate.setEndpoint(endpoint);
	}

	public void setPort(int port) {
		delegate.setPort(port);
	}

	public void setDesKeyPath(String path) {
		this.desKeyPath = path;
	}

	public void setPrivateKey(String path) {
		this.privateKeyPath = path;
	}

	public void setPublicKey(String path) {
		this.publicKeyPath = path;
	}

	public void init() throws Exception {
		Key priv = initRSAKey(privateKeyPath, true);
		Key pub = initRSAKey(publicKeyPath, false);
		this.kp = new KeyPair((PublicKey) pub, (PrivateKey) priv);
		this.des = initDESKey();
	}

	public Host registerHost(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		return delegate.registerHost(hostname);
	}

	public void setHostFacts(int hostid, Map<String, String> facts)
			throws TransportException, InvalidApiKey, InvalidHost {
		delegate.setHostFacts(hostid, facts);
	}

	public Host getHostByName(String hostname) throws TransportException,
			InvalidApiKey, InvalidHost {
		return delegate.getHostByName(hostname);
	}

	public String getHostSetting(int hostid, String name, String defaultValue)
			throws TransportException, InvalidApiKey, InvalidHost {
		return delegate.getHostSetting(hostid, name, defaultValue);
	}

	public String getSetting(String name, String defaultValue)
			throws TransportException, InvalidApiKey {
		return delegate.getSetting(name, defaultValue);
	}

	public void log(int jobletId, int jobletStatus, String message)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		delegate.log(jobletId, jobletStatus, message);
	}

	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
			InvalidJobId {
		return delegate.getJob(jobId);
	}

	public int getJobletQueueSize() throws TransportException, InvalidApiKey {
		return delegate.getJobletQueueSize();
	}

	public JobletResult getJobletResult(int jobletId)
			throws TransportException, InvalidApiKey, InvalidJobletId {
		return delegate.getJobletResult(jobletId);
	}

	public List<JobletResult> getResults(int jobId) throws TransportException,
			InvalidApiKey, InvalidJobId {
		return delegate.getResults(jobId);
	}

	public int submitJob(Job job) throws TransportException, InvalidApiKey {
		List<Joblet> joblets = job.getJoblets();
		for (Joblet joblet : joblets) {
			signJoblet(joblet);
			encryptJoblet(joblet);
		}
		return delegate.submitJob(job);
	}

	public int submitJoblet(Joblet joblet, int jobId, int callbackType,
			String callbackAddress, String callbackContent)
			throws TransportException, InvalidApiKey, InvalidJobId {
		signJoblet(joblet);
		encryptJoblet(joblet);
		return delegate.submitJoblet(joblet, jobId, callbackType,
				callbackAddress, callbackContent);
	}

	public Joblet getWork() throws TransportException, InvalidApiKey,
			InvalidHost, NoWorkAvailable {
		Joblet joblet = delegate.getWork();
		decryptJoblet(joblet);
		if (!verify(joblet))
			throw new RuntimeException(String.format(
					"Could not verify signature on joblet # %1$d", joblet
							.getId()));
		return joblet;
	}

	public void completeJoblet(int jobletId, JobletResult result,
			String logMessage) throws TransportException, InvalidApiKey,
			InvalidJobletId {
		delegate.completeJoblet(jobletId, result, logMessage);
	}

	public JobletResult gridExecute(Joblet joblet, int retries)
			throws InvalidApiKey, TransportException, NoHostAvailable {
		return delegate.gridExecute(joblet, retries);
	}

	public void releaseJoblet(int jobletId) throws InvalidApiKey,
			TransportException, InvalidJobletId {
		delegate.releaseJoblet(jobletId);
	}

	private void signJoblet(Joblet joblet) {
		if ((joblet.getContent() != null) && (joblet.getContent().length() > 0)) {
			String sig = Encryption.signString(joblet.getContent(), kp
					.getPrivate(), Encryption.SHA1withRSA);
			joblet.getParameters().put("signature", sig);
		}
	}

	private void encryptJoblet(Joblet joblet) throws RuntimeException {
		if ((joblet.getContent() != null) && (joblet.getContent().length() > 0)) {
			joblet.setContent(Encryption.encryptStringBase64(joblet
					.getContent(), Encryption.DES_PADDED, des));
		}
		Set<Entry<String, String>> entries = joblet.getParameters().entrySet();
		for (Entry<String, String> entry : entries) {
			String encryptedValue = Encryption.encryptStringBase64(entry
					.getValue(), Encryption.DES_PADDED, des);
			joblet.getParameters().put(entry.getKey(), encryptedValue);
		}
	}

	private void decryptJoblet(Joblet joblet) throws RuntimeException {
		if ((joblet.getContent() != null) && (joblet.getContent().length() > 0)) {
			joblet.setContent(Encryption.decryptStringBase64(joblet
					.getContent(), Encryption.DES_PADDED, des));
		}
		Set<Entry<String, String>> entries = joblet.getParameters().entrySet();
		for (Entry<String, String> entry : entries) {
			String decryptedValue = Encryption.decryptStringBase64(entry
					.getValue(), Encryption.DES_PADDED, des);
			joblet.getParameters().put(entry.getKey(), decryptedValue);
		}
	}

	private boolean verify(Joblet joblet) throws RuntimeException {
		if ((joblet.getContent() == null)
				|| (joblet.getContent().length() == 0))
			return true;
		String sig = joblet.getParameters().get("signature");
		return Encryption.verifyBase64(joblet.getContent(), kp.getPublic(),
				Encryption.SHA1withRSA, sig);
	}

	private Key initRSAKey(String path, boolean isPrivateKey) throws Exception {
		File file = new File(path);
		Key key = null;
		if (file.exists()) {
			String b64Key = InputStreamUtils.getFileAsString(file);
			key = Encryption.getKey(Base64Encoder.base64Decode(b64Key)
					.getBytes(), Encryption.RSA, isPrivateKey);
		} else {
			KeyPair keyPair = Encryption.generateKeyPair(Encryption.RSA, 1024);
			OutputStreamUtils.writeStringToFile(new String(Base64Encoder
					.base64Encode(keyPair.getPrivate().getEncoded())),
					new File(privateKeyPath));
			OutputStreamUtils.writeStringToFile(new String(Base64Encoder
					.base64Encode(keyPair.getPublic().getEncoded())), new File(
					publicKeyPath));
			key = (isPrivateKey) ? keyPair.getPrivate() : keyPair.getPublic();
		}
		return key;
	}

	private Key initDESKey() throws Exception {
		File file = new File(desKeyPath);
		Key key = null;
		if (file.exists()) {
			String b64Key = InputStreamUtils.getFileAsString(file);
			key = Encryption.getSecretKey(Encryption.DES, Base64Encoder
					.base64Decode(b64Key.getBytes()));
		} else {
			key = Encryption.generateSecretKey(Encryption.DES);
			OutputStreamUtils.writeStringToFile(new String(Base64Encoder
					.base64Encode(key.getEncoded())), file);
		}
		return key;
	}
}
