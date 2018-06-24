package bg.uni.sofia.fmi.peer.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import bg.uni.sofia.fmi.peer.activepeer.ActivePeer;

public class MiniClient {

	private ActivePeer miniServerOfSender;
	private String nameOfFileToDownload;
	private String pathToSaveDownload;

	public MiniClient(ActivePeer miniServerOfSender,
			String nameOfFileToDownload, String pathToSaveDownload) {
		this.miniServerOfSender = miniServerOfSender;
		this.nameOfFileToDownload = nameOfFileToDownload;
		this.pathToSaveDownload = pathToSaveDownload;
	}

	public void initiateConnectionAndDownloadFile() throws UnknownHostException {
		SocketAddress address = new InetSocketAddress(
				InetAddress.getByName(miniServerOfSender.getIp()),
				miniServerOfSender.getPort());
		try (SocketChannel clientChannel = SocketChannel.open(address)) {
			clientChannel.configureBlocking(false);
			ByteBuffer buffer = ByteBuffer.allocate(1024);

			sendFilePathToDownloadFrom(clientChannel, buffer);
			saveReceivedFile(clientChannel, buffer);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendFilePathToDownloadFrom(SocketChannel clientChannel,
			ByteBuffer buffer) throws IOException {
		buffer.put(nameOfFileToDownload.getBytes());
		buffer.flip();
		clientChannel.write(buffer);
		buffer.clear();
		buffer.flip();
	}

	private void saveReceivedFile(SocketChannel clientChannel, ByteBuffer buffer) {
		String fileName = (new File(nameOfFileToDownload)).getName();
		try (FileOutputStream fileOut = new FileOutputStream(
				new File(pathToSaveDownload + fileName));
				FileChannel fileChannel = fileOut.getChannel();) {
			int bytesRead = clientChannel.read(buffer);
			while (bytesRead != -1) {
				buffer.flip();
				fileChannel.write(buffer);
				buffer.compact();
				bytesRead = clientChannel.read(buffer);
			}
			fileChannel.close();
			System.out.println("client: File received");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
