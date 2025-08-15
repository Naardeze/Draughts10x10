package draughts10x10;

import static draughts10x10.board.SquareBoard.SIZE;
import static draughts10x10.board.SquareBoard.x;
import static draughts10x10.board.SquareBoard.y;

/**
 * move in 4 directions
 * 
 * @author Naardeze
 */

enum Direction {
    MIN_X_MIN_Y(-1, -1),
    PLUS_X_MIN_Y(1, -1),
    MIN_X_PLUS_Y(-1, 1),
    PLUS_X_PLUS_Y(1, 1);
    
    final public int x;
    final public int y;
    
    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }
    boolean hasNext(int index) {
        int x = x(index) + this.x;
        int y = y(index) + this.y;
        
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
    
    int getNext(int index) {
        return (x(index) + x) / 2 + (y(index) + y) * (SIZE / 2);
    }
    
    static Direction getDirection(int index1, int index2) {
        if (x(index1) > x(index2)) {
            if (index1 > index2) {
                return MIN_X_MIN_Y;
            } else {
                return MIN_X_PLUS_Y;
            }
        } else {
            if (index1 > index2) {
                return PLUS_X_MIN_Y;
            } else {
                return PLUS_X_PLUS_Y;
            }
        }
    }

}
