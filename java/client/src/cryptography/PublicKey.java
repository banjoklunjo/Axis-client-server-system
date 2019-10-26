package cryptography;

import java.math.BigInteger;

public class PublicKey {
	private BigInteger publicExponent;
	private BigInteger modulus;

	public PublicKey(BigInteger publicExponent, BigInteger modulus) {
		this.publicExponent = publicExponent;
		this.modulus = modulus;
	}

	public BigInteger getPublicExponent() {
		return publicExponent;
	}

	public void setPublicExponent(BigInteger publicExponent) {
		this.publicExponent = publicExponent;
	}

	public BigInteger getModulus() {
		return modulus;
	}

	public void setModulus(BigInteger modulus) {
		this.modulus = modulus;
	}

	public void printKeyValues() {
		System.out.println("[PUBLIC KEY]");
		System.out.println("modulus = " + this.modulus.toString() + "\nexponent = " + this.publicExponent.toString());
	}

}
