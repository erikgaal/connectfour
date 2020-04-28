package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The clientHandler.
 * @author cotix
 *
 */
public class ThreadListener extends Thread {
    private /*@ spec_public @*/ Socket socket;
    private /*@ spec_public @*/ String name;
    private /*@ spec_public @*/ BufferedReader input;
    private /*@ spec_public @*/ BufferedWriter output;
    private /*@ spec_public @*/ Game game;
    private /*@ spec_public @*/ int allowedMistakes;
    private /*@ spec_public @*/ long connectedSince;
    private /*@ spec_public @*/ boolean isClosed;
    private /*@ spec_public @*/ long lastSeen;
    /**
     * Initialises all variables
     * @param sock
     * @throws IOException
     */
	//@ ensures input != null && output != null && allowedMistakes == 5 && connectedSince == System.currentTimeMillis();
    
    public ThreadListener(Socket sock) throws IOException {
        socket = sock;
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        game = null;
        name = null;
        allowedMistakes = 5;
        connectedSince = System.currentTimeMillis();
        isClosed = false;
        lastSeen = System.currentTimeMillis();
    }
    /**
     * entry point. While loop where it passes all packets to parseMessage()
     */
    //@ requires isActive();
    public void run() {
        while (isActive()) {
            try {
                lastSeen = System.currentTimeMillis();
                parseMessage(input.readLine());
            } catch (IOException e) {
                return;
            }
        }
        Logging.log(name + " listening thread stoped.");
    }
    /**
     * 
     * @return time since last packet
     */
    //@ ensures \result >= 0;
    //@ pure
    public long lastSeen() {
        return System.currentTimeMillis() - lastSeen;
    }
    /**
     * 
     * @return connectedTime
     */
    //@ ensures \result >= 0;
    //@ pure
    public long getConnectedTime() {
        return connectedSince - System.currentTimeMillis();
    }
    /**
     * 
     * @return !isClosed
     */
    //@ pure
    public boolean isActive() {
        return !isClosed;
    }
    /**
     * Lots of if-elses that check every possible command and act accordingly.
     * Most of the time it checks if all the arguments are correct, and calls the apriopiate function
     * @param msg
     */
    //@ requires msg.length() >= 1 && isActive();
    //@ ensures msg.startsWith("CONNECT") && name == null && msg.length() >= 9 && msg.length() <= 40  && PlayerList.getPlayerCount() > \old(PlayerList.getPlayerCount());
    //@ requires msg.startsWith("CONNECT") || msg.startsWith("ACCEPT") || msg.startsWith("PING");
    //@ requires msg.startsWith("CONNECT") || msg.startsWith("ACCEPT") || msg.startsWith("PING");
    //@ requires msg.startsWith("CHAT") || msg.startsWith("DECLINE") || msg.startsWith("INVITE");
    //@ requires msg.startsWith("MOVE") || msg.startsWith("QUIT") || msg.startsWith("REQUEST");
    //@ requires msg.startsWith("LEADERBOARD") || msg.startsWith("LOBBY") || msg.startsWith("ERROR");
    
