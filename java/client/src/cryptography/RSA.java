package cryptography;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class RSA {
	private int bitlength = 8;
	private Random r = new Random();

	// p and q are primer numbers used to calculate N
	private BigInteger p;
	private BigInteger q;

	// N is the modulus and is part of the public key and private key
	private BigInteger n;
	private BigInteger phi;

	// e is the public exponent and is part of the public key
	private BigInteger e;

	// d is the private exponent and is part of the private key
	private BigInteger d;

	public RSA() {
		p = BigInteger.probablePrime(bitlength, r);
		q = BigInteger.probablePrime(bitlength, r);

		n = p.multiply(q);

		phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

		e = BigInteger.probablePrime(bitlength / 2, r);

		while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(BigInteger.ONE);
		}
		d = e.modInverse(phi);
	}

	public byte[] encrypt(byte[] message) {
		return (new BigInteger(message)).modPow(e, n).toByteArray();
	}

	public byte[] decrypt(byte[] message) {
		return (new BigInteger(message)).modPow(d, n).toByteArray();
	}

	public BigInteger getModulus() {
		return this.n;
	}
	
	public BigInteger getE() {
		return this.e;
	}

	
	public static void main(String[] args) throws IOException {
		RSA rsa = new RSA();
		rsa.printKeyInformation();

		String message = "105";

		System.out.println("Encrypting String: " + message);

		byte[] encrypted = rsa.encrypt(message.getBytes());
		byte[] decrypted = rsa.decrypt(encrypted);

		System.out.println("Decrypted String: " + new String(decrypted));
	}

	
	public void printKeyInformation() {
		System.out.println("[PUBLIC KEY]");
		System.out
				.println("modulus = " + n.toString() + "\nexponent = "
						+ e.toString() + "\nmodulus length = "
						+ n.toByteArray().length);

		System.out.println("\n\n[PRIVATE KEY]");
		System.out.println("modulus = " + n.toString() + "\nexponent = "
				+ d.toString());
	}

}
