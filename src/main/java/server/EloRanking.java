package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author cotix
 * Ranking class, keeps track of ELO rankings and win/losses
 */
public class EloRanking {
	private Map<String,Integer[]> rankings;
	private static final int STARTELO = 1000;
	
	public EloRanking() {
		rankings = new ConcurrentHashMap<String,Integer[]>();
	}
	
	/***
	 * Adds a player to the ranking list and sets ELO rating to 1000
	 * @param player
	 * @return ReturnCode, succes or not.
	 */
	public synchronized ReturnCode addPlayer(String player) {
		if (rankings.containsKey(player)) {
			return ReturnCode.ALREADY_EXISTS;
		}
		Integer[] array = new Integer[4];
		array[3] = STARTELO;
		array[0] = 0;
		array[1] = 0;
		array[2] = 0;
		
		rankings.put(player, array);
		return ReturnCode.SUCCES;
	}
	/**
	 * Removes player from the ranking list
	 * @param player
	 * @return ReturnCode, succes or not
	 */
	public ReturnCode removePlayer(String player) {
		if (!rankings.containsKey(player)) {
			return ReturnCode.NOT_FOUND;
		} else {
			rankings.remove(player);
			return ReturnCode.SUCCES;
		}
	}
	/**
	 * Parameter needed for adjustment of ratings
	 * @param rating
	 * @param opponentRating
	 * @return expectedScore
	 */
	private double getExpected (int rating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10.0, ((double) (opponentRating - rating) / 400.0)));
	}

	/**
	 * Adjusts ratings and wins/loses stats
	 * @param winner
	 * @param loser
	 * @param draw
	 * @return Returns ReturnCode
	 */
	public ReturnCode finishGame(String winner, String loser, boolean draw) {
		if (!rankings.containsKey(winner) ||
			!rankings.containsKey(loser)) {
			return ReturnCode.NOT_FOUND;
		}
		Integer[] rating = rankings.get(winner);
		Integer[] rating2 = rankings.get(loser);
		if (!draw) {
			double exp = getExpected(rating[3], rating2[3]);
			rating[3] +=(int)( 32 * (1 - exp));
			rating2[3] += (int)(32 * (0 - exp));
			rating[0]++;
			rating2[1]++;
		}
		rating[2]++;
		rating2[2]++;
		rankings.put(winner, rating);
		rankings.put(loser, rating2);
		return ReturnCode.SUCCES;
	}
	/**
	 * Generates the leaderboard String
	 * @return String
	 */
	public String getLeaderBoard() {
		String res = "";
		for (Map.Entry<String, Integer[]> entry : rankings.entrySet()) {
			Integer rating[] = entry.getValue();
			String name = entry.getKey();
			res += name + " " + rating[0] + " " + rating[1] + " " + rating[2] + " " + rating[3] + " ";
		}
		return res;
	}
	
}
