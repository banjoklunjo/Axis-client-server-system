import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javax.imageio.ImageIO;

public class Server implements Runnable {
	Constants constants = new Constants();

	private Socket socket;
	private InputStream input;
	private InputStreamReader reader;
	private BufferedReader buffReader;
	private ObjectOutputStream output;



	public Server(Socket socket) throws IOException {
		this.socket = socket;
		this.input = createInputStream();
		this.reader = new InputStreamReader(input);
		this.buffReader = new BufferedReader(reader);
		output = new ObjectOutputStream(socket.getOutputStream());
	}

	@Override
	public void run() {
		sendMessageToClient();
		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		while (true) {
			try {
				//sendImageToClient();
				sendImageToClientByteArray();

			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}


			//readMessageFromClient();
		}
	}

	/*private void sendImageToClient() throws InterruptedException, IOException {
		BufferedImage bufferedImage = ImageIO.read(new File(constants.path1));
		if (bufferedImage != null) {
			ImageIO.write(bufferedImage, "bmp", socket.getOutputStream());
			System.out.println("Picture sent");
			Thread.sleep(3000);
		}
		
		bufferedImage = ImageIO.read(new File(constants.path2));
		if (bufferedImage != null) {
			ImageIO.write(bufferedImage, "bmp", socket.getOutputStream());
			System.out.println("Picture sent");
			Thread.sleep(3000);
		}
	}*/

	private void sendImageToClientByteArray() throws InterruptedException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage bufferedImage = ImageIO.read(new File(constants.path1));

		if (bufferedImage != null) {
			ImageIO.write(bufferedImage, "jpg", baos);
			baos.flush();
			byte[] buffer = baos.toByteArray();
            sendFakeImageSizeToClient(String.valueOf(buffer.length));
			baos.close();
			baos = null;
			output.writeObject(buffer);
			output.flush();
			output = new ObjectOutputStream(socket.getOutputStream());
			Thread.sleep(3000);
		}

		baos = new ByteArrayOutputStream();
		bufferedImage = ImageIO.read(new File(constants.path2));

		if (bufferedImage != null) {
			ImageIO.write(bufferedImage, "jpg", baos);
			baos.flush();
			System.out.println("Size of baos = " + baos.size());
			byte[] buffer = baos.toByteArray();
            sendFakeImageSizeToClient(String.valueOf(buffer.length));
			baos.close();
			baos = null;
			output.writeObject(buffer);
			output.flush();
			output = new ObjectOutputStream(socket.getOutputStream());
			Thread.sleep(3000);
		}
	}
	
	private void sendMessageToClient() {
		PrintWriter output;
		try {
			output = new PrintWriter(socket.getOutputStream());
			output.println("640x480, 800x600, 960x720, 1024x768, 1280x960, 1400x1050, 1440x1080, 1600x1200, 1856x1392, 1920x1440");
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendFakeImageSizeToClient(String imgSize) {
		PrintWriter output;
		try {
			output = new PrintWriter(socket.getOutputStream());
			output.println(imgSize);
			System.out.println("imgSize " + imgSize + " was sent to the client");
			output.flush();
			Thread.sleep(3000);
		} catch (IOException | InterruptedException e ) {
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
