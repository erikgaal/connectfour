package server;
/**
 * 
 * @author cotix
 * Class that holds the state of a game. Every game gets its own Game instance
 */
public class Game {
	private /*@ spec_public @*/ long[] board = {0,0}; 
	private /*@ spec_public @*/ int[] rowCount = {0,0,0,0,0,0,0}; 
    private /*@ spec_public @*/ short maxHeight;
    private /*@ spec_public @*/ int moveCount;
    private /*@ spec_public @*/ short maxWidth;
    private /*@ spec_public @*/ ThreadListener[] players;
    private /*@ spec_public @*/ long moveTimer;
    private /*@ spec_public @*/ boolean active;
    private /*@ spec_public @*/ String moves;
    /**
     * Constructor. 
     * @param x
     * @param y
     * @param p1
     * @param p2
     */
    //@ requires x >= 4 && y >= 4 && p1 != null && p2 != null;
    //@ ensures moveCount == 0 && moves.equals("") && players[0] == p1 && players[1] == p2;
    //@ ensures maxHeight == y && maxWidth == x && active == true && moveTimer-System.currentTimeMillis() == 15000;
    
    public Game(short x, short y, ThreadListener p1, ThreadListener p2) {
        moveCount = 0;
        moves = "";
        players = new ThreadListener[2];
        players[0] = p1;
        players[1] = p2;
        maxHeight = y;
        maxWidth = x;
        active = true;
        moveTimer = System.currentTimeMillis() + 15 * 1000;
    }
    /**
     * Removes the game
     */
    //@ ensures players[0].getGame() == null && players[1].getGame() == null;
    public void cleanup() {
        players[0].setGame(null);
        players[1].setGame(null);
    }
    /**
     * 
     * @return Who's turn it is
     */
    //@ requires isActive();
    //@ pure
    public int getTurn() {
        return moveCount & 1;
    }
    /**
     * 
     * @return isActive
     */
    //@ pure
    public boolean isActive() {
        return active;
    }

    /**
     * 
     * @return returns the player if he timed out, returns null if he didnt
     */
    //@ ensures \result != null && System.currentTimeMillis() > moveTimer && \result == players[getTurn()];
    //@ pure;
    public ThreadListener getTimeOut() {
        if (System.currentTimeMillis() > moveTimer) {
            return players[getTurn()];
        }
        return null;
    }
    /**
     * checks for a win
     * @param c
     * @return boolean
     */
    //@ requires isActive();
    public boolean hasWon(int c){
	    long diag = board[c] & (board[c]>>6);
	    long hor = board[c] & (board[c]>>7);
	    long diag2 = board[c] & (board[c]>>8);
	    long ver = board[c] & (board[c]>>1);
	    return ((diag & (diag >> 2*6)) |(hor & (hor >> 2*7)) |(diag2 & (diag2 >> 2*8)) | (ver & (ver >> 2))) != 0;
	}
    
    /**
     * stops the game with a win or disconnect
     * @param type
     * @param player
     */
    //@ requires hasWon(0)|hasWon(1);
    //@ ensures active == false && players[0].getGame() == null && players[1].getGame() == null;
    public synchronized void gameEnd(String type, int player) {
        if (type.equals("DRAW")) {
            Logging.logError("Calling gameEnd draw with a winning player? Sending draw instead.");
            gameEnd();
            return;
        }
        Logging.logGame(players[0].getPlayerName(), players[1].getPlayerName(), moves, players[player].getPlayerName());
        PlayerList.finishGame(players[player].getPlayerName(), players[player^1].getPlayerName(), false);
        active = false;
        players[player].sendGameEnd(type, players[player].getPlayerName());
        players[player ^ 1].sendGameEnd(type, players[player].getPlayerName());
        players[0].setGame(null);
        players[1].setGame(null);
    }
    /**
     * Ends the game in a draw
     */
    //@ requires moveCount >= 41 && !(hasWon(0)|hasWon(1));
    //@ ensures active == false && players[0].getGame() == null && players[1].getGame() == null;
    public synchronized void gameEnd() {
        Logging.logGame(players[0].getPlayerName(), players[1].getPlayerName(), moves, "DRAW");
        active = false;
        PlayerList.finishGame(players[0].getPlayerName(), players[1].getPlayerName(), true);
        players[0].sendGameEnd();
        players[1].sendGameEnd();
        players[0].setGame(null);
        players[1].setGame(null);
    }
    /**
     * Ends the game with a DC
     * @param name
     */
    //@ requires (players[0].getPlayerName().equals(name) && !players[0].isActive()) || (players[1].getPlayerName().equals(name) && !players[1].isActive());
    //@ ensures active == false && players[0].getGame() == null && players[1].getGame() == null;
    public synchronized void gameEndDC(String name) {
        if (players[0].getPlayerName().equals(name)) {
            gameEnd("DISCONNECT", 1);
        } else if (players[1].getPlayerName().equals(name)) {
            gameEnd("DISCONNECT", 0);
        } else {
            Logging.log("Called gameEndDC with wrong player name!");
        }
    }
    /**
     * Generates board string
     * @return String
     */
    //@ requires isActive();
    public String getBoard() {
        String res = "";
        for (int y = maxHeight - 1; y >= 0; --y) {
            for (int x = 0; x != maxHeight; ++x) {
                String c = "0";
                if (((board[0]>>>(y+x*7))&1) == 1){
                	c = "1";
                } else if (((board[1]>>>(y+x*7))&1) == 1){
                	c = "2";
                }
            	res += c + " ";
            }
            res += "\n";
        }
        return res;
    }
    
    /**
     * Puts a piece in the row
     * @param pos
     * @param c
     * @return if valid move returns true otherwise false
     */
    //@ requires isActive();
    //@ ensures (board[c]&(1L<<(7*pos+rowCount[pos]))) != 0;
    public boolean put(int pos, int c){
        if (!(0 <= pos && pos <= 6))
            return false;
		if (rowCount[pos] == 6){
			return false;
		}
		moveCount++;
        moves += pos;
		long bit = 1L << (7*pos);
		board[c] ^= (bit<<rowCount[pos]);
		rowCount[pos]++;
		return true;
	}
    
    /**
     * Makes move
     * @param player
     * @param row
     * @return ReturnCode
     */
    //@ requires player != null && row >= 0 && row <= 6;
    //@ requires isActive();
    //@ ensures \result == ReturnCode.SUCCES && moveCount == \old(moveCount)+1;
    //@ ensures \result == ReturnCode.HAS_WON && hasWon(\old(getTurn()));
    public synchronized ReturnCode move(String player, int row) {
        if (!players[getTurn()].getPlayerName().equals(player)) {
            Logging.log("Wrong player makes a move in the game between: " + players[0].getPlayerName() + " vs " + players[1].getPlayerName());
            return ReturnCode.WRONG_PLAYER;
        }
        if (moveCount == 41) {
            gameEnd();
        }
        if (put(row, getTurn()) == false) {
            return ReturnCode.INVALID_MOVE;
        }
        players[getTurn() ^ 1].sendMoveOk(row);
        players[getTurn()].sendMoveOk(row);

        moveTimer = System.currentTimeMillis() + 15 * 1000;
        if (hasWon(getTurn()^1)) {
            gameEnd("WIN", getTurn()^1);
            return ReturnCode.HAS_WON;
        }
        if (active) {
            players[getTurn()].sendRequestMove();
        }
        return ReturnCode.SUCCES;
    }
}
