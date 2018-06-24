package bg.uni.sofia.fmi.peer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import bg.uni.sofia.fmi.peer.activepeer.ActivePeer;

public class Server implements Runnable {
	public static final int SERVER_PORT = 4444;
	private static Map<ActivePeer, HashSet<String>> peers = new HashMap<>();
	private Socket socket;
	private ActivePeer user;

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

			getClientInfoAndInitializeIt(reader);
			receiveAndProcessQueriesFromClient(reader, pw);

		} catch (Exception e) {
			System.out.println("There is an issue with the connection");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
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

	private Server(Socket socket) {
		this.socket = socket;
	}

	private void getClientInfoAndInitializeIt(BufferedReader reader) throws IOException {
		String ip = this.socket.getInetAddress().getHostAddress();
		String nameAndPortOfClient = reader.readLine();
		System.out.println(nameAndPortOfClient);
		String[] nameAndPort = nameAndPortOfClient.split(" ");
		this.user = new ActivePeer(nameAndPort[0], ip, Integer.parseInt(nameAndPort[1]));
	}

	private void listFiles(PrintWriter pw) {
		for (Map.Entry<ActivePeer, HashSet<String>> peer : peers.entrySet()) {
			ActivePeer client = peer.getKey();
			for (String file : peer.getValue()) {
				pw.println(client.getName() + " : " + file);
			}
		}
		pw.println();
		pw.flush();
	}

	private void unregister(ActivePeer peer, String... files) {
		for (String file : files) {
			peers.get(peer).remove(file);
		}
	}

	private void register(ActivePeer peer, String... files) {
		if (peers.containsKey(peer)) {
			for (String file : files) {
				peers.get(peer).add(file);
			}
		} else {
			HashSet<String> filesHashSet = new HashSet<>();
			for (String file : files) {
				filesHashSet.add(file);
			}
			peers.put(peer, filesHashSet);
		}
	}

	private void listPeers(PrintWriter pw) {
		for (ActivePeer peer : peers.keySet()) {
			pw.println(peer.getClient());
		}
		pw.println();
		pw.flush();
	}

	private void closeConnection() {
		System.out.println("close connection");
		peers.remove(user);
	}

	private void receiveAndProcessQueriesFromClient(BufferedReader reader, PrintWriter pw) throws IOException {
		close: while (true) {
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
				unregister(this.user, Arrays.copyOfRange(splittedMsg, 2, splittedMsg.length));
				break;
			case ("list-files"):
				listFiles(pw);
				break;
			case ("close"):
				closeConnection();
				break close;
			}
		}
	}
}
