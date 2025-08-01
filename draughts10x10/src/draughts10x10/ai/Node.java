package draughts10x10.ai;

enum Node {
    MAX {
        @Override
        boolean isAlfaBeta(int alfaBeta, int value) {//beta
            return value > alfaBeta;
        }
        @Override
        int valueOf(int value) {//ai-opponent
            return value;
        }
    },
    MIN {
        @Override
        boolean isAlfaBeta(int alfaBeta, int value) {//alfa
            return value < alfaBeta;
        }
        @Override
        int valueOf(int value) {//opponent-ai
            return -value;
        }
    };

    abstract boolean isAlfaBeta(int alfaBeta, int value);
    abstract int valueOf(int value);

}
