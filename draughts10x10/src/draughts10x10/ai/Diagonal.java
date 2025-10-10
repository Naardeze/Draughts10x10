package draughts10x10.ai;

import static draughts10x10.ai.MinMax.COLUMN;
import static draughts10x10.ai.MinMax.ROW;
import static draughts10x10.board.SquareBoard.GRID;

/**
 * Diagonal
 * 
 * move in 4 directions (bitboards)
 * orientation: square[0] = MIN LEFT
 * 
 * Special Thanx to Logic Crazy Chess
 * 
 * @author Naardeze
 */

enum Diagonal {
    MIN_LEFT(COLUMN, 0, -COLUMN) {//-- -> square[0]
        @Override
        long getLine(int index, long occupied, long from) {
            long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

            return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
        }
    }, 
    MIN_RIGHT(COLUMN - 1, 0, -COLUMN + 1) {//+- -> square[4]
        @Override
        long getLine(int index, long occupied, long from) {
            long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

            return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
        }
    }, 
    PLUS_LEFT(COLUMN, ROW, COLUMN) {//-+ -> square[44]
        @Override
        long getLine(int index, long occupied, long from) {
            long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

            return diagonal & (occupied ^ ((diagonal & occupied) - from));
        }
    }, 
    PLUS_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++ -> square[49]
        @Override
        long getLine(int index, long occupied, long from) {
            long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

            return diagonal & (occupied ^ ((diagonal & occupied) - from));
        }
    };

    //canStep
    final private int column;
    final private int row;
    //getStep
    final private int step;

    Diagonal(int column, int row, int step) {
        this.column = column;
        this.row = row;

        this.step = step;
    }

    //has neightbour
    boolean canStep(int index) {
        return index % GRID != column && index / COLUMN != row;
    }

    //man step
    long getStep(int index) {
        return 1l << index + step - index / COLUMN % 2;
    }

    //king steps
    abstract long getLine(int index, long occupied, long from);

    //diagonals
    final private static long[] LEFT_TO_RIGHT = new long[GRID];//->
    final private static long[] RIGHT_TO_LEFT = new long[GRID - 1];//<-

    static {
        for (int i = 0; i < LEFT_TO_RIGHT.length; i++) {
            LEFT_TO_RIGHT[i] = 0l;

            //bit: 4-0, 5-45
            //bitCount: 1-9, 9-1
            for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, bitCount = 0; bitCount < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bitCount++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                LEFT_TO_RIGHT[i] ^= 1l << bit;
            }
        }

        for (int i = 0; i < RIGHT_TO_LEFT.length; i++) {
            RIGHT_TO_LEFT[i] = 0l;

            //bit: 0-4-44
            //bitCount: 2-10-2
            for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, bitCount = 0; bitCount < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bitCount++, bit += COLUMN - bit / COLUMN % 2) {
                RIGHT_TO_LEFT[i] ^= 1l << bit;
            }
        }
    }
}
