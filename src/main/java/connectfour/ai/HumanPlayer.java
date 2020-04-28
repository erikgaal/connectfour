package connectfour.ai;

import connectfour.Controller;
import connectfour.objects.Board;

/**
 * Player controlled by human input.
 */
public class HumanPlayer implements Player {

    private final Controller controller;
    private int colour;
    private final NaiveAI hintAI;
    private int hint;
    private String moves = "";

    private boolean turn;

    public HumanPlayer(Controller controller) {
        this.controller = controller;
        this.colour = 0;
        hintAI = new NaiveAI(controller);
    }

    public void move(int column, int colour) {
        moves += column;
        hintAI.move(column, colour);
    }

    public int getHint() {
        if (hint == -1 && turn) {
            return hintAI.getHint();
        }
        return hint;
    }

    public void getMove() {
        if (moves.equals("")) {
            colour = 1;
        }
        turn = true;
    }

    public int getColour() {
        return colour;
    }

    /**
     * Called by the controller from the GUI to make a move if it's their turn.
     * @param column column to make the move on
     */
    public void makeMove(int column) {
        if (turn) {
            if (controller.move(column)) {
                turn = false;
                hint = -1;
            }
        }
    }
}
