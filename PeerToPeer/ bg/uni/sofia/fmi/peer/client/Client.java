package bg.uni.sofia.fmi.peer.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import bg.uni.sofia.fmi.peer.activeclient.ActiveClient;
import bg.uni.sofia.fmi.peer.exception.InactivePeerException;
import bg.uni.sofia.fmi.peer.server.Server;

public class Client {

	private MiniServer miniServer;
	private String name;
	private int port;
	private final static String FILE_DIR = "/home/gabi/";
	private File file;

	public Client(String name, int port) {
		this.name = name;
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public String getName() {
		return this.name;
	}

	public void loginClient(PrintWriter pw) throws IOException {
		pw.println(this.name + ":" + this.port);
		pw.flush();
		miniServer = new MiniServer(port);

	}

	public void startMiniServer() {

	}

	public static Client loginClient(BufferedReader userInput, PrintWriter pw) throws IOException {
		System.out.print("What is your name: ");
		String name = userInput.readLine();
		System.out.print("What is the port you want your mini server to work on: ");
		Client peer = null;
		boolean success = false;
		while (!success) {
			try {
				peer = new Client(name, Integer.parseInt(userInput.readLine()));
				success = true;
			} catch (NumberFormatException e) {
				System.out.print("Please input number of a port: ");
			}
		}
		pw.println(peer.getName() + " " + peer.getPort());
		pw.flush();
		return peer;
	}

	public static void printHowToUse() {
		System.out.println("Invalid command");
		System.out.print("Valid commands: close, list-files, ");
		System.out.print("register <String...files>), ");
		System.out.println("unregister <String ...files> ");
	}

	public void register(PrintWriter pw, String... input) {

		pw.print("register" + " " + this.getName() + " ");
		for (int i = 1; i < input.length; ++i) {
			if (new File((input[i])).isFile()) {
				pw.print(input[i]);
				pw.print(" ");
			}
		}
		pw.println();
		pw.flush();
	}

	public void listFiles(PrintWriter pw, BufferedReader br, String... input) throws IOException {
		pw.println(input[0]);
		pw.flush();
		String line;
		synchronized (br) {
			while ((line = br.readLine()) != null && line.length() > 0) {
				System.out.println(line);
			}
		}
	}

	public static void sendMsgToServer(PrintWriter pw, String msg) {
		pw.println(msg);
		pw.flush();
	}

	public void downloadFile(String... params)
			throws FileNotFoundException, InactivePeerException, UnknownHostException {
		String nameOfPeerToDownloadFrom = params[1];
		// find if the peer from who the client wants to download exist
		Scanner scan;
		String infoForPeerToDownloadFrom = null;
		synchronized (file) {
			try {
				scan = new Scanner(file);
			} catch (NullPointerException e) {
				throw new InactivePeerException();
			}
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (line.contains(nameOfPeerToDownloadFrom)) {
					infoForPeerToDownloadFrom = line;
					break;
				}
			}
		}
		String ipAndPortOfPeer;
		if (infoForPeerToDownloadFrom != null) {
			ipAndPortOfPeer = (infoForPeerToDownloadFrom.split(" "))[2];
		} else {
			throw new InactivePeerException();
		}
		scan.close();
		String[] ipPort = ipAndPortOfPeer.split(":");
		InetAddress ipAddr = InetAddress.getByName(ipPort[0]);
		int peerPort = Integer.parseInt(ipPort[1]);
		String pathToFileOnUser = params[2];
		String pathToSave = params[3];
		ActiveClient peerToDownloadFrom = new ActiveClient(nameOfPeerToDownloadFrom, ipAddr, peerPort);
		MiniClient miniClient = new MiniClient(peerToDownloadFrom, params[2], params[3]);
	}

	public static void main(String[] args) throws InterruptedException {
		// connect to main server
		try (Socket s = new Socket("localhost", Server.SERVER_PORT);
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			Client peer = loginClient(userInput, pw);
			File file = new File(FILE_DIR + peer.getName() + ".txt");
			Thread peersGetter = new Thread(new PeersGetter(br, pw, s, file));
			peersGetter.start();
			closeClient: while (true) {
				String userWish = userInput.readLine();
				String[] input = userWish.split(" ");
				// validation of the user command input
				synchronized (pw) {
					switch (input[0]) {
					case ("close"):
						sendMsgToServer(pw, input[0]);
						break closeClient;
					case ("list-files"):
						peer.listFiles(pw, br, input);
						break;
					case ("register"):
						peer.register(pw, input);
						break;
					case ("unregister"):
						sendMsgToServer(pw, userWish);
						break;
					case ("download"):
						try {
							peer.downloadFile(input);
						} catch (InactivePeerException | FileNotFoundException e) {
							e.printStackTrace();
						}
						break;
					default:
						printHowToUse();
					}
				}
			}
		} catch (IOException e) {
			System.out.println("couldn't connect!");
			System.exit(-1);
		}

	}
}
