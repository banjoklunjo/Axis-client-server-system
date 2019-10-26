package cryptography;

import java.math.BigInteger;

public class PrivateKey {
	private BigInteger privatExponent;
	private BigInteger modulus;

	public PrivateKey(BigInteger privatExponent, BigInteger modulus) {
		this.privatExponent = privatExponent;
		this.modulus = modulus;
	}

	public BigInteger getPrivatExponent() {
		return privatExponent;
	}

	public void setPrivatExponent(BigInteger privatExponent) {
		this.privatExponent = privatExponent;
	}

	public BigInteger getModulus() {
		return modulus;
	}

	public void setModulus(BigInteger modulus) {
		this.modulus = modulus;
	}

	public void printKeyValues() {
		System.out.println("[PRIVATE KEY]");
		System.out.println("modulus = " + this.modulus.toString() + "\nexponent = " + this.privatExponent.toString());
	}

}
