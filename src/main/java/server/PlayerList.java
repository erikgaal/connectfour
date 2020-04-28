package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Static class used to keep track of all players and invites
 * This is the main point were everything comes together.
 * @author cotix
 *
 */
public class PlayerList {
    private /*@ spec_public @*/ static Map<String, ThreadListener> playerMap;
    private /*@ spec_public @*/ static Map<String, Set<String>> inviteMap;
    private /*@ spec_public @*/ static int playerCount;
    private /*@ spec_public @*/ static SynchronizedLinkedList<Game> gameList;
    private /*@ spec_public @*/ static EloRanking rankings;
    //@ ensures playerMap != null && inviteMap != null && playerCount == 0 && rankings != null && gameList == games;
    //@ requires games != null;
    public PlayerList(SynchronizedLinkedList<Game> games) {
        playerMap = new ConcurrentHashMap<String, ThreadListener>();
        inviteMap = new ConcurrentHashMap<String, Set<String>>();
        playerCount = 0;
        rankings = new EloRanking();
        gameList = games;
    }

    /**
     * Returns player map
     * @return player map
     */
    //@ ensures \result == playerMap;
    //@ pure
    public static Map<String, ThreadListener> getPlayerMap() {
        return playerMap;
    }

    /**
     * Adds a player
     * @param name
     * @param sock
     * @return ReturnCode
     */
    //@ requires name != null && name.length() > 31 || name.isEmpty();
    //@ ensures \result == ReturnCode.SUCCES && playerCount == \old(playerCount+1);
    
    public static ReturnCode addPlayer(String name, ThreadListener sock) {
        if (name == null) {
            Logging.logError("Tried adding a player with a null string!");
            return ReturnCode.NAME_INVALID;
        }
        if (name.length() > 31 || name.isEmpty()) {
            return ReturnCode.NAME_INVALID;
        }
        if (playerMap.containsKey(name)) {
            return ReturnCode.NAME_TAKEN;
        }
        if (playerMap.containsValue(sock)) {
            return ReturnCode.NAME_ALREADY_SET;
        }
        if (rankings.addPlayer(name) != ReturnCode.SUCCES) {
        	Logging.logError("Unkown fail trying to add " + name + " to the rankings!");
        	return ReturnCode.UNKNOWN_FAIL;
        }
        playerMap.put(name, sock);
        playerCount++;
        sendLobbyToAll();
        return ReturnCode.SUCCES;
    }

    /**
     * Sends the lobby to all connected clients
     */
    //@ requires playerCount >= 1;
    //@ pure
    
    public static void sendLobbyToAll() {
        String names = "";
        for (ThreadListener player : playerMap.values()) {
            names += " " + player.getPlayerName();
        }
        for (ThreadListener player : playerMap.values()) {
            player.writeLine("LOBBY" + names);
        }

    }

    /**
     * CLeans up a player
     * @param name
     * @return ReturnCode
     */
    //@ requires name != null;
    //@ ensures !playerMap.containsKey(name) && playerCount == \old(playerCount)-1;
    
    public static ReturnCode cleanPlayer(String name) {
        if (!playerMap.containsKey(name)) {
            return ReturnCode.SUCCES_NOT_FOUND;
        }
        rankings.removePlayer(name);
        playerMap.remove(name);
        inviteMap.remove(name);
        for (Set<String> set : inviteMap.values()) {
            if (set.contains(name)) {
                set.remove(name);
            }
        }
        playerCount--;
        sendLobbyToAll();
        return ReturnCode.SUCCES;
    }

    /**
     * Creates an invite
     * @param inviter
     * @param invitee
     * @return ReturnCode
     */
    //@ requires inviter != null && invitee != null;
    //@ requires !inviteMap.containsKey(inviter) && playerMap.containsKey(invitee) && playerMap.containsKey(inviter) ;
    //@ requires !(playerMap.get(inviter).getGame() != null || playerMap.get(invitee).getGame() != null);
    //@ requires inviteMap.containsKey(inviter);
    //@ ensures \result == ReturnCode.SUCCES && inviteMap.get(inviter).contains(invitee);
    
