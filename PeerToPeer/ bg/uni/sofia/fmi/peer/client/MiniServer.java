package bg.uni.sofia.fmi.peer.client;

import java.io.IOException;
//import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class MiniServer implements Runnable {

	private final int port;
	private Selector selector;
	private ServerSocketChannel socketChannel;

	public MiniServer(int port) throws IOException {
		this.port = port;
		this.socketChannel = ServerSocketChannel.open();
		this.socketChannel.socket().bind(new InetSocketAddress(port));
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
						SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						sc.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						
						// Read what file the client wants
						//int bytesRead = sc.read(buffer);
						buffer.flip();
						String fileName = new String(buffer.array(), Charset.forName("UTF-8"));
						buffer.clear();
						buffer.flip();
						System.out.println(fileName);
						//then send it to him
//						RandomAccessFile aFile = new RandomAccessFile(fileName, "r");
//						FileChannel inChannel = aFile.getChannel();
//						while () {
//							sc.read(buffer);
//							buffer.flip();
//							String fileName;
//							// fileName.write(buffer);
//						}

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
