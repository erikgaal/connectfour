package connectfour.ai;

import connectfour.Controller;
import connectfour.util.Logging;
import connectfour.objects.Board;

/**
 * Perfect AI strategy implemented by bruteforcing the board 42 steps deep.
 */
public class BaasAI implements Player {

    private Controller controller;
    private int colour;
    private MoveBook book;
    private Board board;
    private String moves = "";
    private boolean hasWon = false;

    /**
     * Constructs a BaasAI
     * @param controller
     * @param book a movebook
     */
    public BaasAI(Controller controller, String book) {
        this.controller = controller;
        this.book = new MoveBook(book);
        this.board = new Board();
        this.colour = 0;
    }

    public void move(int column, int colour) {
        board.move(column, colour);
        moves += column;
    }

    public int getHint() {
        return getBestMove();
    }

    public void getMove() {
        controller.move(getBestMove());
    }

    public int getColour() {
        return colour;
    }

    /**
     * Returns the best move, from the MoveBook or through calculations
     * @return board column
     */
    public int getBestMove() {
        if (moves.equals("")) {
            controller.sendChat("DO YOUR WORST! IT'S NO USE.");
            hasWon = true;
            colour = 1;
            return 3;
        } else {
            int best = -1;
            if (colour == 1 && board.getMoveCount() < 14) {
                best = book.getMove(book.getIndex(moves));
            }

            if (best == -1) {
                Logging.logVerbose("Checking for direct win");
                //Als we winnende move hebben, speel die
                for (int i = 0; i < Board.WIDTH; i++) {
                    if (board.move(i, colour)) {
                        Logging.log(i+" is a valid move");
                        if (board.hasWon(colour)) {
                            board.unmove(i, colour);
                            Logging.logVerbose("Winning with move " + i);
                            return i;
                        }
                        board.unmove(i, colour);
                    }
                }
                int depth = ((colour == 0) ? (board.getMoveCount()*2+4) : Board.HEIGHT*Board.WIDTH);
                if (controller.getDepth() != 42) {
                    depth = controller.getDepth();
                }
                Logging.log("Continuing on AlphaBeta with depth " + depth + " (" + (board.getMoveCount()+1) + ")");
                int bestScore = (int) (-1.05*Board.INF);
                for (int i = 0; i < Board.WIDTH; i++) {
                    if (board.move(Board.map[i], colour)) {
                        int score = -board.alphaBeta(depth, -Board.INF, Board.INF);
                        if (score > bestScore) {
                            bestScore = score;
                            best = Board.map[i];
                        }
                        board.unmove(Board.map[i], colour);
                    }
                }
                Logging.log("Best score: " + bestScore);
                if (bestScore == Board.INF && !hasWon) {
                    controller.sendChat("DO YOUR WORST! IT'S NO USE.");
                    hasWon = true;
                }
            }
            if (best == -1) {
                Logging.logError("Couldn't find a move! Picking first valid move now!");
                for (int i = 0; i != Board.WIDTH; i++) {
                    if (board.validMove(i)) {
                        Logging.log("Picked " + i);
                        return i;
                    }
                }
            }
            return best;
        }
    }
}
