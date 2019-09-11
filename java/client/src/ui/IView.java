package ui;

public interface IView {

	public void onConnect();

	public void onDisconnect();

	public void onEmptyFields();

	public void onMessageSent(String message);

	public void onMessageReceived(String message);
	
	public void display();

}
