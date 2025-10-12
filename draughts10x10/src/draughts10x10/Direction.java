package draughts10x10;

import static draughts10x10.board.SquareBoard.GRID;
import static draughts10x10.board.SquareBoard.x;
import static draughts10x10.board.SquareBoard.y;

/**
 * Direction
 * 
 * move in 4 directions (x, y)
 * 
 * @author Naardeze
 */

enum Direction {
    MIN_X_MIN_Y(-1, -1),
    PLUS_X_MIN_Y(1, -1),
    MIN_X_PLUS_Y(-1, 1),
    PLUS_X_PLUS_Y(1, 1);

    final protected  int x;
    final protected int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    boolean canStep(int index) {
        int x = x(index) + this.x;
        int y = y(index) + this.y;

        return x >= 0 && x < GRID && y >= 0 && y < GRID;
    }

    int getStep(int index) {
        return (x(index) + x) / 2 + (y(index) + y) * (GRID / 2);
    }

    static Direction getDirection(int from, int to) {//from -> to
        if (x(from) > x(to)) {//-x
            if (from > to) {//-y
                return MIN_X_MIN_Y;
            } else {//+y
                return MIN_X_PLUS_Y;
            }
        } else {//+x
            if (from > to) {//-y
                return PLUS_X_MIN_Y;
            } else {//+y
                return PLUS_X_PLUS_Y;
            }
        }
    }
}

