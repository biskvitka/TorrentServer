package bg.uni.sofia.fmi.peer.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import bg.uni.sofia.fmi.peer.activeclient.ActiveClient;

public class MiniClient {

	private ActiveClient miniServer; // miniSever config of the peer who sends the file
	private String fileToDownload; // name of the file that will be downloaded
	private String newFile; // final file of interest

	public MiniClient(ActiveClient miniServer, String fileToDownload, String newFile) {
		this.miniServer = miniServer;
		this.fileToDownload = fileToDownload;
		this.newFile = newFile;
	}

	public void saveFile() {
		SocketAddress address = new InetSocketAddress(miniServer.getIp(), miniServer.getPort());
		try (SocketChannel clientChannel = SocketChannel.open(address)) {
			//FileOutputStream out = new FileOutputStream(this.newFile);
			// FileChannel

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
