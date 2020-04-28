package connectfour.net;

import connectfour.Controller;
import connectfour.util.Logging;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Connection between the Server and the Client.
 */
public class Connection extends Thread {

    private final Socket socket;
    private final Controller controller;
    private String name;

    private BufferedReader input;
    private BufferedWriter output;

    private boolean running;

    /**
     * Constructs the Connection
     * @param socket Socket with the server
     * @param name name of the player
     * @param controller connection
     * @throws IOException
     */
    public Connection(Socket socket, String name, Controller controller) throws IOException {
        this.socket = socket;
        this.controller = controller;
        this.name = name;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        running = true;
    }

    /**
     * Starts the connection and listens for input.
     */
    public void run() {
        init();

        while (running) {
            try {
                parseMessage(input.readLine());
            } catch (IOException e) {
                return;
            }
        }
    }

    /**
     * Starts the connection
     */
    private void init() {
        connect(name);
        requestLobby();
        requestLeaderboard();
    }

    /**
     * Writes a packet to the Socket and flushes it.
     * @param packet
     */
    private void writeLine(String packet) {
        try {
            output.write(packet + "\n\n");
            output.flush();
        } catch (IOException e) {
            if (!socket.isClosed()) {
                disconnect();
            }
        }
    }

    /**
     * Disconnects from the Socket, closing any streams where possible.
     */
    private void disconnect() {
        if (!running) {
            return;
        }
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            // Already disconnected, nothing to do
        }
        try {
            input.close();
        } catch (IOException e) {
            System.out.println("Couldn't close input");
        }
        try {
            output.close();
        } catch (IOException e) {
            System.out.println("Couldn't close output");
        }
    }

    /**
     * Parses a packet and calls the appropriate controller function.
     * @param msg raw message from the socket
     */
    private void parseMessage(String msg) {
        if (msg == null) {
            return;
        }
        Scanner in = new Scanner(msg);
        if (!in.hasNext()) {
            in.close();
            return;
        }
        String packet = in.nextLine();
        Logging.logVerbose(packet);
        String cmd = packet.split(" ", 2)[0];
        if (cmd.equals(Packet.ACCEPT_CONNECT)) {
            if (packet.split(" ").length > 1) {
                String data = packet.split(" ", 2)[1];
                for (String f : data.split(" ")) {
                    if (f.equals(Feature.CHAT)) {
                        controller.setVisible(controller.getChatFrame(), true);
                    } else if (f.equals(Feature.LEADERBOARD)) {
                        controller.setVisible(controller.getLeaderboardFrame(), true);
                    }else if (f.equals(Feature.AWESOMENESS)) {
                        Logging.log("Hey Ser4, how are you doing?");
                    } else {
                        Logging.log("Server supports " + f + ", but we don't.");
                    }
                }
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.CHAT)) {
            if (packet.split(" ").length > 1) {
                String data = packet.split(" ", 2)[1];
                controller.getChatFrame().showMessage(data);
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.GAME_END)) {
            if (packet.split(" ").length >= 3) {
                String data = packet.split(" ", 2)[1];
                String reason = data.split(" ")[0];
                String winner = data.split(" ")[1];
                controller.gameEnd(winner, reason);
            } if (packet.split(" ").length == 2) {
                String data = packet.split(" ", 2)[1];
                controller.gameEnd(data);
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.GAME_START)) {
            controller.gameStart();
        } else if (cmd.equals(Packet.INVITE)) {
            if (packet.split(" ").length >= 2) {
                String data = packet.split(" ", 2)[1];
                controller.showInvite(data);
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.LEADERBOARD)) {
            Logging.logError(String.valueOf(packet.split(" ").length));
            if (((packet.split(" ").length - 1) % 5 == 0)) {
                String data = packet.split(" ", 2)[1];
                String[] words = data.split(" ");

                Object[][] ranks = new Object[words.length / 5][];
                for (int i = 0; i < words.length / 5; i++) {
                    ranks[i] = new Object[]{words[5 * i], words[5 * i + 1], words[5 * i + 2], words[5 * i + 3], Integer.parseInt(words[5 * i + 4])};
                }

                controller.getLeaderboardFrame().updateList(ranks);
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.LOBBY)) {
            if (packet.split(" ").length > 1) {
                Set<String> players = new HashSet<String>();
                String data = packet.split(" ", 2)[1];
                Collections.addAll(players, data.split(" "));
                controller.updateLobby(players);
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.MOVE_OK)) {
            if (packet.split(" ").length >= 3) {
                String data = packet.split(" ", 2)[1];
                int colour = Integer.parseInt(data.split(" ")[0]) == 2 ? 0 : 1;
                int column = Integer.parseInt(data.split(" ")[1]);
                controller.moveOk(colour, column);
            } else {
                Logging.logError("Server supplied the wrong number of arguments in his " + cmd + " request.");
                sendError("You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals(Packet.PONG)) {
            Logging.logVerbose("PONG");
        } else if (cmd.equals(Packet.PING)) {
            Logging.logVerbose("PING");
        } else if (cmd.equals(Packet.REQUEST_MOVE)) {
            controller.requestMove();
        } else if (cmd.equals(Packet.EASTEREGG)) {
            if (packet.split(" ").length > 1) {
                String data = packet.split(" ", 2)[1];
                Logging.logError(data);
            }
        } else {
            Logging.logError("Server sent an unknown request " + msg);
            sendError(ReturnCode.UNKNOWN_FAIL, "Unknown request");
        }
    }

    /**
     * Sends a <code>CONNECT &lt;name&gt;</code> packet to the server.
     * @param name
     */
    private void connect(String name) {
        Logging.logError("Connecting with name " + name);
        writeLine(Packet.CONNECT + " " + name);
    }

    /**
     * Sends a <code>INVITE &lt;player&gt;</code> packet to the server.
     * @param player name of the player
     */
    public void invite(String player) {
        Logging.logError("Inviting player " + player);
        writeLine(Packet.INVITE + " " + player);
    }

    /**
     * Sends a <code>ACCEPT &lt;player&gt;</code> packet to the server.
     * @param player name of the player
     */
    public void acceptInvite(String player) {
        Logging.logError("Starting a game with " + player);
        writeLine(Packet.ACCEPT_INVITE + " " + player);
    }

    /**
     * Sends a <code>DECLINE &lt;player&gt;</code> packet to the server.
     * @param player name of the player
     */
    public void declineInvite(String player) {
        Logging.logError("Starting a game with " + player);
        writeLine(Packet.DECLINE_INVITE + " " + player);
    }

    /**
     * Sends a <code>CHAT &lt;message&gt;</code> packet to the server.
     * @param message text message
     */
    public void chat(String message) {
        Logging.logError("Chatting '" + message + "'");
        writeLine(Packet.CHAT + " " + message);
    }

    /**
     * Sends a <code>MOVE &lt;column%gt;</code> packet to the server.
     * @param column column
     */
    public void move(int column) {
        Logging.logError("Moving at " + column);
        writeLine(Packet.MOVE + " " + column);
    }

    /**
     * Sends a <code>QUIT</code> packet to the server.
     */
    public void quit() {
        Logging.logError("Quitting the game");
        writeLine(Packet.QUIT);
    }

    /**
     * Sends a <code>LOBBY</code> packet to the server.
     */
    public void requestLobby() {
        Logging.logError("Requesting the lobby");
        writeLine(Packet.REQUEST_LOBBY);
    }

    /**
     * Sends a <code>LEADERBOARD</code> packet to the server.
     */
    public void requestLeaderboard() {
        Logging.logError("Requesting the lobby");
        writeLine(Packet.REQUEST_LEADERBOARD);
    }

    /**
     * Sends an <code>ERROR &lt;message&gt;</code> packet to the server.
     * @param error error message
     */
    public void sendError(String error) {
        if (error.length() <= 512) {
            writeLine(Packet.ERROR + " " + error);
        } else {
            Logging.logError("Tried to send too long error message \'" + error + "\'.");
        }
    }

    /**
     * Sends an <code>ERROR &lt;code&gt; &lt;error&gt;</code> packet to the server.
     * @param code name of the player
     * @param error error message
     */
    public void sendError(ReturnCode code, String error) {
        if (error.length() <= 512) {
            writeLine(Packet.ERROR + " " + code + " " + error);
        } else {
            Logging.logError("Tried to send too long error message(" + error + ")");
        }
    }
}
