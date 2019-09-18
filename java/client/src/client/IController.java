package client;

import java.awt.image.BufferedImage;

public interface IController {

	public void connect(String ip, String port);

	public void onDisconnect();

	public void sendMessage(String message);
	
	public void receivedMessage(String message);

	public void onWindowExit();
	
	public void updateImage(BufferedImage image);

}
