package bg.uni.sofia.fmi.peer.activeclient;

import java.net.InetAddress;

public class ActiveClient {
	private String name;
	private InetAddress ip;
	private int port;

	public ActiveClient(String name, InetAddress ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public String getName() {
		return this.name;
	}

	public InetAddress getIp() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}

	public String getClient() {
		return this.name + " - " + this.ip + ":" + this.port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActiveClient other = (ActiveClient) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}
