package cryptography;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSA {
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private PublicKey publicKeyServer;

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
		byte[] bytes = CryptUtils.Base64DecodeToByteArray(message);
		Cipher decriptCipher = Cipher.getInstance("RSA"); // Cipher.getInstance("RSA/None/PKCS1Padding", "BC")
		decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8);
	}

	public static String encrypt(String message, PublicKey publicKey) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] cipherText = encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
		return CryptUtils.Base64EncodeToString(cipherText);
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}
	
	public PublicKey getServerPublicKey() {
		return this.publicKeyServer;
	}

	public void loadPKCS1RSAPublicKey(String publicKeyBase64Format) {
		byte[] base64KeyInBytes = CryptUtils.Base64DecodeToByteArray(publicKeyBase64Format);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(base64KeyInBytes);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKeyServer = keyFactory.generatePublic(publicKeySpec);
			System.out.println("loadPKCS1RSAPublicKey() -> Server Public Key (Base64): "
					+ CryptUtils.AddPublicPemHeaders(CryptUtils.Base64EncodeToString(publicKeyServer.getEncoded())));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public void printInformation() {
		System.out.println("Public Key Format: " + publicKey.getFormat());
		System.out.println("Public Key Algortihm: " + publicKey.getAlgorithm());
		System.out.println("Private key Format: " + privateKey.getFormat());
		System.out.println("Private key algortihm: " + privateKey.getAlgorithm());

		System.out.println(
				"Public Key (encoded with Base64): " + CryptUtils.Base64EncodeToString(publicKey.getEncoded()));

		System.out.println(
				"Private Key (encoded with Base64): " + CryptUtils.Base64EncodeToString(privateKey.getEncoded()));
	}

	public static void main(String[] args) throws Exception {
		RSA rsa = new RSA(1024);
		rsa.printInformation();

		String cipherText = encrypt("the answer to life the universe and everything", rsa.getPublicKey());
		System.out.println("cipherText = " + cipherText);
		String decipheredMessage = decrypt(cipherText, rsa.getPrivateKey());
		System.out.println("decipheredMessage = " + decipheredMessage);
	}

}
