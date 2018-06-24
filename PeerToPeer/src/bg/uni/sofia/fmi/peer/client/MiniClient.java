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
	private String fileToDownload; // name of the file that will be downloaded
	private String newFilePath; // final file of interest

	private void sendFilePathToDownloadFrom(ByteBuffer buffer,
			SocketChannel clientChannel) throws IOException {
		buffer.put(fileToDownload.getBytes());
		buffer.flip();
		clientChannel.write(buffer);
		buffer.clear();
		buffer.flip();
	}
	
	public MiniClient(ActivePeer miniServerOfSender, String fileToDownload, String newFilePath) {
		this.miniServerOfSender = miniServerOfSender;
		this.fileToDownload = fileToDownload;
		this.newFilePath = newFilePath;
	}

	public void saveReceivedFile() throws UnknownHostException {
		SocketAddress address = new InetSocketAddress(
				InetAddress.getByName(miniServerOfSender.getIp()),
				miniServerOfSender.getPort());
		try (SocketChannel clientChannel = SocketChannel.open(address)) {
			clientChannel.configureBlocking(false);
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			sendFilePathToDownloadFrom(buffer, clientChannel);

			// save file
			String fileName = (new File(fileToDownload)).getName();
			try (FileOutputStream fileOut = new FileOutputStream(new File(newFilePath + fileName));
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
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
