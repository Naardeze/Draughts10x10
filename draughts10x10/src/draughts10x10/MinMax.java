package draughts10x10;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PositionBoard.EMPTY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import static draughts10x10.SquareBoard.GRID;

/**
 * MinMax
 * 
 * basic minimax algoritm with alfa beta pruning
 * 
 * -valueOf -> move value
 * -getPosition (static) -> moved board
 * -getAIMove (static) -> best move
 * 
 * enum Node -> evaluation
 * enum Diagonal -> move in 4 directions (bitboards)
 * 
 * @author Naardeze
 */

final class MinMax extends HashMap<String, Integer> {//<position, move value>    
    //static constants
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
        
    //4 neighbour squares (x>0 & x<9 & y>0 & y<9)
    private static long middle = 0l;
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //like Game.turn (don't invent the wheel twice)
    //1: moves, maxCapture
    //2: alfa beta pruning
    private int valueOf(char[] position, long turn, long opponent, MinMax minMax, int[] alfaBeta, int depth) {
        //1 (moves, maxCapture)
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        //pieces[color]
        for (long empty = ~(turn ^ opponent), captureMiddle = opponent & middle, pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int index = Long.numberOfTrailingZeros(pieces);
            boolean isKing = position[index] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;
            
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_DIAGONAL}}) {//-+
                for (Diagonal vertical : horizontal) {//-+
                    if (vertical.hasNext(index)) {
                        long move = vertical.getNext(index);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(index, ~empty, move);
                        }
                        
                        long capture = move & captureMiddle;

                        if (capture != 0l) {
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            if ((next & empty) == next) {
                                if (isKing && (next & middle) == next) {
                                    next = vertical.getLine(index, ~empty, next) & empty;
                                }
                                
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ next}));//<capture, <empty>>
                                
                                empty ^= 1l << index;
                                
                                do {
                                    move = captureMoves.remove(0);
                                 
                                    long captures = move & opponent;
                                    
                                    if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();
                                            
                                            maxCapturePiece++;
                                        }
                                        
                                        movesPiece.add(move);
                                    }
                                    
                                    if (captures != captureMiddle) {//not all captured
                                        for (long steps = move & empty; steps != 0l; steps ^= Long.lowestOneBit(steps)) {//empty square(s)
                                            int step = Long.numberOfTrailingZeros(steps);
                                            
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                if (diagonal.hasNext(step)) {
                                                    next = diagonal.getNext(step);
                                                    
                                                    if (isKing && (next & middle & empty) == next) {
                                                        next = diagonal.getLine(step, ~empty, next);
                                                    }

                                                    if ((next & move) == 0l) {//no dubbles
                                                        capture = next & captureMiddle;
                                                        
                                                        if (capture != 0l) {
                                                            next = diagonal.getNext(Long.numberOfTrailingZeros(capture));

                                                            if ((next & empty) == next) {
                                                                if (isKing && (next & middle) == next) {
                                                                    next = diagonal.getLine(step, ~empty, next) & empty;
                                                                }

                                                                captureMoves.add(captures ^ capture ^ next);//<<captures>, capture, <empty>>
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());
                                
                                empty ^= 1l << index;
                            }
                        }
                        
                        if (maxCapturePiece == 0 && (isKing || vertical == horizontal[color])) {
                            move &= empty;

                            if (move != 0l) {
                                movesPiece.add(move);
                            }
                        }
                    }
                }
            }
            
            //moveable
            if (!movesPiece.isEmpty()) {
                if (maxCapturePiece > maxCapture) {
                    moves.clear();
                    
                    maxCapture = maxCapturePiece;
                }
                
                moves.put(index, movesPiece);
            }
        }
        
        //2 (alfa beta pruning)
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//continue
            depth--;
        } else if (maxCapture == 0) {//depth=0 & maxCapture=0
            return node.valueOf(Long.bitCount(turn) - Long.bitCount(opponent));
        }

        pruning : for (int from : moves.keySet()) {
            char piece = position[from];

            position[from] = EMPTY;

            for (long move : moves.get(from)) {
                long capture = move & opponent;
                ArrayList<Integer> captures = new ArrayList();

                //capture -> captures
                for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                    captures.add(Long.numberOfTrailingZeros(copy));
                }
                
                //empty square(s)
                for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                    int to = Long.numberOfTrailingZeros(destinations);                    
                    String key = String.valueOf(getPosition(color, position.clone(), piece, captures, to));

                    //check if key (board) already occured
                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), opponent ^ capture, turn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth));
                    }
                    
                    //move value
                    int value = get(key);
                    
                    //alfabeta pruning
                    if (node.isAlfaBeta(alfaBeta[node.ordinal()], value)) {
                        alfaBeta[node.ordinal()] = value;

                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            break pruning;
                        }
                    }
                }
            }

            position[from] = piece;
        }
        
        //<-move value
        return alfaBeta[node.ordinal()];
    }

    //position & move -> key
    private static char[] getPosition(int color, char[] position, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        position[to] = piece;
        
        captures.forEach(capture -> position[capture] = EMPTY);
        
        //<-key
        return position;
    }

    //AI move
    static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, Move[]> moves, int depth) {
        int player = 1 - ai;
        
        long turn = 0l;//ai
        long opponent = 0l;//player

        for (int index : pieces[ai]) {
            turn ^= 1l << index;
        }
        
        for (int index : pieces[player]) {
            opponent ^= 1l << index;
        }

        ArrayList<ArrayList<Integer>> bestMoves = new ArrayList();
        int max = BETA;

        for (int from : moves.keySet()) {
            char piece = position[from];
            
            position[from] = EMPTY;
            
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;//bitboard captures
                
                //captures -> capture
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                int to = move.getTo();
                int min = new MinMax(Node.MIN, player).valueOf(getPosition(ai, position.clone(), piece, captures, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, depth);
                
                if (min >= max) {
                    if (min > max) {
                        bestMoves.clear();
                        
                        max = min;
                    }

                    bestMoves.add(move.getMove(from));
                }
            }
            
            position[from] = piece;
        }
        
        //<-ai move
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }
    
    private static enum Diagonal {
        MIN_DIAGONAL(COLUMN, 0, -COLUMN) {//--
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_ANTI_DIAGONAL(COLUMN - 1, 0, -COLUMN + 1) {//+-
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = ANTI_DIAGONAL[index % COLUMN + index / GRID];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_ANTI_DIAGONAL(COLUMN, ROW, COLUMN) {//-+
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = ANTI_DIAGONAL[index % COLUMN + index / GRID];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        }, 
        PLUS_DIAGONAL(COLUMN - 1, ROW, COLUMN + 1) {//++
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        };

        final int column;
        final int row;
        final int step;

        Diagonal(int column, int row, int step) {
            this.column = column;
            this.row = row;
            this.step = step;
        }

        boolean hasNext(int index) {
            return index % GRID != column && index / COLUMN != row;
        }

        long getNext(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        abstract long getLine(int index, long occupied, long from);

        //mask
        final private static long[] DIAGONAL = new long[GRID];//-+
        final private static long[] ANTI_DIAGONAL = new long[GRID - 1];//+-

        static {
            //4-0, 5-45
            for (int i = 0; i < DIAGONAL.length; i++) {
                DIAGONAL[i] = 0l;

                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bit += COLUMN + 1 - bit / COLUMN % 2, j++) {
                    DIAGONAL[i] ^= 1l << bit;
                }
            }

            //0-4-44
            for (int i = 0; i < ANTI_DIAGONAL.length; i++) {
                ANTI_DIAGONAL[i] = 0l;

                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bit += COLUMN - bit / COLUMN % 2, j++) {
                    ANTI_DIAGONAL[i] ^= 1l << bit;
                }
            }
        }
    }

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
        abstract int valueOf(int value);//hasTurn-noTurn
    }
    
    static {
        //middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!4 & !5
                middle ^= 1l << i;
            }
        }
    }

}

