package draughts10x10;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PieceBoard.EMPTY;
import static draughts10x10.SquareBoard.SIZE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * minimax with alfa beta pruning
 * 
 * depth = 0 -> captureMax = 0 -> return value
 *              captureMax != 0 -> extra search
 * 
 * enum Node -> evaluation (alfa beta)
 * enum Diagonal -> 4 directions (bitboards)
 * 
 * valueOf -> alfa beta
 * getBoard -> moved board
 * getAIMove -> best move
 * 
 * @author Naardeze
 */

final class MinMax extends HashMap<String, Integer> {//<board, value>    
    //constants
    final private static int COLUMN = SIZE / 2;//5
    final private static int ROW = SIZE - 1;//9
    
    //alfa beta
    final private static int ALFA = Integer.MAX_VALUE;
    final private static int BETA = Integer.MIN_VALUE;
    
    //horizontal x vertical
    final private static Diagonal[][] HORIZONTAL = {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_DIAGONAL}};

    //middle squares -> x>0 & x<9 & y>0 & y<9 (can move in all 4 directions)
    private static long middle = 0l;
    
    //alfa beta evaluation
    private static enum Node {
        MIN {//beta
            @Override
            boolean isAlfaBeta(int alfaBeta, int value) {
                return value < alfaBeta;
            }
            @Override
            int valueOf(int value) {
                return -value;
            }
        },
        MAX {//alfa
            @Override
            boolean isAlfaBeta(int alfaBeta, int value) {
                return value > alfaBeta;
            }
            @Override
            int valueOf(int value) {
                return value;
            }
        };

        abstract boolean isAlfaBeta(int alfaBeta, int value);//>alfa, <beta
        abstract int valueOf(int value);//isTurn-noTurn
    }
    
    //4 move directions
    //hasNext -> can move?
    //getNext -> pawn step
    //getLine -> king line
    private static enum Diagonal {
        MIN_DIAGONAL(COLUMN, 0, -COLUMN) {//-x-y
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / SIZE];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_ANTI_DIAGONAL(COLUMN - 1, 0, -COLUMN + 1) {//+x-y
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = ANTI_DIAGONAL[index % COLUMN + index / SIZE];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_ANTI_DIAGONAL(COLUMN, ROW, COLUMN) {//-x+y
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = ANTI_DIAGONAL[index % COLUMN + index / SIZE];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        }, 
        PLUS_DIAGONAL(COLUMN - 1, ROW, COLUMN + 1) {//+x+y
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / SIZE];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        };

        //hasNext
        final int column;
        final int row;
        //getNext
        final int step;

        Diagonal(int column, int row, int step) {
            this.column = column;
            this.row = row;
            
            this.step = step;
        }

        //can move?
        boolean hasNext(int index) {
            return index % SIZE != column && index / COLUMN != row;
        }

        //pawn step
        long getNext(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        //king line
        abstract long getLine(int index, long occupied, long from);

        //mask
        final private static long[] DIAGONAL = new long[SIZE];//-+
        final private static long[] ANTI_DIAGONAL = new long[SIZE - 1];//+-

        static {
            //diagonal: 4-0, 5-45
            for (int i = 0; i < DIAGONAL.length; i++) {
                DIAGONAL[i] = 0l;

                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * SIZE, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bit += COLUMN + 1 - bit / COLUMN % 2, j++) {
                    DIAGONAL[i] ^= 1l << bit;
                }
            }

            //anti diagonal: 0-4-44
            for (int i = 0; i < ANTI_DIAGONAL.length; i++) {
                ANTI_DIAGONAL[i] = 0l;

                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * SIZE, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bit += COLUMN - bit / COLUMN % 2, j++) {
                    ANTI_DIAGONAL[i] ^= 1l << bit;
                }
            }
        }
    }
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //like Game.turn (don't invent the wheel twice)
    //1: get state (moves and maxCapture)
    //2: evaluation
    //3: pruning
    private int valueOf(char[] board, long hasTurn, long noTurn, MinMax minMax, int[] alfaBeta, int depth) {
        //1 get state (moves and maxCapture)
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        //occupied -> white & black
        //empty -> !occupied
        //captureMiddle -> noTurn pieces on board middle, can be captured
        //turn = hasTurn
        //loop pieces (turn)
        for (long occupied = hasTurn ^ noTurn, empty = ~occupied, captureMiddle = noTurn & middle, turn = hasTurn; turn != 0l; turn ^= Long.lowestOneBit(turn)) {
            int index = Long.numberOfTrailingZeros(turn);
            boolean isKing = board[index] == KING[color];
            HashSet<Long> pieceMoves = new HashSet();
            
            //horizontal x vertical
            for (Diagonal[] horizontal : HORIZONTAL) {//-+
                for (Diagonal vertical : horizontal) {//-+
                    //can move
                    if (vertical.hasNext(index)) {
                        //pawn step
                        long move = vertical.getNext(index);

                        //king line
                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(index, occupied, move);
                        }
                        
                        //can capture
                        if ((move & captureMiddle) != 0l) {
                            //capture
                            long capture = move & captureMiddle;
                            //pawn step
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            //empty
                            if ((next & empty) == next) {
                                //empty king line
                                if (isKing && (next & middle) == next) {
                                    next = vertical.getLine(index, occupied, next) & empty;
                                }
                                
                                //capturemoves to check for extra captures
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ next}));
                                
                                //piece off board
                                occupied ^= 1l << index;
                                empty = ~occupied;
                                
                                //check for extra captures
                                do {
                                    //capture move to check
                                    move = captureMoves.remove(0);
                                 
                                    //is allready captured
                                    long captures = move & captureMiddle;
                                    
                                    //adjust maxCapture
                                    if (Long.bitCount(captures) >= maxCapture) {
                                        if (Long.bitCount(captures) > maxCapture) {
                                            pieceMoves.clear();
                                            moves.clear();
                                            
                                            maxCapture++;
                                        }
                                        
                                        //legal move
                                        pieceMoves.add(move);
                                    }
                                    
                                    //not all captured
                                    if (captures != captureMiddle) {
                                        //loop empty squares
                                        for (long destinations = move ^ captures; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                                            //empty square
                                            int to = Long.numberOfTrailingZeros(destinations);
                                            
                                            //loop 4 directions
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                //can move
                                                if (diagonal.hasNext(to)) {
                                                    //pawn step
                                                    next = diagonal.getNext(to);
                                                    
                                                    //king line
                                                    if (isKing && (next & middle & empty) == next) {
                                                        next = diagonal.getLine(to, occupied, next);
                                                    }

                                                    //extra capture
                                                    if ((next & move) == 0l && (next & captureMiddle) != 0l) {
                                                        //capture
                                                        capture = next & captureMiddle;
                                                        //pawn step
                                                        next = diagonal.getNext(Long.numberOfTrailingZeros(capture));

                                                        //empty
                                                        if ((next & empty) == next) {
                                                            //empty king line
                                                            if (isKing && (next & middle) == next) {
                                                                next = diagonal.getLine(to, occupied, next) & empty;
                                                            }

                                                            //capture -> captureMoves
                                                            captureMoves.add(captures ^ capture ^ next);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());
                                
                                //piece on board
                                occupied ^= 1l << index;
                                empty = ~occupied;
                            }
                        }
                        
                        //empty squares, no capture
                        if (maxCapture == 0 && (isKing || vertical == horizontal[color])) {
                            move &= empty;
                            
                            //legal move
                            if (move != 0l) {
                                pieceMoves.add(move);
                            }
                        }
                    }
                }
            }
            
            //moveable piece
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves);
            }
        }
        
        //2 evaluation
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//continue
            depth--;
        } else if (maxCapture == 0) {//depth=0 -> return value (move result)
            return node.valueOf(Long.bitCount(hasTurn) - Long.bitCount(noTurn));
        }

        //3 pruning
        //loop pieces
        pruning : for (int from : moves.keySet()) {
            char piece = board[from];

            //piece off board
            board[from] = EMPTY;

            //loop moves
            for (long move : moves.get(from)) {
                long capture = move & noTurn;
                ArrayList<Integer> captures = new ArrayList();

                //capture -> captures
                for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                    captures.add(Long.numberOfTrailingZeros(copy));
                }
                
                //loop empty squares
                for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                    int to = Long.numberOfTrailingZeros(destinations);                    
                    String key = String.valueOf(getBoard(color, board.clone(), piece, captures, to));

                    //check if key (board) already occured
                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth));
                    }
                    
                    //move result
                    int value = get(key);
                    
                    //alfabeta
                    if (node.isAlfaBeta(alfaBeta[node.ordinal()], value)) {
                        alfaBeta[node.ordinal()] = value;

                        //prune
                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            break pruning;
                        }
                    }
                }
            }

            //piece on board
            board[from] = piece;
        }
        
        return alfaBeta[node.ordinal()];
    }

    //board -> move, promotion, captures
    private static char[] getBoard(int color, char[] board, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {//promotion
            board[to] = KING[color];
        } else {//move
            board[to] = piece;
        }
        
        //loop captures
        captures.forEach(capture -> board[capture] = EMPTY);
        
        return board;
    }

    //AI move
    static ArrayList<Integer> getAIMove(int ai, char[] board, HashSet<Integer>[] position, HashMap<Integer, Move[]> moves, int depth) {
        //opponent
        int player = 1 - ai;
        
        //position bitboards
        long hasTurn = 0l;//ai
        long noTurn = 0l;//player

        //position[ai] -> hasTurn
        for (int index : position[ai]) {
            hasTurn ^= 1l << index;
        }
        
        //position[player] -> noTurn
        for (int index : position[player]) {
            noTurn ^= 1l << index;
        }

        //best alfa moves
        ArrayList<ArrayList<Integer>> bestMoves = new ArrayList();
        //alfa
        int max = BETA;

        //loop pieces
        for (int from : moves.keySet()) {
            char piece = board[from];
            
            //piece off board
            board[from] = EMPTY;
            
            //loop moves
            for (Move move : moves.get(from)) {
                //capture(s)
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;//bitboard
                
                //captures -> capture
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                //destination
                int to = move.getTo();
                //beta
                int min = new MinMax(Node.MIN, player).valueOf(getBoard(ai, board.clone(), piece, captures, to), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, depth);
                
                //alfa move
                if (min >= max) {
                    //alfa=beta
                    if (min > max) {
                        bestMoves.clear();
                        
                        max = min;
                    }
                    
                    //boardmove
                    bestMoves.add(move.getBoardMove(from));
                }
            }
            
            //piece on board
            board[from] = piece;
        }
        
        //<- best alfa move (random)
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }
    
    static {
        //middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % SIZE != COLUMN - 1 && i % SIZE != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}

