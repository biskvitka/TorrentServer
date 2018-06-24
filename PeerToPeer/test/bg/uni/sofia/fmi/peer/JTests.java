//package bg.uni.sofia.fmi.peer;
//
////import static org.junit.Assert.*;
//
//import org.junit.Test;
//
//import bg.uni.sofia.fmi.peer.client.Client;
//import bg.uni.sofia.fmi.peer.server.Server;
//
//public class JTests {
//
//	@Test
//	public void test() {
//		Thread server = new Thread(new Runnable() {
//			public void run() {
//				Server.main();
//			}
//		});
//		
//		Thread client1 = new Thread(new Runnable() {
//			public void run() {
//				Client.main();
//			}
//		});
//		
//		Thread client2 = new Thread(new Runnable() {
//			public void run() {
//				Client.main();
//			}
//		});
//		server.run();
//		
//		client1.run();
//		
//		client2.run();
//	}
//
//}
