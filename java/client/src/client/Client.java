package client;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Client implements Runnable {
	private IController controller;
	private Socket socket;
	private BufferedReader buffReader;;
	private InputStreamReader input;
	private boolean online;

	public Client(IController controller, Socket socket) {
		this.controller = controller;
		this.socket = socket;
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
		while (online) {
			//String msgFromServer = readServerMessage();
			readServerImage();
			/*if (msgFromServer != null) {
				controller.receivedMessage(msgFromServer);
			}*/
		}
	}

	
	private void readServerImage() {
		//byte[] sizeAr = new byte[1024];
		try {
			System.out.println("readServerImage -> start");
			BufferedImage bufferedImage = ImageIO.read(/*ImageIO.createImageInputStream*/(socket.getInputStream()));
			System.out.println("readServerImage() --> Recieved image from server -> width = " + 
			String.valueOf(bufferedImage.getWidth()) + 
			" height = " + String.valueOf(bufferedImage.getHeight()));
			controller.updateImage(bufferedImage);
		} catch (IOException e) {
			System.out.println("readServerImage() --> Error = " + e.getMessage());
			e.printStackTrace();
		}
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