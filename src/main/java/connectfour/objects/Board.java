package connectfour.objects;

/**
 * Board saved as two bitboards. This is WAY more efficient than using an Array.
 * Board is saved as two 64-bit longs. Each long represents one of the players.
 */
public class Board {

    // .  .   .   .   .   .   .
    // 5  12  19  26  33  40  47
    // 4  11  18  25  32  39  46
    // 3  10  17  24  31  38  45
    // 2  9   16  23  30  37  44
    // 1  8   15  22  29  36  43
    // 0  7   14  21  28  35  42
    public static final int WIDTH = 7;
    public static final int HEIGHT = 6;

    private long[] boards = new long[2];
    private int[] heights = new int[WIDTH];

    public static final int INF = 2000000;
    private static final long BOTTOMLONG = 4432676798593L;
    private static final long RIGHTLONG = 0x1FC0000000000L;
    public static final int[] map = {3, 1, 5, 2, 4, 0, 6};
    private long ABcount;
    private int turn;

    /**
     * Returns the two boards.
     * @return long[] boards
     */
    public long[] getBoards() {
        return boards;
    }

    /**
     * Returns wheter the given player has won.
     * @param colour colour of the player
     * @return true if the player has won
     */
    public boolean hasWon(int colour) {
        long board = boards[colour];

        long vertical = board & (board >> 1);
        long horizontal = board & (board >> HEIGHT + 1);
        long diagonal1 = board & (board >> HEIGHT + 2);
        long diagonal2 = board & (board >> HEIGHT);

        return (((vertical & (vertical >> 2)) != 0) ||
                ((horizontal & (horizontal >> (HEIGHT + 1) * 2)) != 0) ||
                ((diagonal1 & (diagonal1 >> (HEIGHT + 2) * 2)) != 0) ||
                ((diagonal2 & (diagonal2 >> (HEIGHT) * 2))) != 0);
    }

    /**
     * Returns a hash of the current board.
     * Add both boards, then add the bottom row and then add the first board again.
     * Also checks for mirroring when before turn 8.
     * @return hash
     */
    public long hash() {
        long hash = (boards[0] | boards[1]) + BOTTOMLONG + boards[0];
        if (turn >= 8) {
            return hash;
        }
        long hash1 = hash;
        long hash2 = 0L;
        for (int i = 0; i != WIDTH - 1; ++i) {
            hash2 |= (hash & RIGHTLONG);
            hash2 >>>= HEIGHT + 1;
            hash <<= HEIGHT + 1;
        }
        hash2 |= (hash & RIGHTLONG);
        return hash1 >= hash2 ? hash1 : hash2;
    }

    /**
     * Applies a move to the board
     * @param column board column
     * @param colour colour of the player
     * @return true if success
     */
    public boolean move(int column, int colour) {
        if (validMove(column)) {
            turn++;
            long bit = 1L << column * (HEIGHT + 1) + heights[column];
            boards[colour] ^= bit;
            heights[column]++;
            return true;
        }
        return false;
    }

    /**
     * Undoes a move from the board
     * @param column board column
     * @param colour colour of the player
     */
    public void unmove(int column, int colour) {
        turn--;
        heights[column]--;
        long bit = 1L << column * (HEIGHT + 1) + heights[column];
        boards[colour] ^= bit;
    }

    /**
     * Return if the move is valid
     * @param column board column
     * @return true if valid
     */
    public boolean validMove(int column) {
        return heights[column] < HEIGHT;
    }

    /**
     * Returns the turn
     * @return turn number
     */
    public int getMoveCount() {
        return turn;
    }

    /**
     * Applies AlphaBeta algorithm and returns the best move.
     * @param ply depth of the algorithm
     * @param alpha lowest value
     * @param beta highest value
     * @return best move
     */
    public int alphaBeta(int ply, int alpha, int beta) {
        int color = (turn & 1) ^ 1;
        if (hasWon(color)) {
            return INF;
        } else if (hasWon(color ^ 1)) {
            return -INF;
        }
        if (turn == 41 || ply == 0) {
            return 0;
        }
        int starta = alpha;
        long h = hash();
        int index = Transpositions.index(h);
        int flag, proof, bucket = -1;
        proof = Transpositions.proof(h);
        if (proof == Transpositions.getProof(index, 0)) {
            bucket = 0;
        } else if (proof == Transpositions.getProof(index, 1)) {
            bucket = 1;
        }
        if (bucket != -1) {
            flag = Transpositions.getFlag(index, bucket);
            if (flag != Transpositions.TT_EMPTY) {
                if (flag == Transpositions.TT_EXACT) {
                    return Transpositions.getScore(index, bucket);
                } else if (flag == Transpositions.TT_UPPERBOUND) {
                    beta = 0;
                    if (alpha >= beta) {
                        return Transpositions.getScore(index, bucket);
                    }
                } else if (flag == Transpositions.TT_LOWERBOUND) {
                    alpha = 0;
                    if (alpha >= beta) {
                        return Transpositions.getScore(index, bucket);
                    }
                }
            }
        }
        //Als we winnende move hebben, return INF
        for (int i = 0; i != 7; ++i) {
            if (move(i, color)) {
                if (hasWon(color)) {
                    unmove(i, color);
                    return INF;
                }
                unmove(i, color);
            }
        }

        int forcedMove = 8;
        for (int i = 0; i != 7; ++i) {
            if (move(map[i], color ^ 1)) {
                if (hasWon(color ^ 1)) {
                    if (forcedMove != 8) {
                        unmove(map[i], color ^ 1);
                        return -INF;
                    }
                    forcedMove = i;
                }
                unmove(map[i], color ^ 1);
            }

        }
        long oldABCount = ABcount++;
        int best = -INF - 100;
        int i = forcedMove;
        if (i == 8) {
            i = 0;
        }
        for (; i != 7 && i <= forcedMove; ++i) {
            if (!move(map[i], color)) {
                continue;
            }
            int s = -alphaBeta(ply - 1, -beta, -alpha);
            if (s >= best) {
                best = s;
            }
            alpha = Math.max(alpha, s);
            unmove(map[i], color);
            if (alpha >= beta) {
                break;
            }
        }
        flag = Transpositions.TT_EXACT;
        if (best <= starta) {
            flag = Transpositions.TT_UPPERBOUND;
        } else if (best >= beta) {
            flag = Transpositions.TT_LOWERBOUND;
        }
        Transpositions.saveTransposition(hash(), best, turn, flag, (ABcount - oldABCount));

        return best;
    }
}
