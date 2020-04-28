package Rogue;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class Connection {
	private Socket sock;
	private String server;
	private int port;
	private boolean autoCon;
	private OutputStream out;
	public Connection(String serverArg, int portArg, boolean autoReconnect) {
		server = serverArg;
		port = portArg;
		autoCon = autoReconnect;
		connect();
	}
	
	public void connect() {
		try {
			sock = new Socket(server, port);
			out = sock.getOutputStream();
		} catch (UnknownHostException e) {
			System.out.println("Couldn't find " + server + ":" + port);
		} catch (IOException e) {
			System.out.println("Couldn't find " + server + ":" + port);
		}
	}
	
	public void sendLine(String msg) {
		String data = msg + "\n\n";
		sendData(data);
	}
	
	public void sendData(String data) {
		try {
			out.write(data.getBytes());
			out.flush();
		} catch (IOException e) {
			disconnect();
		}
	}
	
	public void disconnect() {
		try {
			sock.close();
			out.close();
		} catch (IOException e) {
			
		}
		if (autoCon) {
			connect();
		}
	}
}
