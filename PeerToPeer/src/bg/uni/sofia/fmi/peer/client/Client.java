package bg.uni.sofia.fmi.peer.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import bg.uni.sofia.fmi.peer.activepeer.ActivePeer;
import bg.uni.sofia.fmi.peer.exception.InactivePeerException;
import bg.uni.sofia.fmi.peer.exception.InvalidCommandException;
import bg.uni.sofia.fmi.peer.server.Server;

public class Client {

	private final static String FILE_DIR = "/home/gabi/";
	private Thread miniServer;
	private String name;
	private int port;
	private File file;

	public Client(String name, int port) throws IOException {
		this.name = name;
		this.port = port;
		this.miniServer = new Thread(new MiniServer(this.port));
	}

	public static Client login(BufferedReader userInput, PrintWriter pw)
			throws IOException {
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
		System.out.println("Valid commands: close, list-files, ");
		System.out.println("register <String...files>), ");
		System.out.println("unregister <String ...files> ");
		System.out.println("download <user> <path to file on user> <path to save>");
	}

	public void executeCommands(PrintWriter pw, BufferedReader userInput,
			BufferedReader br) throws IOException {
		closeClient: while (true) {
			System.out.print("$ ");
			String userWish = userInput.readLine();
			String[] input = userWish.split(" ");
			synchronized (pw) {
				switch (input[0]) {
				case ("close"):
					sendMsgToServer(pw, input[0]);
					break closeClient;
				case ("list-files"):
					this.listFiles(pw, br, input);
					break;
				case ("register"):
					this.registerFiles(pw, input);
					break;
				case ("unregister"):
					sendMsgToServer(pw, userWish);
					break;
				case ("download"):
					try {
						this.validateDownloadCommandAndDownloadFile(input);
					} catch (InactivePeerException e) {
						System.out.println("couldn't download");
					} catch (InvalidCommandException e) {
						System.out.print("To download use: download ");
						System.out.println(" <user> <path to file on user> <path to save>");
					}
					break;
				default:
					printHowToUse();
				}
			}
		}
	}

	public static void main(String[] args) {
		try (Socket s = new Socket("localhost", Server.SERVER_PORT);
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				BufferedReader br = new BufferedReader(
						new InputStreamReader(s.getInputStream()))) {

			BufferedReader userInput = new BufferedReader(
					new InputStreamReader(System.in));
			Client client = login(userInput, pw);
			File file = new File(FILE_DIR + client.getName() + ".txt");
			client.setFile(file);
			Thread peersGetter = new Thread(new PeersGetter(br, pw, s, file));
			peersGetter.start();
			client.executeCommands(pw, userInput, br);
		} catch (IOException e) {
			System.out.println("couldn't connect!");
			System.exit(-1);
		}
	}

	private void setFile(File file) {
		this.file = file;
	}

	private int getPort() {
		return this.port;
	}

	private String getName() {
		return this.name;
	}

	private void registerFiles(PrintWriter pw, String... input) {
		boolean fileIsRegistered = false;
		System.out.println("REGISTER");
		pw.print("register " + this.getName() + " ");
		for (int i = 1; i < input.length; ++i) {
			if (new File((input[i])).isFile()) {
				System.out.println("file");
				pw.print(input[i]);
				pw.print(" ");
				fileIsRegistered = true;
			}
		}
		pw.println();
		pw.flush();
		if (fileIsRegistered && !this.miniServer.isAlive()) {
			this.miniServer.start();
		}
	}

	private void listFiles(PrintWriter pw, BufferedReader br, 
			String... input) throws IOException {
		pw.println(input[0]);
		pw.flush();
		String line;
		synchronized (br) {
			while ((line = br.readLine()) != null && line.length() > 0) {
				System.out.println(line);
			}
		}
	}

	private void sendMsgToServer(PrintWriter pw, String msg) {
		pw.println(msg);
		pw.flush();
	}

	private void validateDownloadCommandAndDownloadFile(String... params)
			throws InactivePeerException, UnknownHostException, InvalidCommandException {
		if (params.length != 4) {
			throw new InvalidCommandException();
		}

		String nameOfPeerToDownloadFrom = params[1];
		String infoForPeerToDownloadFrom = this.ifPeerExistsInPeerFileTakeHisInfo(
				nameOfPeerToDownloadFrom);
		String ipAndPortOfPeer;
		if (infoForPeerToDownloadFrom != null) {
			ipAndPortOfPeer = (infoForPeerToDownloadFrom.split(" "))[2];
		} else {
			throw new InactivePeerException();
		}

		String[] ipPort = ipAndPortOfPeer.split(":");
		String ip = ipPort[0];
		int port = Integer.parseInt(ipPort[1]);
		ActivePeer peerToDownloadFrom = new ActivePeer(nameOfPeerToDownloadFrom, ip, port);
		MiniClient miniClient = new MiniClient(peerToDownloadFrom, params[2], params[3]);
		miniClient.initiateConnectionAndDownloadFile();
	}

	private String ifPeerExistsInPeerFileTakeHisInfo(String nameOfPeerToDownloadFrom)
			throws InactivePeerException {
		String infoForPeerToDownloadFrom = null;
		synchronized (this.file) {
			try (Scanner scan = new Scanner(file)) {
				while (scan.hasNext()) {
					String line = scan.nextLine();
					if (line.contains(nameOfPeerToDownloadFrom)) {
						infoForPeerToDownloadFrom = line;
						break;
					}
				}
			} catch (FileNotFoundException e) {
				throw new InactivePeerException();
			}
		}
		return infoForPeerToDownloadFrom;
	}
}
