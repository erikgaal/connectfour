package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static PlayerList playerList;
    public static GameLogic gameLogic;

    public static void main(String args[]) {
        gameLogic = new GameLogic();
        playerList = gameLogic.getPlayerList();
        gameLogic.start();
        ServerSocket socket = null;
        while (socket == null) {
	        System.out.println("Enter a port number to listen on: ");
	        Scanner sc = new Scanner(System.in);
	        try {
	            socket = new ServerSocket(sc.nextInt());
	        } catch (IOException e) {
	        	socket = null;
	            Logging.logError("Couldn't listen on port 8888.");
	        }
        }
        while (true) {
            Socket client;
            try {
                client = socket.accept();
            } catch (IOException e) {
                Logging.logError("We are under a DoS attack. Let's sleep for a bit so we dont consume so much cpu");
                try {
                    Thread.sleep(1000);
                    socket.close();
                    socket = new ServerSocket(8888);
                } catch (InterruptedException e1) {
                    Logging.logError("Interupted!");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                continue;
            }

            ThreadListener player;
            Logging.log("Connection from " + client.getInetAddress());
            try {
                player = new ThreadListener(client);
                gameLogic.addThread(player);
                player.start();
            } catch (IOException e) {
                Logging.logError("Couldn't open a ThreadListener for a new client.");
            }
        }
    }
}
