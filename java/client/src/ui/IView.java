package ui;

import java.awt.image.BufferedImage;

public interface IView {

	public void onConnect();

	public void onDisconnect();

	public void onEmptyFields();

	public void onMessageSent(String message);

	public void onMessageReceived(String message);
	
	public void display();
	
	public void updateImage(BufferedImage image);

}