    private void parseMessage(String msg) {
        if (msg == null) {
            disconnect();
            return;
        }
        Scanner in = new Scanner(msg);
        if (!in.hasNext()) {
            in.close();
            return;
        }
        String cmd = in.next();
        if (name == null) {
            if (cmd.equals("CONNECT")) {
                if (in.hasNext()) {
                    String n = in.next();
                    if (n.length() >= 1 && n.length() <= 15) {
                        name = n;
                        if (PlayerList.addPlayer(n, this) == ReturnCode.NAME_TAKEN) {
                            sendError(ReturnCode.NAME_TAKEN, "Name taken! Try something else..");
                            Logging.log(socket.getInetAddress() + " tried to connect with the name " + n + " but it was taken.");
                            in.close();
                            name = null;
                            return;
                        }

                        Logging.log(socket.getInetAddress() + " connected with name " + name + ".");
                        writeLine("OK CHAT LEADERBOARD MULTIPLAYER SECURITY AWESOMENESS");
                    } else {
                        Logging.log(socket.getInetAddress() + " tried to connect but his/her name is invalid.");
                    }
                } else {
                    Logging.log(name + " supplied the wrong number of arguments in his " + cmd + " request.");
                    sendError(ReturnCode.UNKNOWN_FAIL, "You supplied the wrong number of arguments in your " + cmd + " request.");
                }
                in.close();
                return;
            }
            Logging.log(socket.getRemoteSocketAddress() + " didn't start with a CONNECT command!");
            sendError(ReturnCode.NAME_INVALID, "Please connect first!");
            disconnect();
        }
        if (cmd.equals("ACCEPT")) {
            if (in.hasNext()) {
                ReturnCode res = PlayerList.acceptInvite(in.next(), name);
                if (res == ReturnCode.NOT_FOUND) {
                    sendError(ReturnCode.NOT_FOUND, "You dont have an invite from him!");
                }
                if (res == ReturnCode.ALREADY_PLAYING) {
                    sendError(ReturnCode.ALREADY_PLAYING, "One of you is allready playing!");
                }
            } else {
                Logging.log(name + " supplied the wrong number of arguments in his " + cmd + " request.");
                sendError(ReturnCode.UNKNOWN_FAIL, "You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals("PING")) {
            writeLine("PONG");
        } else if (cmd.equals("CHAT")) {
            if (in.hasNextLine()) {
                String chat = name + ":" + in.nextLine();
                if (chat.length() > 512) {
                    sendError(ReturnCode.UNKNOWN_FAIL, "Messsage too large!");
                    in.close();
                    return;
                }
                Logging.log(chat);
                for (ThreadListener client : PlayerList.getPlayerMap().values()) {
                    if (client != this) {
                        client.sendChat(chat);
                    }
                }
            } else {
                Logging.log(name + " supplied the wrong number of arguments in his " + cmd + " request.");
                sendError(ReturnCode.UNKNOWN_FAIL, "You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals("DECLINE")) {
            if (in.hasNext()) {
                String inviter = in.next();
                Logging.log(name + " declines an invite from " + inviter + ".");
                if (PlayerList.decline(inviter, name) == ReturnCode.NOT_FOUND) {
                    sendError(ReturnCode.NOT_FOUND, "Invite not found! Consider it removed.");
                    Logging.log(name + " tried to decline an invite from " + inviter + " but the invite did not exist.");
                }
            } else {
                Logging.log(name + " supplied the wrong number of arguments in his " + cmd + " request.");
                sendError(ReturnCode.UNKNOWN_FAIL, "You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals("INVITE")) {
            if (in.hasNext()) {
                String invitee = in.next();
                Logging.log(name + " invites " + invitee);
                ReturnCode res = PlayerList.invite(name, invitee);
                if (res == ReturnCode.ALREADY_EXISTS) {
                    sendError(ReturnCode.ALREADY_EXISTS, "You allready invited him!");
                }
                if (res == ReturnCode.NOT_FOUND) {
                    sendError(ReturnCode.NOT_FOUND, "The player you are trying to invite can not be found!");
                }
            } else {
                Logging.log(name + " supplied the wrong number of arguments in his " + cmd + " request.");
                sendError(ReturnCode.UNKNOWN_FAIL, "You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals("MOVE")) {
            if (in.hasNextInt()) {
                int row = in.nextInt();
                if (game == null) {
                    sendError(ReturnCode.INVALID_MOVE, "You're not in a game!");
                    Logging.log(name + " tried to make a move while he's not in the game.");
                } else {
                    ReturnCode res = game.move(name, row);
                    if (res == ReturnCode.WRONG_PLAYER) {
                        sendError(ReturnCode.INVALID_MOVE, "Wait on your turn!");
                    }
                    if (res == ReturnCode.INVALID_MOVE) {
                        sendError(ReturnCode.INVALID_MOVE, "Invalid move! Try again.");
                        sendRequestMove();
                    }
                }
            } else {
                Logging.log(name + " supplied the wrong number of arguments in his " + cmd + " request.");
                sendError(ReturnCode.NOT_FOUND, "You supplied the wrong number of arguments in your " + cmd + " request.");
            }
        } else if (cmd.equals("QUIT")) {
            if (in.hasNext()) {
                Logging.log(name + " quits!(" + in.next());
            }
            disconnect();
        } else if (cmd.equals("REQUEST")) {
            sendBoard();
        } else if (cmd.equals("LEADERBOARD")) {
            sendLeaderBoard();
        } else if (cmd.equals("LOBBY")) {
            sendLobby(); 
        } else if (cmd.equals("ERROR")) {
        	if (in.hasNextLine()) {
                Logging.logError(in.nextLine());
        	}
            
        } else {
            Logging.log(name + " send an unknown command: " + msg);
            sendError(ReturnCode.UNKNOWN_FAIL, "Unknown request");
        }
        in.close();
    }
    /**
     * Add a mistake to a connection. Used to kick players after 5 mistakes
     */
    //@ requires isActive();
    //@ ensures allowedMistakes == 0 && isActive() == false;
    public synchronized void noteMistake() {
        sendErrorText("Mistakes happen. On this server we allow 5 mistakes. You have " + --allowedMistakes + " left.");
        if (allowedMistakes == 0) {
            sendErrorText("That was your final mistake. Bye.");
            disconnect();
        }
    }
    /**
     * Sends a packet.
     * @param packet
     */
    //@ requires isActive();
    //@ requires packet.length() >= 1;
    public synchronized void writeLine(String packet) {
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
     * Sends the leaderboard
     */
    //@  requires isActive();
    public void sendLeaderBoard() {
        Logging.log(name + " requested the leaderboard!");
        writeLine("LEADERBOARD " + PlayerList.getRankings());
    }
    /**
     * Sends lobby. 
     */
    //@  requires isActive();
    public void sendLobby() {
        Logging.log(name + " requested the lobby!");
        String lobby = "";
        try {
            for (String n : PlayerList.getPlayerMap().keySet()) {
                if (PlayerList.getPlayerMap().get(n).getGame() == null) {
                    lobby += n + " ";
                }
            }
        } catch (Exception e) {
            Logging.logError("Exception caught in sendLobby! " + e.getMessage());
        }
        writeLine("LOBBY " + lobby);
    }
    /**
     * 
     * @return name
     */
    //@ ensures name == getPlayerName();
    //@ pure;
    public String getPlayerName() {
        return name;
    }
    /**
     * Sets current game
     * @param g
     * @return Succes
     */
    //@ ensures game == g;
    
    public ReturnCode setGame(Game g) {
        game = g;
        return ReturnCode.SUCCES;
    }
    /**
     * 
     * @return game
     */

    //@ ensures game == getGame();
    public Game getGame() {
        return game;
    }
    /**
     * Sends board
     */
    //@ requires game != null;
    //@ requires isActive();
    public void sendBoard() {
        if (game == null) {
            Logging.log(name + " requested his board, but he is not in a game!");
            sendError(ReturnCode.NOT_FOUND, "You are not in a game silly!");
        } else {
            writeLine("BOARD " + game.getBoard());
        }
    }
    /**
     * Sends a request move
     */
    //@ requires game != null;
    //@ requires isActive();
    public void sendRequestMove() {
        if (game == null) {
            Logging.log("Trying to send move request while not in a game with " + name);
            return;
        }
        writeLine("REQUEST");
    }
    /**
     * Sends chat
     * @param chat
     */
    //@ requires chat.length() <= 512;
    //@ requires isActive();
    public void sendChat(String chat) {
        if (chat.length() <= 512) {
            writeLine("CHAT " + chat);
        }
    }
    /**
     * Sends move Ok
     * @param column
     */
    //@ requires isActive();
    //@ requires game != null;
    public void sendMoveOk(int column) {
        if (game == null) {
            Logging.log("Trying to send move ok while not in a game to " + name);
            return;
        }
        writeLine("MOVE " + ((game.getTurn()^1) + 1) + " " + column);
    }
    /**
     * sends game end
     * @param type
     * @param winner
     */
    //@ requires isActive();
    //@ requires game != null;
    //@ requires type.equals("DRAW") || type.equals("WIN") || type.equals("DISCONNECT"); 
    
    public void sendGameEnd(String type, String winner) {
        if (type != "DRAW" && type != "WIN" && type != "DISCONNECT") {
            Logging.logError("GameEnd type not correct(" + type + ") in " + name + " thread!");
        }
        writeLine("END " + type + " " + winner + "\n\n");
    }
    /**
     * Sends game end draw
     */
    //@ requires isActive();
    //@ requires game != null;
    public void sendGameEnd() {
        writeLine("END DRAW");
    }
    /**
     * Disconnects and cleans up the player
     */
    //@ requires isActive();
    public void disconnect() {
        if (isClosed) {
            return;
        }
        Logging.log("Disconnecting " + socket.getRemoteSocketAddress() + "(" + name + ")");
        isClosed = true;
        if (name != null) {
            PlayerList.cleanPlayer(name);
            if (game != null) {
                game.gameEndDC(name);
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            //Already disconnected, nothing to do
        }
        try {
            input.close();
        } catch (IOException e1) {
            Logging.logError("Couldn't close input on " + name);
        }
        try {
            output.close();
        } catch (IOException e1) {
            Logging.logError("Couldn't close output on " + name);
        }
    }
    /**
     * Send invite
     * @param inviter
     */
    //@ requires isActive();
    public void sendInvite(String inviter) {
        writeLine("INVITE " + inviter);
    }
    /**
     * Send startGame
     * @param player
     */
    //@ requires isActive();
    public void sendStartGame(String player1, String player2) {
        writeLine("START " + player1 + " " + player2);
    }
    /**
     * Sends error and notes a mistake. 
     */
    //@ requires isActive();
    //@ ensures allowedMistakes == \old(allowedMistakes)-1;
    public void sendError(ReturnCode type, String error) {
        noteMistake();
        if (error.length() <= 512) {
            writeLine("ERROR " + type + " " + error);
        } else {
            Logging.logError("Tried to send too long error message(" + error + ")");
        }
    }
    /**
     * Sends an error without noting a mistake.
     * @param error
     */
    //@ requires isActive();
    public void sendErrorText(String error) {
        if (error.length() <= 512) {
            writeLine("ERROR " + ReturnCode.UNKNOWN_FAIL + " " + error);
        } else {
            Logging.logError("Tried to send too long error message(" + error + ")");
        }
    }
}
