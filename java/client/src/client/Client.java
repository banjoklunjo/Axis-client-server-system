package client;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import cryptography.RSA;
import cryptography.XorCipher;

public class Client implements Runnable {
	private JFrame frame;
	private Controller controller;

	// image size
	private int realSize = 0;

	// flag to control while-loop
	private boolean online;

	// socket used for server communication
	private Socket socket;

	// reads bytes and decodes them into characters
	private BufferedReader bufferedReader;

	// representing an input stream of bytes
	private InputStream inputStream;

	// representing an output stream of bytes
	private OutputStream outputStream;

	// write unicode characters over the socket
	private PrintWriter printWriter;

	// encryption/decryption of messages to/from server
	private XorCipher xorCipher;

	// generate private key and public key
	private RSA rsa;

	public Client(Controller controller, Socket socket) {
		this.controller = controller;
		this.socket = socket;
		this.rsa = new RSA();
	}

	@Override
	public void run() {

		initializeStreams();

		//sendPublicKey();

		//readXorKey();

		//readXorEncryptedMessage();
		
		setCameraResolutions();
		
		while(online) {
			readServerImage();
		}

		close();
	}

	private void sendPublicKey() {
		String n = rsa.getModulus().toString();
		String e = rsa.getE().toString();
		sendToServer(n);
		sendToServer(e);
	}
	
	private void readXorKey() {
		String encryptedXorKey = readServerMessage();
		System.out.println("RSA Encrypted XOR Key: " + encryptedXorKey);
		
		byte[] decryptedXorKey = rsa.decrypt(encryptedXorKey.getBytes());
		xorCipher = new XorCipher(new String(decryptedXorKey));
	}

	private void readXorEncryptedMessage() {
		String xorEncryptedMessage = readServerMessage();
		System.out.println("XOR Encrypted Message: " + xorEncryptedMessage);
		
		String xorDecryptedMessage = xorCipher.xorMessage(xorEncryptedMessage);
		System.out.println("XOR Decrypted Message: " + xorDecryptedMessage);
	}

	private void readServerImage() {

		// for first time start
		if (frame == null) {
			frame = new JFrame();
			// else for the second image, third, fourth and so on
			// remove everything old to show new image
		} else
			frame.getContentPane().removeAll();
		// now we try
		try {

			int length = inputStream.available();

			byte[] message = new byte[length];

			String s = "";

			// if the message is so small, it means we are receiving the size
			// first
			if (length > 2 && length < 20) {
				message = readExactly(inputStream, length);

				s = new String(message);

				// here we receive the size as a message
				try {
					realSize = Integer.valueOf(s.trim());
				} catch (NumberFormatException e) {
					System.out.println(e.toString());
				}

			}

			// else if the message is bigger then we are receiving the image
			else if (length > 20) {
				message = new byte[realSize];
				// using bufferedinput for smoother reading of the byte array
				BufferedInputStream stream = new BufferedInputStream(inputStream);
				// with the bufferedinputstream we can read each byte
				for (int read = 0; read < realSize;) {
					read += stream.read(message, read, message.length - read);
				}
				// using bytearrayinputstream for reading images
				ByteArrayInputStream bais = new ByteArrayInputStream(message);
				// from above bytearrayinputstream we have bufferedImage
				final BufferedImage bufferedImage = ImageIO.read(bais);
				// if the buffered image is not null, we will show it.
				if (bufferedImage != null) {
					frame.getContentPane().setLayout(new FlowLayout());
					frame.getContentPane().add(new JLabel(new ImageIcon(bufferedImage)));
					frame.pack();
					frame.setVisible(true);
				}
			}

		} catch (IOException e) {
			System.out.println("readServerImage() --> Error = " + e.getMessage());
			e.printStackTrace();
		}
	}

	public byte[] readExactly(InputStream input, int size) throws IOException {
		byte[] data = new byte[size];
		int index = 0;
		while (index < size) {
			int bytesRead = input.read(data, index, size - index);
			System.out.println("Bytes read = " + String.valueOf(bytesRead));
			if (bytesRead < 0) {
				throw new IOException("Insufficient data in stream");
			}
			index += size;
		}
		return data;
	}

	public void sendToServer(String message) {
		printWriter.println(message);
		printWriter.flush();
		System.out.println("sendToServer -> " + message);
	}

	private String readServerMessage() {
		String message = null;
		try {
			message = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	}

	private void setCameraResolutions() {
		List<String> resolutions = getListResolutions();
		if (resolutions != null)
			controller.setResolutions(resolutions);
	}

	private List<String> getListResolutions() {
		String cameraResolutions = readServerMessage();
		List<String> listOfResolutions = null;
		if (cameraResolutions != null)
			listOfResolutions = Arrays.asList(cameraResolutions.split(","));
		return listOfResolutions;
	}

	private void initializeStreams() {
		online = true;
		try {
			inputStream = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new DataOutputStream(socket.getOutputStream());
			printWriter = new PrintWriter(socket.getOutputStream());
			System.out.println("initializeStreams() --> Success.");
		} catch (IOException e1) {
			System.out.println("initializeStreams() --> IOException.");
			e1.printStackTrace();
			online = false;
			close();
		}
	}

	public void disconnect() {
		this.online = false;
	}

	private void close() {
		try {
			inputStream.close();
			outputStream.close();
			socket.close();
			bufferedReader.close();
			printWriter.close();
			controller.onDisconnect();
			System.out.println("Socket closed.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}