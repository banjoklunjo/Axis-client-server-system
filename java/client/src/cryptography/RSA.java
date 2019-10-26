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

	public PublicKey getPublicKey() {
		return this.publicKey;
	}
	
	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public static void main(String[] args) throws IOException {
		RSA rsa = new RSA();

		String message = "640×480, 800×600, 960×720, 1024×768, 1280×960, 1400×1050, 1440×1080, 1600×1200, 1856×1392, 1920×1440, and 2048×1536";
		
		System.out.println("Encrypting String: " + message);

		byte[] encrypted = rsa.encrypt(message.getBytes());
		byte[] decrypted = rsa.decrypt(encrypted);

		System.out.println("Decrypted String: " + new String(decrypted));
	}

}