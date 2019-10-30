package cryptography;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.xml.bind.DatatypeConverter;


import javax.crypto.Cipher;

public class RSA {
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public RSA(int keyLength) {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyPairGenerator.initialize(keyLength);
		KeyPair keypair = keyPairGenerator.genKeyPair();
		privateKey = keypair.getPrivate();
		publicKey = keypair.getPublic();
	}

	public static String decrypt(String message, PrivateKey privateKey) throws Exception {
		byte[] bytes = DatatypeConverter.parseBase64Binary(message);
		Cipher decriptCipher = Cipher.getInstance("RSA");
		decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8);
	}

	public static String encrypt(String message, PublicKey publicKey) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] cipherText = encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
		return DatatypeConverter.printBase64Binary(cipherText).toString();
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void loadPKCS1RSAPublicKey(String publicKeyBase64Format) {
		System.out.println("loadPKCS1RSAPublicKey start");
		byte[] base64KeyInBytes = DatatypeConverter.parseBase64Binary(publicKeyBase64Format);
		System.out.println("base64KeyInBytes");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(base64KeyInBytes);
		System.out.println("publicKeySpec");
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			System.out.println("keyFactory init done");
			RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
			System.out.println("pubKey.getModulus().toString()");
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		RSA rsa = new RSA(1024);

		// decode the base64 to bytes
		DatatypeConverter.parseBase64Binary("hej");

		System.out.println("Public Key Format: " + rsa.getPublicKey().getFormat());
		System.out.println("Public Key Algortihm: " + rsa.getPublicKey().getAlgorithm());
		System.out.println("Private key Format: " + rsa.getPrivateKey().getFormat());
		System.out.println("Private key algortihm: " + rsa.getPrivateKey().getAlgorithm());

		System.out.println(
				"Private Key (encoded with Base64): " + DatatypeConverter.printBase64Binary(rsa.getPrivateKey().getEncoded()));

		
		// secret message
		String message = "the answer to life the universe and everything";

		// Encrypt the message
		String cipherText = encrypt(message, rsa.getPublicKey());
		
		System.out.println("cipherText = " + cipherText);

		// Now decrypt it
		String decipheredMessage = decrypt(cipherText, rsa.getPrivateKey());

		System.out.println("decipheredMessage = " + decipheredMessage);
	}

}
