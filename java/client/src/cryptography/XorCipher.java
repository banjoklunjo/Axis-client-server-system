package cryptography;

public class XorCipher {
	private String key;
	private int keyLength;

	public XorCipher(String key) {
		if (key != null && !key.isEmpty()) {
			this.key = key;
			this.keyLength = key.length();
			System.out.println("Xor key: " + this.key + ", Xor key length1: " + this.keyLength);
		} else {
			System.out.println("********* XOR KEY IS NULL OR EMPTY *********");
		}
	}

	public String xorMessage(String message) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			output.append((char) (message.charAt(i) ^ key.charAt(i % keyLength)-48));
		}
		return output.toString();
	}

	public byte[] xorImage(byte[] image) {
		for (int i = 0; i < image.length; i++) {
			image[i] = ( (byte) (image[i] ^ key.charAt(i % keyLength) - 48));
		}
		return image;
	}

}
