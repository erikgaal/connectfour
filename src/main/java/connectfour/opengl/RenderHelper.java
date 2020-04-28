package connectfour.opengl;

import connectfour.objects.Board;
import connectfour.util.Logging;

import java.awt.*;

public class RenderHelper {

    private static final int QUALITY = 32;
    private static final int COINSIZE = 3;
    private static final float COINDEPTH = 0.3f;
    private static final int COINRED = 1;
    private static final int COINYELLOW = 2;

    /**
     * Draws the board
     * @param board board object
     */
    public static void drawBoard(Board board) {
        if (board == null) {
            Logging.logError("Trying to draw a board while not in game");
            return;
        }
        long[] boards = board.getBoards();
        for (int n = 0; n < (Board.HEIGHT + 1) * Board.WIDTH; n++) {
            long copy = boards[COINRED - 1] | boards[COINYELLOW - 1];
            int x = n / (Board.HEIGHT + 1);
            int y = n % (Board.HEIGHT + 1);
            if ((copy >> n & 1) == 1) {
                Color color;
                if ((boards[COINRED - 1] >> n & 1) == 1) {
                    color = Color.RED;
                } else {
                    color = Color.YELLOW;
                }
                Geometry.drawDisk(-COINSIZE * Board.WIDTH / 2 + x * COINSIZE, -COINSIZE * Board.HEIGHT / 2 + y * COINSIZE, -COINDEPTH / 2, COINSIZE, COINDEPTH, color, QUALITY);
            }
            if (y < Board.HEIGHT) {
                Geometry.drawTube(-COINSIZE * Board.WIDTH / 2 + x * COINSIZE, -COINSIZE * Board.HEIGHT / 2 + y * COINSIZE, -COINDEPTH / 2, COINSIZE, COINSIZE * 0.8f, COINDEPTH + 0.6f, Color.BLUE.darker(), QUALITY);
            }
        }
    }

    /**
     * Draws the cursor
     * @param cursor cursor object
     * @param colour color
     */
    public static void drawCursor(Cursor cursor, Color colour) {
        Geometry.drawDisk(-COINSIZE * Board.WIDTH / 2 + cursor.getPosition() * COINSIZE, COINSIZE * Board.HEIGHT / 2, -COINDEPTH / 2, COINSIZE, COINDEPTH, colour, QUALITY);
    }

    /**
     * Draws the hint
     * @param hint board column
     */
    public static void drawHint(int hint) {
        Geometry.drawDisk(-COINSIZE * Board.WIDTH / 2 + hint * COINSIZE, COINSIZE * Board.HEIGHT / 2, -COINDEPTH, COINSIZE, COINDEPTH, Color.GREEN, QUALITY);
    }
}