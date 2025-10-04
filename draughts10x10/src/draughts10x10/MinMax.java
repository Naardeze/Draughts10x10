package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PositionBoard.EMPTY;
import static draughts10x10.SquareBoard.GRID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * MinMax
 * 
 * basic minimax algoritm with alfa beta pruning, keeps searching while position contains captures
 * extends HashMap -> position, alfaBeta -> no dubbel calculations
 * 
 * enum Node -> evaluation
 * enum Diagonal -> move in 4 directions (bit 0-49);
 *
 * -valueOf -> alfa beta pruning
 * -getPosition (static) -> position (key)
 * -getAIMove (static) -> best move
 * 
 * @author Naardeze
 */

final class MinMax extends HashMap<String, Integer> {//<position, move value>    
    //constants
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
        
    //0<x<9 & 0<y<9
    static long middle = 0l;

    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //like Game.turn (don't invent the wheel twice)
    //1: moves, maxCapture
    //2: pruning
    private int valueOf(char[] position, long turn, long opponent, MinMax minMax, int[] alfaBeta, int depth) {
        //1 (moves, maxCapture)
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
    
        for (long empty = ~(turn ^ opponent), pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int index = Long.numberOfTrailingZeros(pieces);
            boolean isKing = position[index] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;

            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_DIAGONAL}}) {//-+
                for (Diagonal vertical : horizontal) {//-+ [WB]
                    if (vertical.canStep(index)) {
                        long move = vertical.getStep(index);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(index, ~empty, move);
                        }
                        
                        long capture = move & opponent;
                        
                        if ((capture & middle) != 0l) {
                            long step = vertical.getStep(Long.numberOfTrailingZeros(capture));
                            
                            if ((step & empty) == step) {
                                if (isKing && (step & middle) == step) {
                                    step = vertical.getLine(index, ~empty, step) & empty;
                                }
                                
                                //captures to check
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ step}));
                                
                                empty ^= 1l << index;
                                
                                //extra captures
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
                                    
                                    for (long destinations = move & empty; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                                        int to = Long.numberOfTrailingZeros(destinations);

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
            //game over
            if (moves.isEmpty()) {
                break pruning;
            //continue
            } else if (depth > 0) {
                depth--;
            //return value
            } else if (maxCapture == 0) {
                return node.valueOf(Long.bitCount(turn) - Long.bitCount(opponent));
            }

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
                            put(key, minMax.valueOf(key.toCharArray(), opponent ^ capture, turn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth));
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

        //<-value
        return alfaBeta[node.ordinal()];
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

    //AI move
    static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, Move[]> moves) {
        //opponent
        int player = 1 - ai;
        
        //pieces
        long turn = 0l;//ai
        long opponent = 0l;//player

        //pieces[ai]->turn
        for (int index : pieces[ai]) {
            turn ^= 1l << index;
        }
        
        //pieces[player]->opponent
        for (int index : pieces[player]) {
            opponent ^= 1l << index;
        }

        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        int max = BETA;

        for (int from : moves.keySet()) {
            char piece = position[from];
            
            position[from] = EMPTY;
            
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;
                
                //captures->capture
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                int to = move.getTo();
                int min = new MinMax(Node.MIN, player).valueOf(getPosition(ai, position.clone(), piece, captures, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, AI.getValue());
                
                if (min >= max) {
                    if (min > max) {
                        alfaMoves.clear();
                        
                        max = min;
                    }
                    
                    alfaMoves.add(move.getPieceMove(from));
                }
            }
            
            position[from] = piece;
        }

        //<-ai move
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
    }

    private enum Node {
        MIN {//beta
            @Override
            int toAlfaBeta(int alfaBeta, int value) {
                return Math.min(alfaBeta, value);
            }

            @Override
            int valueOf(int value) {//player-ai
                return -value;
            }
        },
        MAX {//alfa
            @Override
            int toAlfaBeta(int alfaBeta, int value) {
                return Math.max(alfaBeta, value);
            }

            @Override
            int valueOf(int value) {//ai-player
                return value;
            }
        };

        abstract int toAlfaBeta(int alfaBeta, int value);
        abstract int valueOf(int value);
    }

    private enum Diagonal {
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
        final private static long[] DIAGONAL = new long[GRID];//-+
        final private static long[] ANTI_DIAGONAL = new long[GRID - 1];//+-

        static {
            //bit: index (=square)
            //j: bits p mask (=diagonal length)
            
            for (int i = 0; i < DIAGONAL.length; i++) {
                DIAGONAL[i] = 0l;

                //4-0, 5-45
                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; j++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    DIAGONAL[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < ANTI_DIAGONAL.length; i++) {
                ANTI_DIAGONAL[i] = 0l;

                //0-4-44
                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; j++, bit += COLUMN - bit / COLUMN % 2) {
                    ANTI_DIAGONAL[i] ^= 1l << bit;
                }
            }
        }
    }

    static {
        //middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
