package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Log class. Used to print debugging or logging information
 * @author cotix
 *
 */
public class Logging {
	/**
	 * Intern function used to print text to the screen and prefix the time
	 * @param text
	 */
	static private void print(String text) {
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	System.out.println(sdf.format(cal.getTime())+" " + text);
	}
	/**
	 * Printing an error
	 * @param text
	 */
	static synchronized void logError(String text) {
		if(text.length() <= 1024) {
			print("error: " + text);
		}
	}
	/**
	 * printing a log message
	 * @param text
	 */
	static synchronized void log(String text) {
		if(text.length() <= 1024) {
			print("log: " + text);
		}
	}

	static synchronized  void logGame(String p1, String p2, String moves, String winner) {
		log(p1 + " vs " + p2 + " (winner: " + winner + ", moves: " + moves.length() + ")");
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("games.txt", true)));
			out.println(sdf.format(cal.getTime())+ " " + p1 + " vs " + p2 + " (winner: " + winner + ", moves: " + moves.length() + ") " + moves);
			out.flush();
			out.close();
		}catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}
}
