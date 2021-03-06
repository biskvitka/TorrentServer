package bg.uni.sofia.fmi.peer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import bg.uni.sofia.fmi.peer.activeclient.ActiveClient;

public class Server implements Runnable {
	private static Map<ActiveClient, HashSet<String>> peers = new HashMap<>();
	public static final int SERVER_PORT = 4444;
	private Socket socket;
	private ActiveClient user;

	public Server(Socket socket) {
		this.socket = socket;
	}

	public void listFiles(PrintWriter pw) {
		for (Map.Entry<ActiveClient, HashSet<String>> peer : peers.entrySet()) {
			ActiveClient client = peer.getKey();
			for (String file : peer.getValue()) {
				pw.println(client.getName() + " : " + file);
			}
		}
		pw.println();
		pw.flush();
	}

	public void unregister(ActiveClient peer, String... files) {
		for (String file : files) {
			peers.get(peer).remove(file);
		}
	}

	public void register(ActiveClient peer, String... files) {
		if (peers.containsKey(peer)) {
			for (String file : files) {
				peers.get(peer).add(file);// ?
			}
		} else {
			HashSet<String> filesHashSet = new HashSet<>();
			for (String file : files) {
				filesHashSet.add(file);
			}
			peers.put(peer, filesHashSet);
		}
	}

	public void listPeers(PrintWriter pw) {
		for (ActiveClient peer : peers.keySet()) {
			pw.println(peer.getClient());
		}
		pw.println();
		pw.flush();
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter pw = new PrintWriter(socket.getOutputStream())) {
			InetAddress ip = this.socket.getInetAddress();
			String line = reader.readLine();
			System.out.println(line);
			String[] nameAndPort = line.split(" ");
			this.user = new ActiveClient(nameAndPort[0], ip, Integer.parseInt(nameAndPort[1]));

			close: while (true) { // if the client is still connected
				String wish = reader.readLine();
				String[] splittedMsg = wish.split(" ");
				switch (splittedMsg[0]) {
				case ("sendActivePeers"):
					listPeers(pw);
					break;
				case ("register"):
					register(this.user, Arrays.copyOfRange(splittedMsg, 2, splittedMsg.length));
					break;
				case ("unregister"):
					unregister(this.user, Arrays.copyOfRange(splittedMsg, 1, splittedMsg.length));
					break;
				case ("list-files"):
					listFiles(pw);
					break;
				case ("close"):
					System.out.println("close connection");
					break close;
				}
			}

		} catch (Exception e) {
			System.out.println("There is an issue with the connection");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		try (ServerSocket ss = new ServerSocket(SERVER_PORT)) {
			System.out.println("The server is on");
			while (true) {
				Socket socket = ss.accept();
				Thread server = new Thread(new Server(socket));
				server.start();
			}
		} catch (IOException e) {
			System.out.println("there is a problem the with the server socket");
			e.printStackTrace();
		}
	}

}
