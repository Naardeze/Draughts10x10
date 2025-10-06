package draughts10x10;

import static draughts10x10.SquareBoard.GRID;
import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PositionBoard.EMPTY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * MinMax
 * 
 * Basic minimax algoritme with alfa beta pruning.
 * AI move
 * 
 * enum Diagonal -> move in 4 directions (bitboards)
 * 
 * Special Thanx to Logic Crazy Chess
 * 
 * @author Naardeze
 */

enum MinMax {
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
    
    //constants
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
        
    //0<x<9 & 0<y<9
    private static long middle = 0l;
    
    protected abstract int toAlfaBeta(int alfaBeta, int value);
    protected abstract int valueOf(int value);
    
    //Like Game.turn (don't invent the wheel twice)
    //1 moves, maxCapture
    //2 pruning
    private int valueOf(int color, char[] position, long turn, long opponent, HashMap<String, Integer>[] lookUp, int value, MinMax minMax, int[] alfaBeta, int depth) {
        //1 (moves, maxCapture)
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
    
        for (long empty = ~(turn ^ opponent), pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int index = Long.numberOfTrailingZeros(pieces);
            boolean isKing = position[index] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;

            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_LEFT_RIGHT, Diagonal.PLUS_RIGHT_LEFT}, {Diagonal.MIN_RIGHT_LEFT, Diagonal.PLUS_LEFT_RIGHT}}) {//-+
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
                                
                                //check for extra captures
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
                        
                        //empty
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

            value += valueOf(maxCapture);
            
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

                        if (!lookUp[color].containsKey(key)) {
                            lookUp[color].put(key, minMax.valueOf(1 - color, key.toCharArray(), opponent ^ capture, turn ^ (1l << from ^ 1l << to), lookUp, value, this, alfaBeta.clone(), depth));
                        }

                        // alfaBeta
                        alfaBeta[ordinal()] = toAlfaBeta(alfaBeta[ordinal()], lookUp[color].get(key));

                        //prune
                        if (alfaBeta[MAX.ordinal()] >= alfaBeta[MIN.ordinal()]) {
                            break pruning;
                        }
                    }
                }

                position[from] = piece;
            }
        }        

        //<-value
        return alfaBeta[ordinal()];
    }
    
    private static char[] getPosition(int color, char[] position, char piece, ArrayList<Integer> captures, int to) {
        //promotion
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        //move
        position[to] = piece;
        
        //capture
        captures.forEach(capture -> position[capture] = EMPTY);
        
        //<-key
        return position;
    }
    
    static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, ArrayList<Integer>[]> moves, int maxCapture) {
        int player = 1 - ai;//opponent
        
        long turn = 0l;//pieces[ai]
        long opponent = 0l;//pieces[player]

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
                
                int min = MIN.valueOf(player, getPosition(ai, position.clone(), piece, move, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new HashMap[] {new HashMap(), new HashMap()}, maxCapture, MAX, new int[] {ALFA, BETA}, AI.getValue());
                
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
    
    private enum Diagonal {
        MIN_LEFT_RIGHT(COLUMN, 0, -COLUMN) {//--
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = LEFT_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_RIGHT_LEFT(COLUMN - 1, 0, -COLUMN + 1) {//+-
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = RIGHT_LEFT[index % COLUMN + index / GRID];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_RIGHT_LEFT(COLUMN, ROW, COLUMN) {//-+
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = RIGHT_LEFT[index % COLUMN + index / GRID];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        }, 
        PLUS_LEFT_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = LEFT_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

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
        final private static long[] LEFT_RIGHT = new long[GRID];//-+
        final private static long[] RIGHT_LEFT = new long[GRID - 1];//+-

        static {
            for (int i = 0; i < LEFT_RIGHT.length; i++) {
                LEFT_RIGHT[i] = 0l;

                //bit: 4-0, 5-45
                //j: 1-9, 9-1
                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; j++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    LEFT_RIGHT[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < RIGHT_LEFT.length; i++) {
                RIGHT_LEFT[i] = 0l;

                //bit: 0-4-44
                //j: 2-10-2
                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; j++, bit += COLUMN - bit / COLUMN % 2) {
                    RIGHT_LEFT[i] ^= 1l << bit;
                }
            }
        }
    }

    static {
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
