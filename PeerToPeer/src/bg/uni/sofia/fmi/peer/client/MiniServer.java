package bg.uni.sofia.fmi.peer.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Iterator;

public class MiniServer implements Runnable {

	private final int port;
	private Selector selector;
	private ServerSocketChannel socketChannel;

	public MiniServer(int port) throws IOException {
		this.port = port;
		this.socketChannel = ServerSocketChannel.open();
		this.socketChannel.socket().bind(new InetSocketAddress(this.port));
		this.socketChannel.configureBlocking(false);
		this.selector = Selector.open();
		this.socketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	public void run() {
		try {
			Iterator<SelectionKey> iter;
			SelectionKey key;
			while (this.socketChannel.isOpen()) {
				this.selector.select();
				iter = this.selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					key = iter.next();
					iter.remove();
					if (key.isAcceptable()) {
						SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
						// a connection is being accepted
						client.configureBlocking(false);
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						client.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

						// Read what file the client wants
						client.read(buffer);
						buffer.flip();
						String fileName = new String(buffer.array(), Charset.forName("UTF-8"));
						buffer.clear();
						buffer.flip();

						// then send it to him
						FileChannel fileContent = FileChannel.open(Paths.get(fileName.replace("\0", "")));
						int bytesRead = fileContent.read(buffer);
						while (bytesRead != -1) {
							buffer.flip();
							client.write(buffer);
							buffer.compact();
							bytesRead = fileContent.read(buffer);
						}
						
						fileContent.close();
						client.close();
						System.out.println("server: file sent");
					}

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
