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
 * extends HashMap -> position (key), value (alfa beta result) -> no dubbel calculations
 * 
 * enum Node -> alfa beta evaluation
 * enum Diagonal -> move in 4 directions (bit 0-49);
 *
 * -valueOf -> alfa beta pruning
 * -getPosition (static) -> position (key)
 * -getAIMove (static) -> best move (random)
 * 
 * @author Naardeze
 */

final class MinMax extends HashMap<String, Integer> {//<position, move value>    
    private static enum Node {
        MIN {//player
            @Override
            boolean isAlfaBeta(int alfaBeta, int value) {
                return value < alfaBeta;
            }

            @Override
            int valueOf(int value) {
                return -value;//player-ai
            }
        },
        MAX {//ai
            @Override
            boolean isAlfaBeta(int alfaBeta, int value) {
                return value > alfaBeta;
            }

            @Override
            int valueOf(int value) {
                return value;//ai-player
            }
        };

        abstract boolean isAlfaBeta(int alfaBeta, int value);//<beta, >alfa
        abstract int valueOf(int value);//turn-opponent
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
        final private static long[] DIAGONAL = new long[GRID];//->
        final private static long[] ANTI_DIAGONAL = new long[GRID - 1];//<-

        static {
            for (int i = 0; i < DIAGONAL.length; i++) {//4-0, 5-45
                DIAGONAL[i] = 0l;

                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bit += COLUMN + 1 - bit / COLUMN % 2, j++) {
                    DIAGONAL[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < ANTI_DIAGONAL.length; i++) {//0-4-44
                ANTI_DIAGONAL[i] = 0l;

                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, j = 0; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bit += COLUMN - bit / COLUMN % 2, j++) {
                    ANTI_DIAGONAL[i] ^= 1l << bit;
                }
            }
        }
    }

    //static constants
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
        
    //4 neighbours (x>0 & x<9 & y>0 & y<9)
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
        //legal moves
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        //captures position
        int maxCapture = 0;
        
        //pieces[color]
        for (long empty = ~(turn ^ opponent), pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            //from
            int index = Long.numberOfTrailingZeros(pieces);
            //uppercase
            boolean isKing = position[index] == KING[color];
            //moves
            HashSet<Long> movesPiece = new HashSet();
            //captures piece
            int maxCapturePiece = maxCapture;

            //2x2 directions
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_DIAGONAL}}) {//-+, -+
                for (Diagonal vertical : horizontal) {//-+ [WB]
                    if (vertical.canStep(index)) {
                        //first step
                        long move = vertical.getStep(index);

                        //king steps
                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(index, ~empty, move);
                        }
                        
                        //capture
                        long capture = move & opponent;
                        
                       //is jumpable
                        if ((capture & middle) != 0l) {
                            //square after capture
                            long step = vertical.getStep(Long.numberOfTrailingZeros(capture));
                            
                            //empty square (capture is legal)
                            if ((step & empty) == step) {
                                //empty king steps
                                if (isKing && (step & middle) == step) {
                                    step = vertical.getLine(index, ~empty, step) & empty;
                                }
                                
                                //captures to check
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ step}));//<capture, <empty>>
                                
                                //piece off board
                                empty ^= 1l << index;
                                
                                do {
                                    //captures and empty
                                    move = captureMoves.remove(0);

                                    //is captured
                                    long captures = move & opponent;
                                    
                                    //maxCapturePiece+1
                                    if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();

                                            maxCapturePiece++;
                                        }
                                        
                                        movesPiece.add(move);
                                    }
                                    
                                    //empty square(s)
                                    for (long destinations = move & empty; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                                        //empty square
                                        int to = Long.numberOfTrailingZeros(destinations);

                                        //1x4 directions
                                        for (Diagonal diagonal : Diagonal.values()) {
                                            if (diagonal.canStep(to)) {
                                                //square next of to
                                                step = diagonal.getStep(to);

                                                //king steps
                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getLine(to, ~empty, step);
                                                }

                                                //no dubbles (captures and empty)
                                                if ((step & move) == 0l) {
                                                    //capture
                                                    capture = step & opponent;

                                                    //is jumpable
                                                    if ((capture & middle) != 0l) {
                                                        //square after capture
                                                        step = diagonal.getStep(Long.numberOfTrailingZeros(capture));

                                                        //empty square (capture is legal)
                                                        if ((step & empty) == step) {
                                                            //empty king steps
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getLine(to, ~empty, step) & empty;
                                                            }

                                                            //will be checked
                                                            captureMoves.add(captures ^ capture ^ step);//<<captures>, capture, <empty>>
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all captures are checked
                                
                                //piece on board
                                empty ^= 1l << index;
                            }
                        }
                        
                        //empty no captures
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
        pruning : {
            //game over
            if (moves.isEmpty()) {
                break pruning;
            //continue alfa beta pruning
            } else if (depth > 0) {
                depth--;
            //return move value
            } else if (maxCapture == 0) {
                return node.valueOf(Long.bitCount(turn) - Long.bitCount(opponent));
            }

            //moveable pieces
            for (int from : moves.keySet()) {
                //piece
                char piece = position[from];

                //piece off board
                position[from] = EMPTY;

                //moves piece
                for (long move : moves.get(from)) {
                    //bitboard all captured pieces
                    long capture = move & opponent;
                    //empty ArrayList (capture)
                    ArrayList<Integer> captures = new ArrayList();

                    //fill captures (capture->captures)
                    for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                        captures.add(Long.numberOfTrailingZeros(copy));
                    }

                    //empty square(s)
                    for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                        //destination (empty)
                        int to = Long.numberOfTrailingZeros(destinations);                    
                        //position after move
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

                            //prune
                            if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                                break pruning;
                            }
                        }
                    }
                }

                //piece on board
                position[from] = piece;
            }
        }        
        //move value
        return alfaBeta[node.ordinal()];
    }

    //position & move -> key
    private static char[] getPosition(int color, char[] position, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        position[to] = piece;
        
        captures.forEach(capture -> position[capture] = EMPTY);
        
        //key
        return position;
    }

    //AI move
    static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, Move[]> moves) {
        //color player
        int player = 1 - ai;
        
        //empty bitboards (pieces)
        long turn = 0l;//ai
        long opponent = 0l;//player

        //fill turn (pieces[ai]->turn)
        for (int index : pieces[ai]) {
            turn ^= 1l << index;
        }
        
        //fill opponent (pieces[player]->opponent)
        for (int index : pieces[player]) {
            opponent ^= 1l << index;
        }

        //best moves
        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        //alfa
        int max = BETA;

        //moveable pieces
        for (int from : moves.keySet()) {
            char piece = position[from];
            
            //piece off board
            position[from] = EMPTY;
            
            //moves piece
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                //empty bitboard (captures)
                long capture = 0l;
                
                //fill capture (captures->capture)
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                //destination
                int to = move.getTo();
                //beta
                int min = new MinMax(Node.MIN, player).valueOf(getPosition(ai, position.clone(), piece, captures, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, AI.getValue());
                
                //alfa move (beta>=alfa) 
                if (min >= max) {
                    if (min > max) {
                        alfaMoves.clear();
                        
                        max = min;
                    }
                    
                    alfaMoves.add(move.getPieceMove(from));
                }
            }
            
            //piece on board
            position[from] = piece;
        }

        //ai move
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
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
