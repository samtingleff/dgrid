package com.dgrid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Encryption {
	public static final String RSA = "RSA";

	public static final String DSA = "DSA";

	public static final String SHA1withRSA = "SHA1withRSA";

	public static final int DEFAULT_KEY_SIZE = 1024;

	public static final int PRIVATE = 0;

	public static final int PUBLIC = 1;

	private static final String pbeCipherName = "PBEWithMD5AndDES";

	private static Log log = LogFactory.getLog(Encryption.class);

	private static SecureRandom secureRandom = null;

	private static SecretKeyFactory keyFac = null;

	static {
		try {
			secureRandom = initSecureRandom();
			keyFac = SecretKeyFactory.getInstance(pbeCipherName);
		} catch (Exception e) {
			log.error("Exception initializing encryption provider", e);
		}
	}

	public static KeyPair generateKey(String algorithm, int keySize)
			throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
		kpg.initialize(keySize, secureRandom);
		KeyPair kp = kpg.generateKeyPair();
		return (kp);
	}

	public static byte[] sign(byte[] data, PrivateKey privateKey,
			String algorithm) throws Exception {
		Signature sig = Signature.getInstance(algorithm);
		sig.initSign(privateKey);
		sig.update(data);
		return sig.sign();
	}

	public static String signString(String data, PrivateKey privateKey,
			String algorithm) throws Exception {
		byte[] sigBytes = sign(data.getBytes(), privateKey, algorithm);
		return new String(Base64Encoder.base64Encode(sigBytes));
	}

	public static boolean verify(byte[] data, PublicKey publicKey,
			String algorithm, byte[] signature) throws Exception {
		Signature sig = Signature.getInstance(algorithm);
		sig.initVerify(publicKey);
		sig.update(data);
		return sig.verify(signature);
	}

	public static boolean verifyBase64(String data, PublicKey publicKey,
			String algorithm, String signatureBase64) throws Exception {
		byte[] sig = Base64Encoder.base64Decode(signatureBase64.getBytes());
		byte[] dataBytes = data.getBytes();
		return verify(dataBytes, publicKey, algorithm, sig);
	}

	public static String encryptStringBase64(String plaintext,
			String algorithm, Key publicKey) throws GeneralSecurityException {
		byte[] bytes = encryptString(plaintext, algorithm, publicKey);
		byte[] encoded = Base64Encoder.base64Encode(bytes);
		return (new String(encoded));
	}

	public static byte[] encryptString(String plaintext, String algorithm,
			Key publicKey) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] input = plaintext.getBytes();
		byte[] encryptedBytes = cipher.doFinal(input);
		return encryptedBytes;
	}

	public static String decryptStringBase64(String ciphertext,
			String algorithm, Key privKey) throws GeneralSecurityException {
		byte[] bytes = ciphertext.getBytes();
		byte[] decoded = Base64Encoder.base64Decode(bytes);
		String value = decryptString(decoded, algorithm, privKey);
		return value;
	}

	public static String decryptString(byte[] ciphertext, String algorithm,
			Key privKey) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedBytes = cipher.doFinal(ciphertext);
		return new String(decryptedBytes);
	}

	public static Key getKey(byte[] keyData, String keyType,
			boolean isPrivateKey) throws GeneralSecurityException {
		log.trace("getKey()");
		// keyType should be RSA
		KeyFactory kf = KeyFactory.getInstance(keyType);
		Key key = null;
		if (isPrivateKey) {
			PKCS8EncodedKeySpec encodedPrivateKey = new PKCS8EncodedKeySpec(
					keyData);
			key = kf.generatePrivate(encodedPrivateKey);
		} else {
			X509EncodedKeySpec encodedPublicKey = new X509EncodedKeySpec(
					keyData);
			key = kf.generatePublic(encodedPublicKey);
		}
		return key;
	}

	private static SecureRandom initSecureRandom()
			throws NoSuchAlgorithmException, NoSuchProviderException,
			FileNotFoundException, IOException {
		log.trace("initSecureRandom()");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
		if (new File("/dev/urandom").exists()) {
			log.info("Salting SecureRandom (SHA1PRNG) from /dev/urandom");
			byte[] salt = new byte[8192];
			FileInputStream fis = new FileInputStream("/dev/urandom");
			fis.read(salt);
			fis.close();
			sr.setSeed(salt);
		}
		return sr;
	}
}
