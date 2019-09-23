package client;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class Client implements Runnable {
	private Controller controller;
	private Socket socket;
	private BufferedReader buffReader;;
	private InputStreamReader input;
	private boolean online;
	
	private InputStream inputStream ;

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
		while (online) {
			String msgFromServer = readServerMessage();

			if (msgFromServer != null) {
				List<String> resolutions = Arrays.asList(msgFromServer
						.split(","));
				if (!msgFromServer.equals("\n")) {
					controller.receivedMessage(msgFromServer);
					controller.setResolutions(resolutions);
				}
			}
			readServerImage();
		}
	}

	private void readServerImage() {
		try {
			/*byte[] sizeAr = new byte[40000];
            inputStream.read(sizeAr);
	        int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

	        byte[] imageAr = new byte[size];
	        inputStream.read(imageAr);

	        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageAr));*/
	        
			BufferedImage bufferedImage = ImageIO.read(socket.getInputStream());
			if (bufferedImage != null)
				controller.updateImage(bufferedImage);
		} catch (IOException e) {
			System.out.println("readServerImage() --> Error = "
					+ e.getMessage());
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