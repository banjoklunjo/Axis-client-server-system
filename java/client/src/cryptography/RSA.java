package cryptography;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;


public class RSA {
	private int bitlength = 16;
	private Random r = new Random();

	private PublicKey publicKey;
	private PrivateKey privateKey;

	public RSA() {
		// p and q are prime numbers and are used to calculate N
		BigInteger p = BigInteger.probablePrime(bitlength, r);
		BigInteger q = BigInteger.probablePrime(bitlength, r);

		// calculate the modulus N which will be used in the public and private key
		BigInteger N = p.multiply(q);

		// calculate phi to be able to calculate e and d
		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

		// e is the public exponent and is part of the public key
		BigInteger e = BigInteger.probablePrime(bitlength / 2, r);

		// the greatest common divisor between phi and e must be 1 and e must be smaller
		// than phi
		while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(BigInteger.ONE);
		}

		// d is the private exponent and is part of the private key
		BigInteger d = e.modInverse(phi);

		publicKey = new PublicKey(e, N);
		privateKey = new PrivateKey(d, N);
		
		publicKey.printKeyValues();
		privateKey.printKeyValues();
	}
	
	public byte[] encrypt(byte[] message) {
		return (new BigInteger(message)).modPow(publicKey.getPublicExponent(), publicKey.getModulus()).toByteArray();
	}

	public byte[] decrypt(byte[] message) {
		return (new BigInteger(message)).modPow(privateKey.getPrivatExponent(), privateKey.getModulus()).toByteArray();
	}
	
	public String decrypt(String message, PublicKey key) {
		byte[] decrypted = new BigInteger(message).modPow(this.privateKey.getPrivatExponent(), key.getModulus()).toByteArray();
		return new String(decrypted);
	}
	
	public String encrypt(String message) {
		byte[] encrypted = new BigInteger(message).modPow(this.publicKey.getPublicExponent(), this.publicKey.getModulus()).toByteArray();
		return new String(encrypted);
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}
	
	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public static void main(String[] args) throws IOException {
		RSA rsa = new RSA();

		String message = "123";
		
		// Test 1
		System.out.println("[TEST 1]");
		System.out.println("Encrypting String: " + message);
		byte[] encrypted = rsa.encrypt(message.getBytes("UTF-8"));
		byte[] decrypted = rsa.decrypt(encrypted);
		String decryptedMessage = new String(decrypted);
		System.out.println("Decrypted String: " + decryptedMessage);
		
		// Test 2
		System.out.println("\n[TEST 2]");
		System.out.println("Encrypting String: " + message);
		String encryptedString = rsa.encrypt(message);
		System.out.println("Encrypted String: " + encryptedString);
		String decryptedString = rsa.decrypt(encryptedString, rsa.getPublicKey());
		String decryptedMessage2 = new String(decryptedString);
		System.out.println("Decrypted String: " + decryptedMessage2);
		
	
	}

}