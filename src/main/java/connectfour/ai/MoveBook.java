package connectfour.ai;

import connectfour.util.Logging;
import connectfour.objects.Board;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * MoveBook that tells the AI which moves to make, described in a given file.
 * We made multiple MoveBooks, but we use the 14 ply book at default.
 */
public class MoveBook {

    private byte book[];

    /**
     * Loads a MoveBook from the specified file.
     * @param filename movebook
     */
    public MoveBook(String filename) {
        try {
            URL location = getClass().getClassLoader().getResource(filename);
            File file;

            if (location == null) {
                throw new FileNotFoundException();
            }

            file = new File(location.getFile());
            book = new byte[(int) file.length() + 2];

            FileInputStream in;
            in = new FileInputStream(file);
            in.read(book);
            in.close();
        } catch (Exception e) {
            book = new byte[1];
            Logging.logError("Cannot load MoveBook" + filename);
        }
    }

    /**
     * Returns the index in the MoveBook of the current Board, if available
     * @param moves String of column numbers
     * @return index or -1
     */
    public int getIndex(String moves) {
        int res;
        String index;
        index = "1" + moves.replaceAll(".(.)", "$1");
        try {
            res = Integer.parseInt(index, Board.WIDTH);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
        return res;
    }

    /**
     * Returns which move at the specified index, if found
     * @param index index in the MoveBook
     * @return the column
     */
    public int getMove(int index) {
        if (index / 2 >= book.length) {
            return -1;
        }
        byte pos = book[index / 2];
        if (index % 2 == 0) {
            pos >>>= 4;
        } else {
            pos &= 0xF;
        }
        if (pos == 0) {
            return -1;
        }
        return pos - 1;
    }

}