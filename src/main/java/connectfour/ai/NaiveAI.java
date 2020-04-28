package connectfour.ai;

import connectfour.Controller;
import connectfour.objects.Board;

import java.util.Random;

/**
 * AI implementation that returns the best move decided by a fair dice roll. - http://xkcd.com/221/
 */
public class NaiveAI implements Player {

    private final Controller controller;
    private int colour;
    private final Random random = new Random();
    private final Board board = new Board();
    private String moves = "";

    public NaiveAI(Controller controller) {
        this.controller = controller;
        this.colour = 0;
    }

    public void getMove() {
        controller.move(getBestMove());
    }

    public int getHint() {
        return getBestMove();
    }

    /**
     * Returns the "best" move.
     * @return column
     */
    public int getBestMove() {
        int column = 3;
        while (!board.validMove(column))
            column = random.nextInt(7);

        return column;
    }

    public int getColour() {
        return colour;
    }

    public void move(int column, int colour) {
        moves += column;
        board.move(column, colour);
    }
}
