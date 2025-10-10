package draughts10x10.ai;

/**
 * Node
 * 
 * evaluation alfa beta
 * 
 * @author Naardeze
 */

enum Node {
    MIN {//beta (player)
        @Override
        int toAlfaBeta(int alfaBeta, int value) {
            return Math.min(alfaBeta, value);
        }

        @Override
        int valueOf(int value) {
            return -value;
        }
    },
    MAX {//alfa (ai)
        @Override
        int toAlfaBeta(int alfaBeta, int value) {
            return Math.max(alfaBeta, value);
        }

        @Override
        int valueOf(int value) {
            return value;
        }
    };

    abstract int toAlfaBeta(int alfaBeta, int value);
    abstract int valueOf(int value);
}
