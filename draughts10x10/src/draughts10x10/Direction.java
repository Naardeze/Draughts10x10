package draughts10x10;

import static draughts10x10.board.SquareBoard.SIZE;
import static draughts10x10.board.SquareBoard.x;
import static draughts10x10.board.SquareBoard.y;

/*
    enum used for moves
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
    
    public boolean hasNext(int index) {
        int x = x(index) + this.x;
        int y = y(index) + this.y;
        
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
    
    public int getNext(int index) {
        return (x(index) + x) / 2 + (y(index) + y) * (SIZE / 2);
    }
    
    public static Direction getDirection(int from, int to) {
        if (x(from) > x(to)) {
            if (from > to) {
                return MIN_X_MIN_Y;
            } else {
                return MIN_X_PLUS_Y;
            }
        } else {
            if (from > to) {
                return PLUS_X_MIN_Y;
            } else {
                return PLUS_X_PLUS_Y;
            }
        }
    }
    
}
