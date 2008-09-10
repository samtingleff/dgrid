package com.dgrid.test.helpers;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.dgrid.util.Base64Encoder;
import com.dgrid.util.Encryption;

import junit.framework.TestCase;

public class EncryptionTestCase extends TestCase {

	public void testRSAPK() throws Exception {
		String plaintext = "Hello, world!";
		KeyPair kp = Encryption.generateKey(Encryption.RSA,
				Encryption.DEFAULT_KEY_SIZE);
		String ciphertext = Encryption.encryptStringBase64(plaintext,
				Encryption.RSA, kp.getPublic());
		String decrypted = Encryption.decryptStringBase64(ciphertext,
				Encryption.RSA, kp.getPrivate());
		assertEquals(decrypted, plaintext);
	}

	public void testRSASignature() throws Exception {
		String plaintext = "Hello, world!";
		KeyPair kp = Encryption.generateKey(Encryption.RSA,
				Encryption.DEFAULT_KEY_SIZE);
		byte[] sig = Encryption.sign(plaintext.getBytes(), kp.getPrivate(),
				Encryption.SHA1withRSA);
		boolean valid = Encryption.verify(plaintext.getBytes(), kp.getPublic(),
				Encryption.SHA1withRSA, sig);
		assertTrue(valid);
		String b64sig = Encryption.signString(plaintext, kp.getPrivate(),
				Encryption.SHA1withRSA);
		valid = Encryption.verifyBase64(plaintext, kp.getPublic(),
				Encryption.SHA1withRSA, b64sig);
		assertTrue(valid);
	}

	public void testKeySerializer() throws Exception {
		String plaintext = "Hello, world!";
		KeyPair kp = Encryption.generateKey(Encryption.RSA,
				Encryption.DEFAULT_KEY_SIZE);
		String b64privateKey = new String(Base64Encoder.base64Encode(kp
				.getPrivate().getEncoded()));
		String b64publicKey = new String(Base64Encoder.base64Encode(kp
				.getPublic().getEncoded()));

		PrivateKey priv = (PrivateKey) Encryption.getKey(Base64Encoder
				.base64Decode(b64privateKey.getBytes()), Encryption.RSA, true);
		PublicKey pub = (PublicKey) Encryption.getKey(Base64Encoder
				.base64Decode(b64publicKey.getBytes()), Encryption.RSA, false);
		// sign it
		String b64sig = Encryption.signString(plaintext, priv,
				Encryption.SHA1withRSA);
		boolean valid = Encryption.verifyBase64(plaintext, pub,
				Encryption.SHA1withRSA, b64sig);
		assertTrue(valid);
	}
}
