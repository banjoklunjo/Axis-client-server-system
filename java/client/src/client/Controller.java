package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import ui.IView;
import ui.View;

public class Controller implements IController {
	private IView view;
	private Client client;
	private ExecutorService executor;
	private Socket socket;

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
			System.out.println("Unable to connect to " + ip + " at port " + String.valueOf(port) + "\n");
		}
		return tempSocket;
	}

	@Override
	public void onDisconnect() {
		view.onDisconnect();
	}

	@Override
	public void connect(String ip, String port) {
		if (ip.isEmpty() || port.isEmpty()) {
			view.onEmptyFields();
		} else {
			socket = createSocket(ip, port);
			if (socket != null) {
				client = new Client(this, socket);
				executor.submit(client);
				view.onConnect();
			}
		}
	}

	@Override
	public void sendMessage(String message) {
		if (message != null && !message.isEmpty()) {
			view.onMessageSent(message);
			client.sendToServer(message);
		} else {
			System.out.println("Type a message to send to the server");
		}

	}

	@Override
	public void onWindowExit() {
		try {
			client.disconnect();
		} catch (NullPointerException ex) {
			System.out.println("Client was null. No disconnet was needed! (controller -> closing()");
		}

		executor.shutdown();
	}
	
	
	
	

	@Override
	public void receivedMessage(String message) {
		view.onMessageReceived(message);
	}

	@Override
	public void updateImage(BufferedImage image) {
		view.updateImage(image);
		
	}

}
