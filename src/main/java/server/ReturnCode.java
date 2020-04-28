package server;

/**
 * Returncodes used internally
 * @author cotix
 *
 */
public enum ReturnCode {
    SUCCES(0),
    SUCCES_WARNING(1), SUCCES_NOT_FOUND(2), SUCCES_NO_INVITE(3), HAS_WON(4),
    UNKNOWN_FAIL(-1), NAME_TAKEN(-2), NAME_ALREADY_SET(-3), ALREADY_EXISTS(-4), ALREADY_PLAYING(-5),
    NOT_FOUND(-6), NAME_INVALID(-7), INVALID_MOVE(-8), WRONG_PLAYER(-9);

    private ReturnCode(int v) {
    }
}