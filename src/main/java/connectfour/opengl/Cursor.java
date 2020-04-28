package connectfour.opengl;

/**
 * Cursor used by the Human player
 */
public class Cursor {
    private int position;

    public Cursor() {
        this.position = 0;
    }

    /**
     * Moves the cursor one to the left.
     */
    public void moveLeft() {
        position = position != 0 ? (position - 1) % 7 : 6;
    }

    /**
     * Moves the cursor one to the right.
     */
    public void moveRight() {
        position = position != 6 ? (position + 1) % 7 : 0;
    }

    /**
     * Returns the position of the cursor.
     * @return position
     */
    public int getPosition() {
        return position;
    }
}
