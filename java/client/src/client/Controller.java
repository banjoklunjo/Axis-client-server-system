package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;


import ui.View;

public class Controller {
	private View view;
	private Client client;
	private ExecutorService executor;
	private Socket socket;
	private static int resetVar = 0;

	public Controller(ExecutorService executor) {
		this.executor = executor;
		this.view = new View(this);
		view.display();
	}

	private Socket createSocket(String ip, String port) {
		Socket tempSocket = null;
		try {
			tempSocket = new Socket(ip, Integer.valueOf(port));
		} catch (IOException e) {
			System.out.println("Unable to connect to " + ip + " at port "
					+ String.valueOf(port) + "\n");
		}
		return tempSocket;
	}

	public void onDisconnect() {
		view.onDisconnect();
	}

	public void connect(String ip, String port) {
		socket = createSocket(ip, port);
		if (socket != null) {
			client = new Client(this, socket);
			executor.submit(client);
			view.onConnect();
		}
	}

	public void sendMessage(String message) {
		if (message != null && !message.isEmpty()) {
			view.onMessageSent(message);
			client.sendToServer(message);
		} else {
			System.out.println("Type a message to send to the server");
		}

	}

	public void onWindowExit() {
		try {
			client.disconnect();
		} catch (NullPointerException ex) {
			System.out
					.println("Client was null. No disconnet was needed! (controller -> closing()");
		}

		executor.shutdown();
	}

	public void receivedMessage(String message) {
		//view.onMessageReceived(message);
	}

	public void updateImage(BufferedImage image) {

		
		System.out.println("updateImage() --> received image");
	}

	public void setResolutions(List<String> resolutions) {
		view.setResolutions(resolutions);
	}

}
