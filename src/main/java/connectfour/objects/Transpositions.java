package connectfour.objects;

import connectfour.util.Logging;

/**
 *
 */
public class Transpositions {
    /* Transposition table structure:
	 * Elke index heeft twee buckets. Eentje met het meeste werk, en een willekeurige
	 * proof(de deler): 21 bits
	 * score: 2 bits
	 * work: 6 bits 
	 * flag: 2 bits
	 * total: 31 bits = 1 signed int
	 * |*PPPPPPPPPPPPPPPPPPPPPWWWWWWFFSS|*PPPPPPPPPPPPPPPPPPPPPWWWWWWFFSS|
	 * Twee buckets, dus 64 bits(long) per index.
	 */

    //Config:
    // transSize must be bigger than 268435459!
    // For performance i found 700000001 to be a nice number
    //268435459 = 2 GB A very nice default.
    private static final int transSize = 268435459;
    // public static final int transSize = 711111143;

    //Defines:
    private static final long PROOFMASK = 0x7FFFFC00;
    private static final long SCOREMASK = 0x3;
    private static final long WORKMASK = 0x3F0;
    private static final long FLAGMASK = 0xC;
    private static final int PROOFSHIFT = 10;
    private static final int SCORESHIFT = 0;
    private static final int WORKSHIFT = 4;
    private static final int FLAGSHIFT = 2;

    private static final long FIRSTBUCKETMASK = 0xFFFFFFFFL;

    public static final int TT_EMPTY = 0;
    public static final int TT_EXACT = 3;
    public static final int TT_LOWERBOUND = 1;
    public static final int TT_UPPERBOUND = 2;

    private static int hashCounter = 0;
    private static final long[] hashTable = new long[transSize];

    public static int index(long hash) {
        return (int) (hash % transSize);
    }


    public static int proof(long hash) {
        return (int) (hash / transSize);
    }

    public static int getProof(int index, int bucket) {
        return (int) ((hashTable[index] >>> (32 * bucket)) & PROOFMASK) >>> PROOFSHIFT;
    }

    public static int getScore(int index, int bucket) {
        return (((int) ((hashTable[index] >>> (32 * bucket)) & SCOREMASK) >>> SCORESHIFT) - 1) * Board.INF; // not going to change this checkstyle thing, might want to edit it later
    }

    private static int getWork(int index, int bucket) {
        return (int) ((hashTable[index] >>> (32 * bucket)) & WORKMASK) >>> WORKSHIFT;
    }

    public static int getFlag(int index, int bucket) {
        return (int) ((hashTable[index] >>> (32 * bucket)) & FLAGMASK) >>> FLAGSHIFT;
    }

    public static void resetTrans() {
        for (int i = 0; i != transSize; ++i) {
            hashTable[i] = 0L;
        }
        Logging.log("Transposition table cleared");
    }

    private static void saveEntry(int index, int bucket, int proof, int score, int work, int flag) {
        if (score >= Board.INF - 100) {
            score = Board.INF;
        } else if (score <= -Board.INF + 100) {
            score = -Board.INF;
        }
        score += Board.INF;
        score /= Board.INF;
        if (getFlag(index, bucket) == TT_EMPTY) {
            hashCounter++;
        }
        long diff = bucket == 0 ? hashTable[index] & (FIRSTBUCKETMASK << 32) : hashTable[index] & FIRSTBUCKETMASK;

        hashTable[index] = diff | (((long) (
                (proof << PROOFSHIFT) |
                        (score << SCORESHIFT) | // not going to change this, we might want to edit this later.
                        (work << WORKSHIFT) |
                        (flag << FLAGSHIFT))) << (32 * bucket));
    }


    public static void saveTransposition(long hash, int _score, int _ply, int _flag, long _work) {
        int index = index(hash);
        int proof = proof(hash);
        int work = 0;
        for (; _work > 0; _work >>= 1) work++;
        if (work >= getWork(index, 0) || proof == getProof(index, 0)) {
            saveEntry(index, 0, proof, _score, work, _flag);
        } else {
            saveEntry(index, 1, proof, _score, work, _flag);
        }
    }
}
