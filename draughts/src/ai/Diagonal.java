package ai;

import static board.SquareBoard.SIZE;
import static ai.MinMax.PROMOTION;
import static ai.MinMax.ROW;

/*
    enum like Direction with bitboards

    special thanx to LogicCrazy Chess
*/

enum Diagonal {
    MIN_DIAGONAL(ROW, 0, -ROW) {
        @Override
        public long getKingSteps(int index, long all, long from) {
            return DIAGONAL[index] & (all ^ Long.reverse(Long.reverse(DIAGONAL[index] & all) - Long.reverse(from)));
        }
    }, 
    MIN_ANTI_DIAGONAL(ROW - 1, 0, -ROW + 1) {
        @Override
        public long getKingSteps(int index, long all, long from) {
            return ANTI_DIAGONAL[index] & (all ^ Long.reverse(Long.reverse(ANTI_DIAGONAL[index] & all) - Long.reverse(from)));
        }
    }, 
    PLUS_ANTI_DIAGONAL(ROW, PROMOTION, ROW) {
        @Override
        public long getKingSteps(int index, long all, long from) {
            return ANTI_DIAGONAL[index] & (all ^ ((ANTI_DIAGONAL[index] & all) - from));
        }
    }, 
    PLUS_DIAGONAL(ROW - 1, PROMOTION, ROW + 1) {
        @Override
        public long getKingSteps(int index, long all, long from) {
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
    
    final public static long[] DIAGONAL = new long[SIZE * SIZE / 2];
    final public static long[] ANTI_DIAGONAL = new long[SIZE * SIZE / 2];
    
    public boolean hasNext(int index) {
        return index % SIZE != column && index / ROW != row;
    }

    public long getNext(int index) {
        return 1l << index + step - index / ROW % 2;
    }
    
    public abstract long getKingSteps(int index, long all, long from);
    
    static {
        //diagonal
        for (int i = 0; i < SIZE; i++) {
            long mask = 0l;
            
            for (int bit = ROW - 1 - Math.min(i, ROW - 1) + i / ROW * ROW + Math.max(0, i - ROW) * SIZE, j = 0; j < 1 + (Math.min(i, ROW - 1) - Math.max(0, i - ROW)) * 2; bit += ROW + 1 - bit / ROW % 2, j++) {
                mask ^= 1l << bit;
            }
            
            for (long bitBoard = mask; bitBoard != 0l; bitBoard ^= Long.lowestOneBit(bitBoard)) {
                DIAGONAL[Long.numberOfTrailingZeros(bitBoard)] = mask;
            }
        }

        //anti diagonal
        for (int i = 0; i < SIZE - 1; i++) {
            long mask = 0l;
            
            for (int bit = Math.min(i, ROW - 1) + Math.max(0, i - (ROW - 1)) * SIZE, j = 0; j < 2 + (Math.min(i, ROW - 1) - Math.max(0, i - (ROW - 1))) * 2; bit += ROW - bit / ROW % 2, j++) {
                mask ^= 1l << bit;
            }
            
            for (long bitBoard = mask; bitBoard != 0l; bitBoard ^= Long.lowestOneBit(bitBoard)) {
                ANTI_DIAGONAL[Long.numberOfTrailingZeros(bitBoard)] = mask;
            }
        }
    }

}
