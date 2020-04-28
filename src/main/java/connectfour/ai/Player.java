package connectfour.ai;

/**
 * Interface implemented by the HumanPlayer and the BaasAI.
 */
public interface Player {

    /**
     * Lets the player know that it's their turn
     */
    public void getMove();

    /**
     * Used by the human player to show the best possible move
     * @return board column
     */
    public int getHint();

    /**
     * Returns the colour of the player
     * @return colour
     */
    public int getColour();

    /**
     * Updates the board in the player class with a move
     * @param column board column
     * @param colour colour of the player
     */
    public void move(int column, int colour);
}
