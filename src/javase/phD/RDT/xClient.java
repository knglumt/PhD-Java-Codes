package client;

class xClient {
	
	private String ip;
	private int port;
	
	xClient(String ip, int port){
		
		this.setIp(ip);
		this.setPort(port);
		
	}

	String getIp() {
		return ip;
	}

	private void setIp(String ip) {
		this.ip = ip;
	}

	int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}
	
}