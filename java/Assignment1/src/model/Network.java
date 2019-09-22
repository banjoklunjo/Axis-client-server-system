package model;

public class Network {
	private String port;
	private String ip;

	public Network(String ip, String port) {
		this.ip = ip;
		this.port = port;
	}

	public int getPort() {
		return Integer.valueOf(port);
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isValidFormat() {
		return (((port != null) && (!port.equals("")) && (!port.matches("^[a-zA-Z]*$")))

				&&

				((ip != null) && (!ip.equals(""))));
	}

	public String connectionFailedLog() {
		return "Unable to connect to " + ip + " at PORT " + port;
	}
	
	

}
