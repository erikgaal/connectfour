package Rogue;

public class InputTester extends Thread{
	private String server;
	private int port;
	private Connection con, con2;
	private long timeStarted;
	private static final String[] commands = {
		"CONNECT",
		"QUIT",
		"LOBBY",
		"ACCEPT",
		"CHAT",
		"DECLINE",
		"ERROR",
		"INVITE",
		"MOVE",
		"PING",
		"PONG",
		"REQUEST",
		"LEADERBOARD"
	};
	public InputTester(String host, int destPort) {
		server = host;
		port = destPort;

		System.out.println("Testing input on server " + host + ":" + destPort);
	}
	
	public long getRunTime() {
		return System.currentTimeMillis() - timeStarted;
	}
	
	public void run() {
		timeStarted = System.currentTimeMillis();
		String name = "Tester" + (int)(Math.random()*1000);
		while(true) {
			con = new Connection(server, port, false);
			con.sendLine("CONNECT " + name);
			con2 = new Connection(server, port, false);
			con2.sendLine("CONNECT 2" + name );
			
			for(int i = 0; i != 100; ++i) {
				if (Math.random() <= 0.5) {
					con.sendLine(commands[(int) (Math.random()*commands.length)] + " 2" + name + ((Math.random() <= 0.2) ? " 1 2 bla bla 2 3" : ""));
				}
				if (Math.random() <= 0.3) {
					con.sendLine(commands[(int) (Math.random()*commands.length)] + "q -1 2 d");
				}
				if (Math.random() <= 0.5) {
					con.sendLine(commands[(int) (Math.random()*commands.length)] + " " + (int)(Math.random()*10 - 2));
				}
				if (Math.random() <= 0.5) {
					con2.sendLine(commands[(int) (Math.random()*commands.length)] + " " + (int)(Math.random()*10 - 2));
				}
				if (Math.random() <= 0.5) {
					con2.sendLine(commands[(int) (Math.random()*commands.length)] + " "  + name + ((Math.random() <= 0.2) ? " 1 2 bla bla 2 3" : ""));
				}
				if (Math.random() <= 0.3) {
					con2.sendLine(commands[(int) (Math.random()*commands.length)] + "q -1 2 d");
				}
				if (Math.random() <= 0.1) {
					con.sendLine(commands[(int) (Math.random()*commands.length)]+"aaq -1 2 d");
				}
				if (Math.random() <= 0.1) {
					con2.sendLine(commands[(int) (Math.random()*commands.length)]+ "aaq -1 2 d");
				}
				if (Math.random() <= 0.5) {
					con.sendLine("INVITE 2"+name);
					con.sendLine("ACCEPT 2"+name);
					con.sendLine("MOVE 3");
				}
				if (Math.random() <= 0.5) {
					con.sendLine("INVITE "+name);
					con.sendLine("ACCEPT "+name);
					con2.sendLine("MOVE 3");
				}
				
				try {
					Thread.sleep((int)(10* Math.random()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			con.sendLine("QUIT Cause reasons");
			con.disconnect();
			con2.sendLine("QUIT Cause reasons");
			con2.disconnect();
			
			try {
				Thread.sleep((int)(1000* Math.random()));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
}
