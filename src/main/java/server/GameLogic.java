package server;

import java.util.*;

/**
 * Thread that keeps track of connections and games
 * Used to time out players that aren't responding, leaving without quit
 * @author cotix
 *
 */
public class GameLogic extends Thread {
    private PlayerList playerList;
    private SynchronizedLinkedList<Game> gameList;
    private SynchronizedLinkedList<ThreadListener> threadSet;
    /**
     * Constructor
     */
    public GameLogic() {
        gameList = new SynchronizedLinkedList<Game>();
        playerList = new PlayerList(gameList);
        threadSet = new SynchronizedLinkedList<ThreadListener>();
    }
    /**
     * returns playerList 
     */
    public PlayerList getPlayerList() {
        return playerList;
    }
    /**
     * Adds a ThreadListener to the list
     * @param th
     */
    public void addThread(ThreadListener th) {
        threadSet.add(th);
    }
    /**
     * Entry point for the thread. Loops through the player and game list
     * Checks for disconnects and timeouts
     * 
     */
    public void run() {
        while (true) {
            List<Game> toRemove = new LinkedList<Game>();
            gameList.lock();
            for (Game item : gameList) {
                if (!item.isActive()) {
                    toRemove.add(item);
                    item.cleanup();
                    continue;
                }
                ThreadListener client = item.getTimeOut();
                if (client != null) {
                    client.sendError(ReturnCode.INVALID_MOVE, "Timeout!");
                    client.disconnect();
                }
            }
            gameList.unlock();
            threadSet.lock();
            List<ThreadListener> toCleanup = new LinkedList<ThreadListener>();
            for (ThreadListener thread : threadSet) {
                if (!thread.isActive()) {
                    toCleanup.add(thread);
                }
                if ((thread.getPlayerName() == null && thread.getConnectedTime() >= 5000)) {
                    thread.disconnect();
                }
                thread.writeLine("PING");
            }
            threadSet.unlock();
            threadSet.removeAll(toCleanup);
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                Logging.logError("GameLogic thread got interrupted? This is WRONG!");
            }
            gameList.removeAll(toRemove);
        }
    }
}
