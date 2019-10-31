package cryptography;

import java.util.Base64;

//import javax.xml.bind.DatatypeConverter; // enable this only if java 1.7 is used or lesser

public class CryptUtils {

	public static String Base64EncodeToString(byte[] byteArray) {
		// return DatatypeConverter.printBase64Binary(byteArray); // version =< JAVA 1.7
		return Base64.getEncoder().encodeToString(byteArray); // version >= JAVA 1.8
	}

	public static byte[] Base64DecodeToByteArray(String base64) {
		// return DatatypeConverter.parseBase64Binary(base64); // version =< JAVA 1.7
		return Base64.getDecoder().decode(base64); // version >= JAVA 1.8
	}

	public static String AddPublicPemHeaders(String publicKey) {
		StringBuilder sb = new StringBuilder(publicKey);
		sb.insert(0, "-----BEGIN PUBLIC KEY-----\n");
		sb.append("\n-----END PUBLIC KEY-----");
		return sb.toString().replaceAll("(.{64})", "$1\n");
	}

	public static String RemovePublicPemHeaders(String publicKey) {
		return publicKey.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
	}

}
