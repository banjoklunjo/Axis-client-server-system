package client;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	private Socket socket;
	private BufferedReader buffReader;;
	private InputStreamReader input;
	JFrame frame;
	private boolean online;

	private int counter = 0;
	private int realSize = 0;
	private int section = 0;
	byte[][] bilden;

	private InputStream inputStream;

	public Client(Controller controller, Socket socket) {
		this.controller = controller;
		this.socket = socket;

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
			output.println(message); // Send message to server
			output.flush();
			System.out.println("message has been sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		init();
		String msgFromServer = readServerMessage();

		if (msgFromServer != null) {
			List<String> resolutions = Arrays.asList(msgFromServer.split(","));
			if (!msgFromServer.equals("\n")) {
				controller.receivedMessage(msgFromServer);
				controller.setResolutions(resolutions);
			}
		}
		while (online) {

			readServerImage();
		}
	}

	private void readServerImage() {
		if (frame == null) {
			frame = new JFrame();
		} else
			frame.getContentPane().removeAll();

		try {

			InputStream inputStream = socket.getInputStream();
			int length = inputStream.available();
			// byte[] message = readExactly(inputStream, length);
			byte[] message = new byte[length];

			String s = "";
			if (length > 2 && length < 20) {
				message = readExactly(inputStream, length);
				System.out.println("ska skriva ut storleken: ");
				s = new String(message);
				System.out.println(s);

				try {
					realSize = Integer.valueOf(s.trim());
					System.out.println("skriv ut realSize nuuu: ");
					System.out.println(realSize);
				} catch (NumberFormatException e) {
					System.out.println(e.toString());
				}

			}

			//
			else if (length > 20) {

				message = new byte[realSize];

				int index = inputStream.read(message);
				// int index = inputStream.read(message);
				// System.out.println("Index:   " + index);

				// new

				// System.out.println("Index:   " + length);
				ByteArrayInputStream bais = new ByteArrayInputStream(message);
				final BufferedImage bufferedImage = ImageIO.read(bais);

				if (bufferedImage != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							frame.getContentPane().setLayout(new FlowLayout());
							frame.getContentPane().add(
									new JLabel(new ImageIcon(bufferedImage)));
							frame.pack();
							frame.setVisible(true);
						}
					});

				}

			}

		} catch (IOException e) {
			System.out.println("readServerImage() --> Error = "
					+ e.getMessage());
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
			input = new InputStreamReader(socket.getInputStream());
			buffReader = new BufferedReader(input);
		} catch (IOException e1) {
			e1.printStackTrace();
			close();
		}
	}

	private String readServerMessage() {
		String message = null;
		try {
			message = buffReader.readLine();
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
			input.close();
			socket.close();
			online = false;
			controller.onDisconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}