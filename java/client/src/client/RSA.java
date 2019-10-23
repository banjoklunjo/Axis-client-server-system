package client;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

/**
 * https://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml

 */
public class RSA {
	private RSAPublicKeySpec publicKeySpec;
	private RSAPrivateKeySpec privateKeySpec;

	RSA(int keySize) {
		KeyPairGenerator keyPairGenerator;

		try {

			// KeyPair is used to generate the public and private key
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(keySize);

			// KeyPair is used to retrieve the public and private key
			KeyPair keyPair = keyPairGenerator.genKeyPair();

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKeySpec = keyFactory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
			privateKeySpec = keyFactory.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);
			
			printKeyInformation();

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

	}
	
	protected RSAPublicKeySpec getPublicKey() {
		return this.publicKeySpec;
	}
	
	protected RSAPrivateKeySpec getPrivateKey() {
		return this.privateKeySpec;
	}

	public void printKeyInformation() {
		System.out.println("[PUBLIC KEY]");
		System.out.println("modulus = " + publicKeySpec.getModulus() 
		+ "\nexponent = " + publicKeySpec.getPublicExponent()
		+ "\nmodulus length = " + publicKeySpec.getModulus().toByteArray().length);

		System.out.println("\n\n[PRIVATE KEY]");
		System.out.println("modulus = " + privateKeySpec.getModulus()
				+ "\nexponent = " + privateKeySpec.getPrivateExponent());
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSA rsa = new RSA(2048);
		System.out.println(Arrays.toString(rsa.getPublicKey().getModulus().toByteArray()));
		rsa.printKeyInformation();
	}
	
}
