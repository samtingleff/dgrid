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
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Encryption {

	public static final String RSA = "RSA";

	public static final String RSA_PADDED = "RSA/NONE/OAEPWithSHA1AndMGF1Padding";

	public static final String DSA = "DSA";

	public static final String DES = "DES";

	public static final String DES_PADDED = "DES/ECB/PKCS5Padding";

	public static final String DH = "DH";

	public static final String SHA1withRSA = "SHA1withRSA";

	public static final int DEFAULT_KEY_SIZE = 1024;

	public static final int PRIVATE = 0;

	public static final int PUBLIC = 1;

	private static final String pbeCipherName = "PBEWithMD5AndDES";

	private static Log log = LogFactory.getLog(Encryption.class);

	private static SecureRandom secureRandom = null;

	static {
		try {
			secureRandom = initSecureRandom();
		} catch (Exception e) {
			log.error("Exception initializing encryption provider", e);
		}
	}

	public static KeyPair generateKeyPair(String algorithm, int keySize)
			throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
		kpg.initialize(keySize, secureRandom);
		KeyPair kp = kpg.generateKeyPair();
		return (kp);
	}

	public static Key generateSecretKey(String algorithm)
			throws NoSuchAlgorithmException {
		KeyGenerator kg = KeyGenerator.getInstance(algorithm);
		Key secret = kg.generateKey();
		return secret;
	}

	public static byte[] sign(byte[] data, PrivateKey privateKey,
			String algorithm) throws RuntimeException {
		try {
			Signature sig = Signature.getInstance(algorithm);
			sig.initSign(privateKey);
			sig.update(data);
			return sig.sign();
		} catch (Exception e) {
			log.error("Exception in sign()", e);
			throw new RuntimeException(e);
		}
	}

	public static String signString(String data, PrivateKey privateKey,
			String algorithm) throws RuntimeException {
		byte[] sigBytes = sign(data.getBytes(), privateKey, algorithm);
		return new String(Base64Encoder.base64Encode(sigBytes));
	}

	public static boolean verify(byte[] data, PublicKey publicKey,
			String algorithm, byte[] signature) throws RuntimeException {
		try {
			Signature sig = Signature.getInstance(algorithm);
			sig.initVerify(publicKey);
			sig.update(data);
			return sig.verify(signature);
		} catch (Exception e) {
			log.error("Exception in verify()", e);
			throw new RuntimeException(e);
		}
	}

	public static boolean verifyBase64(String data, PublicKey publicKey,
			String algorithm, String signatureBase64) throws RuntimeException {
		byte[] sig = Base64Encoder.base64Decode(signatureBase64.getBytes());
		byte[] dataBytes = data.getBytes();
		return verify(dataBytes, publicKey, algorithm, sig);
	}

	public static String encryptStringBase64(String plaintext,
			String algorithm, Key publicKey) throws RuntimeException {
		byte[] bytes = encryptString(plaintext, algorithm, publicKey);
		byte[] encoded = Base64Encoder.base64Encode(bytes);
		return (new String(encoded));
	}

	public static byte[] encryptString(String plaintext, String algorithm,
			Key publicKey) throws RuntimeException {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] input = plaintext.getBytes();
			byte[] encryptedBytes = cipher.doFinal(input);
			return encryptedBytes;
		} catch (GeneralSecurityException e) {
			log.error("GeneralSecurityException in encryptString()", e);
			throw new RuntimeException(e);
		}
	}

	public static String decryptStringBase64(String ciphertext,
			String algorithm, Key privKey) throws RuntimeException {
		try {
			byte[] bytes = ciphertext.getBytes();
			byte[] decoded = Base64Encoder.base64Decode(bytes);
			String value = decryptString(decoded, algorithm, privKey);
			return value;
		} catch (Exception e) {
			log.error("Exception in encryptString()", e);
			throw new RuntimeException(e);
		}
	}

	public static String decryptString(byte[] ciphertext, String algorithm,
			Key privKey) throws RuntimeException {
		try {
			Cipher cipher = Cipher.getInstance(algorithm, "BC");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] decryptedBytes = cipher.doFinal(ciphertext);
			return new String(decryptedBytes);
		} catch (Exception e) {
			log.error("Exception in encryptString()", e);
			throw new RuntimeException(e);
		}
	}

	public static Key getKey(byte[] keyData, String keyType,
			boolean isPrivateKey) throws RuntimeException {
		log.trace("getKey()");
		try {
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
		} catch (Exception e) {
			log.error("Exception in encryptString()", e);
			throw new RuntimeException(e);
		}
	}

	public static Key getSecretKey(String algorithm, byte[] keyData)
			throws Exception {
		log.trace("getSecretKey()");
		SecretKeySpec keySpec = new SecretKeySpec(keyData, algorithm);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
		return keyFactory.generateSecret(keySpec);
	}

	private static SecureRandom initSecureRandom()
			throws NoSuchAlgorithmException, NoSuchProviderException,
			FileNotFoundException, IOException {
		log.trace("initSecureRandom()");
		Security
				.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
