package client;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Client implements Runnable {
	private Controller controller;
	private JFrame frame;
	private boolean online;
	private int realSize = 0;
	private Socket socket;

	// reads bytes and decodes them into characters
	private BufferedReader bufferedReader;

	// representing an input stream of bytes
	private InputStream inputStream;

	// representing an output stream of bytes
	private OutputStream outputStream;

	// generate public and private key with this class
	private RSA rsa;

	// key size in bits
	private int keySize = 2048;

	public Client(Controller controller, Socket socket) {
		this.controller = controller;
		this.socket = socket;
		this.rsa = new RSA(keySize);

		try {
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendToServer(String message) {
		PrintWriter output;
		try {
			output = new PrintWriter(socket.getOutputStream());
			output.println(message);
			output.flush();
			System.out.println("message has been sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		init();
		/*
		 * String msgFromServer = readServerMessage();
		 * 
		 * if (msgFromServer != null) { List<String> resolutions =
		 * Arrays.asList(msgFromServer.split(",")); if (!msgFromServer.equals("\n")) {
		 * controller.receivedMessage(msgFromServer);
		 * controller.setResolutions(resolutions); } }
		 */

		sendPublicKey();

		while (online) {
			// readServerImage();
		}
	}

	/**
	 * The public key is made of the modulus and the public exponent
	 */
	private void sendPublicKey() {
		try {
			// BigInteger is used because of the big integer calculations which exceeds the limit of the primitive data types.
			// The RSA keys contains a lot of digits.
			BigInteger publicKeyModulus = rsa.getPublicKey().getModulus();
			BigInteger publicKeyExponent = rsa.getPublicKey().getPublicExponent();

			outputStream.write(publicKeyModulus.toByteArray());  // send first part of the public key as a byte array
			outputStream.write(publicKeyExponent.toByteArray()); // send second part of the public key as a byte array
		} catch (IOException e) {
			e.printStackTrace();
		}
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

			// if the message is so small, it means we are receiving the size first
			if (length > 2 && length < 20) {
				message = readExactly(inputStream, length);

				s = new String(message);
				;

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
			if (bytesRead < 0) {
				throw new IOException("Insufficient data in stream");
			}
			index += size;
		}
		return data;
	}

	private void init() {
		online = true;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
			close();
		}
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

	public void disconnect() {
		this.online = false;
	}

	private void close() {
		try {
			outputStream.close();
			socket.close();
			online = false;
			controller.onDisconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}