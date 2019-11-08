package cryptography;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public class RSA {
	private int bitlength = 5;
	private Random r = new Random();

	// p and q are primer numbers used to calculate N
	private BigInteger p;
	private BigInteger q;

	// N is the modulus and is part of the public key and private key
	private BigInteger n;
	private BigInteger phi;

	// e is the public exponent and is part of the public key
	private BigInteger e;
	int ee;
	int nn;
	int dd=0;

	// d is the private exponent and is part of the private key
	private BigInteger d;
	
	private int[] primeValues1 = {11, 13, 17, 19, 23};


	public RSA() {
		p = BigInteger.probablePrime(bitlength, r);
	    q = BigInteger.probablePrime(bitlength, r);
		
		//--nytt
		int qq = primeValues1[r.nextInt(4)];
		int pp = primeValues1[r.nextInt(4)];
		
		while(pp == qq){
			pp = primeValues1[r.nextInt(4)];
		}
		//--till hit


		

		
		n = p.multiply(q);
		
		
		//--nytt
		nn= (pp*qq);
		System.out.println("n: " + nn);

		
		int z = (qq-1)*(pp-1);
		for(ee = 2; ee < nn; ee++){
			if(genE(ee, z)==1)
				break;
		}
		
		while(true){
			if(( (ee * dd) % z ) == 1 )
				break;
			dd++;
		}
		n = new BigDecimal(nn).toBigInteger();
		e = new BigDecimal(ee).toBigInteger();
		d = new BigDecimal(dd).toBigInteger();
		//--till hit
		
		
		/*
		phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

		e = BigInteger.probablePrime(bitlength / 2, r);

		while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(BigInteger.ONE);
		}
		d = e.modInverse(phi);
		*/
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
	
	public BigInteger RSADecrypt(int key) {
		return new BigDecimal(key).toBigInteger().modPow(
				new BigDecimal(d).toBigInteger(),
				new BigDecimal(n).toBigInteger());
}

	
	public static void main(String[] args) throws IOException {
		RSA rsa = new RSA();
		rsa.printKeyInformation();

		String message = "235";

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
	
	
	private int genE(int e, int z){
		if(e == 0)
			return z;
		else
			return genE(z % e, e);
	}

}
