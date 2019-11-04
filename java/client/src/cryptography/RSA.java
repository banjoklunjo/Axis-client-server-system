package cryptography;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class RSA {
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public RSA(int keyLength) {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(keyLength);
			KeyPair keypair = keyPairGenerator.genKeyPair();
			privateKey = keypair.getPrivate();
			publicKey = keypair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("********* FAILED TO INITALIZE RSA KEY *********");
			e.printStackTrace();
		}
	}

	public static String decrypt(String message, PrivateKey privateKey) {
		byte[] bytes = CryptUtils.Base64DecodeToByteArray(message);
		Cipher decriptCipher;
		try {
			decriptCipher = Cipher.getInstance("RSA");
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			return new String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String encrypt(String message, PublicKey publicKey) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] cipherText = encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
		return CryptUtils.EncodeToBase64(cipherText);
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public void printInformation() {
		System.out.println("Public Key Format: " + publicKey.getFormat());
		System.out.println("Public Key Algortihm: " + publicKey.getAlgorithm());
		System.out.println("Private key Format: " + privateKey.getFormat());
		System.out.println("Private key algortihm: " + privateKey.getAlgorithm());
		System.out.println("Public Key (encoded with Base64): " + CryptUtils.EncodeToBase64(publicKey.getEncoded()));
		System.out.println("Private Key (encoded with Base64): " + CryptUtils.EncodeToBase64(privateKey.getEncoded()));
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
