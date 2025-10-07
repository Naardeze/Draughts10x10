package draughts10x10;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PositionBoard.EMPTY;
import static draughts10x10.SquareBoard.GRID;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * MinMax
 * 
 * Basic minimax algoritm with alfa beta pruning
 * 
 * enum Node -> evaluation
 * enum Diagonal -> move in 4 directions (bitboards)
 * 
 * @author Naardez
 */

class MinMax extends HashMap<String, Integer> {
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
        
    //0<x<9 & 0<y<9
    private static long middle = 0l;
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    private int valueOf(char[] position, long turn, long opponent, MinMax mm, int value, int[] alfaBeta, int depth) {
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
    
        for (long empty = ~(turn ^ opponent), pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int index = Long.numberOfTrailingZeros(pieces);
            boolean isKing = position[index] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;

            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_LEFT_TO_RIGHT, Diagonal.PLUS_RIGHT_TO_LEFT}, {Diagonal.MIN_RIGHT_TO_LEFT, Diagonal.PLUS_LEFT_TO_RIGHT}}) {//-+
                for (Diagonal vertical : horizontal) {//-+ [WB]
                    if (vertical.canStep(index)) {
                        long move = vertical.getStep(index);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(index, ~empty, move);
                        }
                        
                        //capture
                        long capture = move & opponent;
                        
                        if ((capture & middle) != 0l) {
                            long step = vertical.getStep(Long.numberOfTrailingZeros(capture));
                            
                            if ((step & empty) == step) {
                                if (isKing && (step & middle) == step) {
                                    step = vertical.getLine(index, ~empty, step) & empty;
                                }
                                
                                //captures to check
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ step}));//captureMove
                                
                                empty ^= 1l << index;
                                
                                //check captureMoves
                                do {
                                    move = captureMoves.remove(0);

                                    long captures = move & opponent;
                
                                    //legal move
                                    if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();

                                            maxCapturePiece++;
                                        }
                                        
                                        movesPiece.add(move);
                                    }

                                    //empty square(s)
                                    for (long destination = move & empty; destination != 0l; destination ^= Long.lowestOneBit(destination)) {
                                        int to = Long.numberOfTrailingZeros(destination);

                                        for (Diagonal diagonal : Diagonal.values()) {
                                            if (diagonal.canStep(to)) {
                                                step = diagonal.getStep(to);

                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getLine(to, ~empty, step);
                                                }

                                                //no dubbles (captures and empty)
                                                if ((step & move) == 0l) {
                                                    capture = step & opponent;

                                                    if ((capture & middle) != 0l) {
                                                        step = diagonal.getStep(Long.numberOfTrailingZeros(capture));

                                                        if ((step & empty) == step) {
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getLine(to, ~empty, step) & empty;
                                                            }

                                                            captureMoves.add(captures ^ capture ^ step);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all capture moves checked
                                
                                empty ^= 1l << index;
                            }
                        }
                        
                        //legal move (empty)
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
        
        //2 (pruning)
        pruning : {
            if (moves.isEmpty()) {//game over
                break pruning;
            } else if (depth > 0) {//pruning
                depth--;
            } else if (maxCapture == 0) {//<-value
                return value;
            }

            value += node.valueOf(maxCapture);
            
            for (int from : moves.keySet()) {
                char piece = position[from];

                position[from] = EMPTY;

                for (long move : moves.get(from)) {
                    long capture = move & opponent;
                    ArrayList<Integer> captures = new ArrayList();

                    //capture->captures
                    for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                        captures.add(Long.numberOfTrailingZeros(copy));
                    }

                    //empty square(s)
                    for (long destination = move ^ capture; destination != 0l; destination ^= Long.lowestOneBit(destination)) {
                        int to = Long.numberOfTrailingZeros(destination);                    
                        String key = String.valueOf(getPosition(color, position.clone(), piece, captures, to));

                        if (!containsKey(key)) {
                            put(key, mm.valueOf(key.toCharArray(), opponent ^ capture, turn ^ (1l << from ^ 1l << to), this, value, alfaBeta.clone(), depth));
                        }

                        // alfaBeta
                        alfaBeta[node.ordinal()] = node.toAlfaBeta(alfaBeta[node.ordinal()], get(key));

                        //prune
                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            break pruning;
                        }
                    }
                }

                position[from] = piece;
            }
        }        

        return alfaBeta[node.ordinal()];
    }
    
     //position->key
    private static char[] getPosition(int color, char[] position, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        position[to] = piece;
        
        captures.forEach(index -> position[index] = EMPTY);
        
        //<-key
        return position;
    }
    
    static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, ArrayList<Integer>[]> moves, int maxCapture, int depth) {
        int player = 1 - ai;
        
        long turn = 0l;
        long opponent = 0l;

        for (int index : pieces[ai]) {
            turn ^= 1l << index;
        }
        
        for (int index : pieces[player]) {
            opponent ^= 1l << index;
        }

        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        int max = BETA;

        for (int from : moves.keySet()) {
            char piece = position[from];
            
            position[from] = EMPTY;
            
            for (ArrayList<Integer> move : moves.get(from)) {
                int to = move.remove(maxCapture);
                long capture = 0l;
                
                //captures->capture
                for (int index : move) {
                    capture ^= 1l << index;
                }
                
                int min = new MinMax(Node.MIN, player).valueOf(getPosition(ai, position.clone(), piece, move, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), maxCapture, new int[] {ALFA, BETA}, depth);
                
                //alfaMove
                if (min >= max) {
                    if (min > max) {
                        alfaMoves.clear();
                        
                        max = min;
                    }
                    
                    move.add(0, from);
                    move.add(to);
                    
                    alfaMoves.add(move);
                }
            }
            
            position[from] = piece;
        }

        //<-ai move
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
    }
    
    private enum Node {
        MIN {//beta (player)
            @Override
            protected int toAlfaBeta(int alfaBeta, int value) {
                return Math.min(alfaBeta, value);
            }

            @Override
            protected int valueOf(int value) {
                return -value;
            }
        },
        MAX {//alfa (ai)
            @Override
            protected int toAlfaBeta(int alfaBeta, int value) {
                return Math.max(alfaBeta, value);
            }

            @Override
            protected int valueOf(int value) {
                return value;
            }
        };
        
        abstract int toAlfaBeta(int alfaBeta, int value);
        abstract int valueOf(int value);
    }
    
     private enum Diagonal {
        MIN_LEFT_TO_RIGHT(COLUMN, 0, -COLUMN) {//--
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_RIGHT_TO_LEFT(COLUMN - 1, 0, -COLUMN + 1) {//+-
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_RIGHT_TO_LEFT(COLUMN, ROW, COLUMN) {//-+
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        }, 
        PLUS_LEFT_TO_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        };

        //canStep
        final int column;
        final int row;
        //getStep
        final int step;

        Diagonal(int column, int row, int step) {
            this.column = column;
            this.row = row;
            
            this.step = step;
        }

        boolean canStep(int index) {
            return index % GRID != column && index / COLUMN != row;
        }

        long getStep(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        //king steps
        abstract long getLine(int index, long occupied, long from);

        //mask
        final private static long[] LEFT_TO_RIGHT = new long[GRID];//-+
        final private static long[] RIGHT_TO_LEFT = new long[GRID - 1];//+-

        static {
            for (int i = 0; i < LEFT_TO_RIGHT.length; i++) {
                LEFT_TO_RIGHT[i] = 0l;

                //bit: 4-0, 5-45
                //j: 1-9, 9-1
                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; j++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    LEFT_TO_RIGHT[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < RIGHT_TO_LEFT.length; i++) {
                RIGHT_TO_LEFT[i] = 0l;

                //bit: 0-4-44
                //j: 2-10-2
                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; j++, bit += COLUMN - bit / COLUMN % 2) {
                    RIGHT_TO_LEFT[i] ^= 1l << bit;
                }
            }
        }
    }

    static {//middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