    public static ReturnCode invite(String inviter, String invitee) {
        if (invitee == null || inviter == null) {
            return ReturnCode.NOT_FOUND;
        }
        if (inviter.equals(invitee)) {
            return ReturnCode.NOT_FOUND;
        }
        if (inviteMap.containsKey(inviter)) {
            if (inviteMap.get(inviter).equals(invitee)) {
                return ReturnCode.ALREADY_EXISTS;
            }
        }
        if (!playerMap.containsKey(invitee)) {
            return ReturnCode.NOT_FOUND;
        }
        if (!playerMap.containsKey(inviter)) {
            Logging.logError("Unknown fail in invite!");
            return ReturnCode.UNKNOWN_FAIL;
        }
        if (playerMap.get(inviter).getGame() != null ||
        	playerMap.get(invitee).getGame() != null) {
        	return ReturnCode.ALREADY_PLAYING;
        }
        if (!inviteMap.containsKey(inviter)) {
            inviteMap.put(inviter, new HashSet<String>());
        }
        inviteMap.get(inviter).add(invitee);
        playerMap.get(invitee).sendInvite(inviter);
        return ReturnCode.SUCCES;
    }

    /**
     * Declines and thus removes an invite
     * @param inviter
     * @param invitee
     * @return
     */
    //@ requires inviter != null && invitee != null;
    //@ requires inviteMap.containsKey(inviter);
    
    public static ReturnCode decline(String inviter, String invitee) {
        if (inviter == null || invitee == null) {
            return ReturnCode.NOT_FOUND;
        }
        if (inviteMap.containsKey(inviter)) {
            if (inviteMap.get(inviter).contains(invitee)) {
                inviteMap.get(inviter).remove(invitee);
                return ReturnCode.SUCCES;
            }
        }
        if (inviteMap.containsKey(invitee)) {
            if (inviteMap.get(invitee).contains(inviter)) {
                inviteMap.get(invitee).remove(inviter);
                return ReturnCode.SUCCES;
            }
        }
        return ReturnCode.NOT_FOUND;

    }
    /**
     * Accepts invite
     * @param player1
     * @param player2
     * @return
     */
    //@ requires !(player1 == null || player2 == null) && inviteMap.get(player1) != null && inviteMap.get(player1).contains(player2);
    
    public static ReturnCode acceptInvite(String player1, String player2) {
        if (player1 == null || player2 == null) {
            return ReturnCode.NOT_FOUND;
        }
        if (inviteMap.get(player1) == null) {
            return ReturnCode.NOT_FOUND;
        }
        if (inviteMap.get(player1).contains(player2)) {
            return startGame(player1, player2);
        } else {
            return ReturnCode.NOT_FOUND;
        }
    }
    
    /**
     * Returns leaderboard string
     * @return String
     */
    //@ pure
    
    public static String getRankings() {
    	return rankings.getLeaderBoard();
    }
    /**
     * Starts a game, adds all necesary things to all the lists and sets etc.
     * @param player1
     * @param player2
     * @return ReturnCode
     */
    //@ requires !(player1 == null || player2 == null);
    //@ requires !(!playerMap.containsKey(player1) || !playerMap.containsKey(player2));
    //@ requires !(playerMap.get(player1).getGame() != null || playerMap.get(player2).getGame() != null);
    //@ ensures playerMap.get(player1).getGame() != null && playerMap.get(player2).getGame() != null;
    
    public static ReturnCode startGame(String player1, String player2) {
        if (player1 == null || player2 == null) {
            return ReturnCode.NOT_FOUND;
        }
        if (!playerMap.containsKey(player1) || !playerMap.containsKey(player2)) {
            return ReturnCode.NOT_FOUND;
        }
        if (playerMap.get(player1).getGame() != null || playerMap.get(player2).getGame() != null) {
            return ReturnCode.ALREADY_PLAYING;
        }
        Game game = new Game((short) 7, (short) 6, playerMap.get(player1), playerMap.get(player2));

        gameList.add(game);
        playerMap.get(player1).sendStartGame(player1, player2);
        playerMap.get(player2).sendStartGame(player1, player2);
        playerMap.get(player1).setGame(game);
        playerMap.get(player2).setGame(game);
        playerMap.get(player1).sendRequestMove();
        Logging.log(player1 + " starts a game with " + player2);
        if (inviteMap.containsKey(player1)) {
            inviteMap.get(player1).remove(player2);
            return ReturnCode.SUCCES;
        }
        return ReturnCode.SUCCES_NO_INVITE;
    }

    /**
     * updates the rankings
     * @param winner
     * @param loser
     * @param isDraw
     */
    //@ requires winner != null && loser != null;
    
    public static void finishGame(String winner, String loser, boolean isDraw) {
    	rankings.finishGame(winner, loser, isDraw);
    }
    /**
     * 
     * @return playerCount
     */
    //@ pure
    
    public static int getPlayerCount() {
        return playerCount;
    }

}
