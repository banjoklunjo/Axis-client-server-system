package cryptography;

import java.io.IOException;

public class XorCipher {
	private String key;
	private int keyLength;

	public XorCipher(String key) {
		if (key != null && !key.isEmpty()) {
			this.key = key;
			this.keyLength = key.length();
			System.out.println("XOR key = " + this.key);
		} else {
			System.out.println("XOR KEY IS NULL OR EMPTY");
		}
	}

	public String xorMessage(String message) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			sb.append((char) (message.charAt(i) ^ (key.charAt(i % keyLength))));
		}
		return sb.toString();
	}

	public byte[] xorImage(byte[] image) {
		for (int i = 0; i < image.length; i++) {
			image[i] = (byte) (image[i] ^ (key.charAt(i % keyLength)));
		}
		return image;
	}
		
	
	public static void main(String[] args) throws IOException {
		XorCipher xorCipher = new XorCipher("0123456789");

		String message = "640×480, 800×600, 960×720, 1024×768, 1280×960, 1400×1050, 1440×1080, 1600×1200, 1856×1392, 1920×1440, and 2048×1536";

		String encryptedMessage = xorCipher.xorMessage(message);
		System.out.println("encryptedMessage = " + encryptedMessage);

		String decryptedMessage = xorCipher.xorMessage(encryptedMessage);
		System.out.println("decryptedMessage = " + decryptedMessage);

	}

}
