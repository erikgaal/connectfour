package connectfour.util;

/**
 * Timer used by the OpenGL context to become framerate independent.
 */
public class Timer {
    private float delta;
    private double time;
    private int fps;
    private double lastFrame;
    private double lastFPS;

    /**
     * Starts the timer.
     */
    public void init() {
        lastFrame = getTime();
        lastFPS = getTime();
    }

    /**
     * Returns the current time.
     * @return time
     */
    public double getTime() {
        return System.nanoTime() / 1000000000D;
    }

    /**
     * Logs the last FPS.
     */
    private void printFPS() {
        if (getTime() - lastFPS >= 1) {
            Logging.logFPS(fps);
            fps = 0;
            lastFPS++;
        }
    }

    /**
     * Update the timer.
     */
    public void update() {
        fps++;
        time = getTime();
        delta = (float) (time - lastFrame);
        lastFrame = time;
        printFPS();
    }
}