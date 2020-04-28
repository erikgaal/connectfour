package connectfour.net;

/**
 * Packets as defined in the protocol.
 */
public class Packet {

    // Client Packets
    public static final String ACCEPT_INVITE = "ACCEPT";
    public static final String CHAT = "CHAT";
    public static final String CONNECT = "CONNECT";
    public static final String DECLINE_INVITE = "DECLINE";
    public static final String ERROR = "ERROR";
    public static final String INVITE = "INVITE";
    public static final String MOVE = "MOVE";
    public static final String PING = "PING";
    public static final String QUIT = "QUIT";
    public static final String REQUEST_BOARD = "REQUEST";
    public static final String REQUEST_LEADERBOARD = "LEADERBOARD";
    public static final String REQUEST_LOBBY = "LOBBY";

    // Server Packets
    public static final String ACCEPT_CONNECT = "OK";
    public static final String BOARD = "BOARD";
    public static final String GAME_END = "END";
    public static final String GAME_START = "START";
    public static final String LEADERBOARD = "LEADERBOARD";
    public static final String LOBBY = "LOBBY";
    public static final String MOVE_OK = "MOVE";
    public static final String PONG = "PONG";
    public static final String REQUEST_MOVE = "REQUEST";
    public static final String EASTEREGG = "SER4";
}
