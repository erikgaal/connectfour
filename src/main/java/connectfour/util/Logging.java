package connectfour.util;

/**
 * Logging class to route all the logging.
 */
public class Logging {

    private static final int VERBOSE = 0;
    private static final int INFO = 1;
    private static final int ERROR = 2;
    private static final int FPS = -1;

    private static int level = VERBOSE;

    public static void setLevel(int level) {
        Logging.level = level;
    }

    public static synchronized void logError(String text) {
        if (ERROR <= level)
            System.out.println("[ERROR]: " + text);
    }

    public static synchronized void logVerbose(String text) {
        if (VERBOSE >= level)
            System.out.println("[VERBOSE]: " + text);
    }

    public static synchronized void log(String text) {
        if (INFO >= level)
            System.out.println("[INFO]: " + text);
    }

    public static synchronized void logFPS(int fps) {
        if (FPS >= level)
            System.out.println("[FPS]: " + fps);
    }
}