package start;

import java.util.concurrent.Executors;

import client.Controller;

public class Main {

	public static void main(String[] args) {
		new Controller(Executors.newCachedThreadPool());
// comment
	}

}
