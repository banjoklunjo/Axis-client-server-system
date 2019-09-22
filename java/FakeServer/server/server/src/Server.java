import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javax.imageio.ImageIO;

public class Server implements Runnable {
	
	private Socket socket;
	private  byte[] byteImage;
	
	private InputStream input;
	private InputStreamReader reader;
	private BufferedReader buffReader;

	String path1 = "C:/Users/benjo/OneDrive/Skrivbord/Axis-client-server-system/java/FakeServer/flower1.bmp";
	String path2 = "C:/Users/benjo/OneDrive/Skrivbord/Axis-client-server-system/java/FakeServer/flower2.bmp";
	String path3 = "C:/Users/benjo/OneDrive/Skrivbord/Axis-client-server-system/java/FakeServer/flower3.bmp";
	String path4 = "C:/Users/benjo/OneDrive/Skrivbord/Axis-client-server-system/java/FakeServer/flower4.bmp";
	String path5 = "C:/Users/benjo/OneDrive/Skrivbord/Axis-client-server-system/java/FakeServer/flower5.bmp";
	String path6 = "C:/Users/benjo/OneDrive/Skrivbord/Axis-client-server-system/java/FakeServer/flower6.bmp";

	public Server(Socket socket) {
		this.socket = socket;
		this.input = createInputStream();
		this.reader = new InputStreamReader(input);
		this.buffReader = new BufferedReader(reader);
	}

	@Override
	public void run() {
		sendMessageToClient();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		while (true) {
			try {
				sendImageToClient();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
			
			//readMessageFromClient();
			
		}
	}

	private void sendImageToClient() throws InterruptedException, IOException {
		BufferedImage bufferedImage = ImageIO.read(new File(path1));
		if (bufferedImage != null) {
			ImageIO.write(bufferedImage, "bmp", socket.getOutputStream());
			System.out.println("Picture sent");
			Thread.sleep(3000);
		}
		
		bufferedImage = ImageIO.read(new File(path2));
		if (bufferedImage != null) {
			ImageIO.write(bufferedImage, "bmp", socket.getOutputStream());
			System.out.println("Picture sent");
			Thread.sleep(3000);
		}
	}
	
	private void sendMessageToClient() {
		PrintWriter output;
		try {
			output = new PrintWriter(socket.getOutputStream());
			output.println("640×480, 800×600, 960×720, 1024×768, 1280×960, 1400×1050, 1440×1080, 1600×1200, 1856×1392, 1920×1440"); 
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readMessageFromClient() {
		String clientMsg = readMessage();
		if (clientMsg != null) {
			System.out.println("new message from client: " + clientMsg);
		}
	}
	
	
	private InputStream createInputStream() {
		InputStream input = null;
		try {
			input = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}
	
	private String readMessage() {
		String clientMsg = null;
		try {
			clientMsg = buffReader.readLine();
		} catch (IOException e) {
			
		}
		return clientMsg;
	}
}

/*
 * for (int i = 0; i < 7; i++) { bufferedImage = ImageIO.read(new File(path1));
 * try { ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); } catch
 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
 * if (i == 0) { bufferedImage = ImageIO.read(new File(path1));
 * ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); } else if (i
 * == 1) { bufferedImage = ImageIO.read(new File(path2));
 * ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); }
 * 
 * else if (i == 2) { bufferedImage = ImageIO.read(new File(path3));
 * ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); }
 * 
 * else if (i == 3) { bufferedImage = ImageIO.read(new File(path4));
 * ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); }
 * 
 * else if (i == 4) { bufferedImage = ImageIO.read(new File(path5));
 * ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); }
 * 
 * else if (i == 5) { bufferedImage = ImageIO.read(new File(path6));
 * ImageIO.write(bufferedImage, "jpg", socket.getOutputStream()); }
 * System.out.println("Picture " + i + " sent"); Thread.sleep(10000); }
 */
