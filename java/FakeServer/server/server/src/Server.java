import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javax.imageio.ImageIO;

public class Server {
	private static final int port = 6666;
	private static final String ONESHOTNAME = "oneshotimage.jpg";
	private static byte[] byteImage;

	public static void main(String[] args) throws IOException {

		ServerSocket server = null;

		try {
			server = new ServerSocket(port);
			System.out.println("Server socket ready on port: " + port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
			System.exit(-1);
		}

		Socket socket = server.accept();
		if (socket.isConnected()) {
			System.out.println("socket is connected");
		} else {
			System.out.println("socket is not connected");
		}

		try {
			while (true) {
				int randomValue = new Random().nextInt(2);
				BufferedImage bufferedImage;
				if (randomValue == 0) {
					bufferedImage = ImageIO.read(new File(
							"/home/axis/workspace/flower.jpg"));
					ImageIO.write(bufferedImage, "jpg", socket.getOutputStream());
					
				} else {
					bufferedImage = ImageIO.read(new File(
							"/home/axis/workspace/cameraPicture.jpeg"));
					ImageIO.write(bufferedImage, "jpeg", socket.getOutputStream());
				}
				
				System.out.println("sending new pic");
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/*
		 * byteImage = ByteArrayConversion.toByteArray(bufferedImage);
		 * 
		 * System.out.println(byteImage.toString());
		 * 
		 * OutputStream os = socket.getOutputStream();
		 * 
		 * ObjectOutputStream oos = new ObjectOutputStream(os);
		 * oos.writeObject(byteImage);
		 */
	}

}
