package connectfour;

import connectfour.ai.BaasAI;
import connectfour.ai.HumanPlayer;
import connectfour.ai.Player;
import connectfour.gui.*;
import connectfour.net.Connection;
import connectfour.net.ReturnCode;
import connectfour.objects.Board;
import connectfour.objects.Transpositions;
import connectfour.opengl.Game;
import connectfour.util.Logging;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

/**
 * Heart of the Game, starts the game and controls EVERYTHING.
 */
public class Controller {

    private Connection connection;

    private Menu menuFrame;
    private AddressQuery addressQueryFrame;
    private Lobby lobbyFrame;
    private Leaderboard leaderboardFrame;
    private Chat chatFrame;

    private Game game;

    private Board board;

    private Player player;
    private String name;

    private boolean ai;
    private int depth = -1;

    public Controller() {
        menuFrame = new Menu(this);
        addressQueryFrame = new AddressQuery(this);
        lobbyFrame = new Lobby(this);
        leaderboardFrame = new Leaderboard(this);
        chatFrame = new Chat(this);
    }

    public void run() {
        menuFrame.setVisible(true);
    }

    /**
     * Destroys all the Frames and exits the application
     */
    public void shutdown() {
        System.exit(0);
    }

    /**
     * Sets the visibility of a frame
     * @param frame   a JFrame
     * @param visible true or false
     */
    public void setVisible(JFrame frame, boolean visible) {
        frame.setVisible(visible);
    }

    /**
     * Returns the AddresQuery JFrame
     * @return addressQueryFrame
     */
    public AddressQuery getAddressQueryFrame() {
        return addressQueryFrame;
    }

    /**
     * Returns the Chat JFrame
     * @return chatFrame
     */
    public Chat getChatFrame() {
        return chatFrame;
    }

    /**
     * Returns the Lobby JFrame
     * @return lobbyFrame
     */
    public Lobby getLobbyFrame() {
        return lobbyFrame;
    }

    /**
     * Returns the Leaderboard JFrame
     * @return leaderboardFrame
     */
    public Leaderboard getLeaderboardFrame() {
        return leaderboardFrame;
    }

    /**
     * Returns the name filled in the connection dialog
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Player object
     * @return player
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * Returns the depth of the AI
     * @return depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the current Board
     * @return board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Connects to the given hostname and port combination and sends a <code>CONNECT</code> packet.
     * @param host hostname or ip-address to connect to
     * @param port portnumber 1-65535
     * @param name String of 1-31 characters
     */
    public void connect(String host, int port, String name) {
        Socket socket;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            System.err.println("Cannot connect to " + host + ":" + port);
            return;
        }
        try {
            this.name = name;
            connection = new Connection(socket, name, this);
            connection.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        addressQueryFrame.setVisible(false);
        menuFrame.setVisible(false);
        lobbyFrame.setVisible(true);
        lobbyFrame.setTitle(host);
    }

    /**
     * Disconnects the player from the game (and server)
     */
    public void disconnect() {
        connection.quit();
        menuFrame.setVisible(true);
        chatFrame.clearMessages();
        chatFrame.setVisible(false);
        leaderboardFrame.setVisible(false);
        lobbyFrame.setVisible(false);
    }

    /**
     * Shows an invite on the view, called by the connection.
     * @param data name of the inviter
     * @return true if success
     */
    public boolean showInvite(String data) {
        if (game == null) {
            lobbyFrame.showInvite(data);
            return true;
        } else {
            connection.declineInvite(data);
            return false;
        }
    }

    /**
     * Starts a new game, called by the connection.
     * @return true if success
     */
    public boolean gameStart() {
        if (game == null) {
            board = new Board();
            if (ai) {
                Logging.log("Starting as AI");
                player = new BaasAI(this, "14ply.book");
            } else {
                Logging.log("Starting as Human");
                player = new HumanPlayer(this);
            }
            game = new Game(this);
            game.start();
            return true;
        } else {
            return false;
        }
    }

    public void invite(String player) {
        connection.invite(player);
    }

    public void acceptInvite(String player) {
        connection.acceptInvite(player);
    }

    /**
     * Declines the invite of a player.
     * @param player name of the Player
     */
    public void declineInvite(String player) {
        connection.declineInvite(player);
    }

    /**
     * Called by the connection when a winner has been announced.
     * @param winner name of the Player who won
     * @param reason reason of the game ending
     */
    public void gameEnd(String winner, String reason) {
        if (game != null) {
            Transpositions.resetTrans();
            game.gameEnd();
            Logging.log(winner + " has won!");
            board = null;
            game = null;
            connection.requestLeaderboard();
        }
    }

    /**
     * Called by the connect when the game ended without a winner.
     * @param reason reason of the game ending
     */
    public void gameEnd(String reason) {
        if (game != null) {
            Transpositions.resetTrans();
            game.gameEnd();
            Logging.log("Game ended in a draw!");
            board = null;
            game = null;
            connection.requestLeaderboard();
        }
    }

    /**
     * Send a move to the connection
     * @param column board column
     */
    public boolean move(int column) {
        if (board.validMove(column)) {
            connection.move(column);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Send the REQUEST to the view
     * @return true if success
     */
    public boolean requestMove() {
        if (game != null) {
            player.getMove();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Confirms the last move, called by the connection.
     * @param colour colour of the player
     * @param column column of the board
     * @return true if success
     */
    public boolean moveOk(int colour, int column) {
        if (game != null) {
            board.move(column, colour);
            player.move(column, colour);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Requests the leaderboard from the connection.
     */
    public void requestLeaderboard() {
        connection.requestLeaderboard();
    }

    /**
     * Send a chat message to the connection
     * @param message
     */
    public void sendChat(String message) {
        connection.chat(message);
    }

    /**
     * Updates the lobby view with a Set of player names
     * @param players the Set&lt;String&gt; of player names
     */
    public void updateLobby(Set<String> players) {
        lobbyFrame.updateList(players);
    }

    /**
     * Sets whether to use an AI or a monkey
     * @param ai
     */
    public void setAI(boolean ai) {
        this.ai = ai;
    }

    /**
     * Sets the "strength" of the AI.
     * @param depth how many moves to calculate, (between 2 and 42)
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }
}