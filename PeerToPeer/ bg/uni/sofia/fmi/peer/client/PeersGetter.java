package bg.uni.sofia.fmi.peer.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PeersGetter implements Runnable {
	private BufferedReader br;
	private PrintWriter pw;
	private Socket sc;
	private File file;

	public PeersGetter(BufferedReader br, PrintWriter pw, Socket sc, File file) {
		this.br = br;
		this.pw = pw;
		this.sc = sc;
		this.file = file;

	}

	@Override
	public void run() {
		while (sc.isBound()) {

			try {
				Thread.currentThread();
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (this) {
				pw.println("sendActivePeers");
				pw.flush();
				try {
					if (!file.exists()) {
						file.createNewFile();
					}
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					String line;

					while ((line = br.readLine()) != null && line.length() > 0) {
						bw.write(line);

					}
					bw.close();
				} catch (IOException e) {
					System.out.println("close the peers getter");
					break;
				}

			}
		}

	}

}
