import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Main {
	private static final int port = 6666;

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
		
		Executors.newCachedThreadPool().execute(new Server(socket));
	}

}
