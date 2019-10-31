package cryptography;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class TestRSA {

	public static void EncryptMessageAndSendToServer(PrintWriter printWriter, RSA rsa) {
		String message = "640x480&10";
		String encryptedMessage = "";
		try {
			encryptedMessage = RSA.encrypt(message, rsa.getServerPublicKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
		printWriter.println(encryptedMessage);
		printWriter.flush();
		System.out.println("sendToServer -> " + encryptedMessage);
	}

	public static void DecryptMessageFromServer(BufferedReader bufferedReader, RSA rsa) {
		try {
			String encryptedMessage = bufferedReader.readLine();
			System.out.println("Received Encrypted Message From Server: " + encryptedMessage);
			String decrypted = RSA.decrypt(encryptedMessage, rsa.getPrivateKey());
			System.out.println("Decrypted Message From Server: " + decrypted);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}
