package draughts10x10.ai;

import static draughts10x10.ai.MinMax.COLUMN;
import static draughts10x10.ai.MinMax.ROW;
import static draughts10x10.board.SquareBoard.SIZE;


/*
    enum like Direction used for movement.

    special thanx to LogicCrazy Chess
*/

enum Diagonal {
    MIN_DIAGONAL(COLUMN, 0, -COLUMN) {//50 -> 1
        @Override
        long getKingSteps(int index, long all, long from) {
            return DIAGONAL[index] & (all ^ Long.reverse(Long.reverse(DIAGONAL[index] & all) - Long.reverse(from)));
        }
    }, 
    MIN_ANTI_DIAGONAL(COLUMN - 1, 0, -COLUMN + 1) {//45 -> 5
        @Override
        long getKingSteps(int index, long all, long from) {
            return ANTI_DIAGONAL[index] & (all ^ Long.reverse(Long.reverse(ANTI_DIAGONAL[index] & all) - Long.reverse(from)));
        }
    }, 
    PLUS_ANTI_DIAGONAL(COLUMN, ROW, COLUMN) {//45 -> 5
        @Override
        long getKingSteps(int index, long all, long from) {
            return ANTI_DIAGONAL[index] & (all ^ ((ANTI_DIAGONAL[index] & all) - from));
        }
    }, 
    PLUS_DIAGONAL(COLUMN - 1, ROW, COLUMN + 1) {//1 -> 50
        @Override
        long getKingSteps(int index, long all, long from) {
            return DIAGONAL[index] & (all ^ ((DIAGONAL[index] & all) - from));
        }
    };
    
    final private int column;
    final private int row;
    
    final private int step;
    
    Diagonal(int column, int row, int step) {
        this.column = column;
        this.row = row;
        
        this.step = step;
    }
    
    final public static long[] DIAGONAL = new long[SIZE * SIZE / 2];//1 <-> 50
    final public static long[] ANTI_DIAGONAL = new long[SIZE * SIZE / 2];//5 <-> 45
    
    boolean hasNext(int index) {
        return index % SIZE != column && index / COLUMN != row;
    }

    long getNext(int index) {
        return 1l << index + step - index / COLUMN % 2;
    }
    
    abstract long getKingSteps(int index, long all, long from);
    
    static {
        //diagonal (1<->50)
        for (int i = 0; i < SIZE; i++) {
            long mask = 0l;
            
            for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * SIZE, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bit += COLUMN + 1 - bit / COLUMN % 2, j++) {
                mask ^= 1l << bit;
            }
            
            for (long bitBoard = mask; bitBoard != 0l; bitBoard ^= Long.lowestOneBit(bitBoard)) {
                DIAGONAL[Long.numberOfTrailingZeros(bitBoard)] = mask;
            }
        }

        //anti diagonal (5<->45)
        for (int i = 0; i < SIZE - 1; i++) {
            long mask = 0l;
            
            for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * SIZE, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bit += COLUMN - bit / COLUMN % 2, j++) {
                mask ^= 1l << bit;
            }
            
            for (long bitBoard = mask; bitBoard != 0l; bitBoard ^= Long.lowestOneBit(bitBoard)) {
                ANTI_DIAGONAL[Long.numberOfTrailingZeros(bitBoard)] = mask;
            }
        }
    }

}
