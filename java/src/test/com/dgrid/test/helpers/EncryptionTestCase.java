package com.dgrid.test.helpers;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import junit.framework.TestCase;

import com.dgrid.util.Base64Encoder;
import com.dgrid.util.Encryption;
import com.dgrid.util.io.InputStreamUtils;

public class EncryptionTestCase extends TestCase {

	public void testRSAPK() throws Exception {
		String plaintext = "Hello, world!";
		KeyPair kp = Encryption.generateKeyPair(Encryption.RSA,
				Encryption.DEFAULT_KEY_SIZE);
		String ciphertext = Encryption.encryptStringBase64(plaintext,
				Encryption.RSA_PADDED, kp.getPublic());
		String decrypted = Encryption.decryptStringBase64(ciphertext,
				Encryption.RSA_PADDED, kp.getPrivate());
		assertEquals(decrypted, plaintext);
	}

	public void testDES() throws Exception {
		String plaintext = "Hello, world!";
		Key secret = Encryption.generateSecretKey(Encryption.DES);
		String ciphertext = Encryption.encryptStringBase64(plaintext,
				Encryption.DES, secret);
		String decrypted = Encryption.decryptStringBase64(ciphertext,
				Encryption.DES, secret);
		assertEquals(decrypted, plaintext);

		// test a short string
		plaintext = "1";
		ciphertext = Encryption.encryptStringBase64(plaintext, Encryption.DES,
				secret);
		decrypted = Encryption.decryptStringBase64(ciphertext, Encryption.DES,
				secret);
		assertEquals(decrypted, plaintext);

		// test a long string
		// this file is 44k of base64 encoded data from /dev/urandom
		plaintext = InputStreamUtils
				.getInputStreamAsString(getClass().getResourceAsStream(
						"/com/dgrid/test/resources/long-file.txt"));
		ciphertext = Encryption.encryptStringBase64(plaintext, Encryption.DES,
				secret);
		decrypted = Encryption.decryptStringBase64(ciphertext, Encryption.DES,
				secret);
		assertEquals(decrypted, plaintext);

		// test key serialization
		String keyString = new String(Base64Encoder.base64Encode(secret
				.getEncoded()));
		secret = Encryption.getSecretKey(Encryption.DES, Base64Encoder
				.base64Decode(keyString.getBytes()));
		plaintext = "Callback test contents";
		ciphertext = Encryption.encryptStringBase64(plaintext,
				Encryption.DES_PADDED, secret);
		decrypted = Encryption.decryptStringBase64(ciphertext,
				Encryption.DES_PADDED, secret);
		assertEquals(decrypted, plaintext);
	}

	public void testRSASignature() throws Exception {
		String plaintext = "Hello, world!";
		KeyPair kp = Encryption.generateKeyPair(Encryption.RSA,
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
		KeyPair kp = Encryption.generateKeyPair(Encryption.RSA,
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
