package draughts;

import static draughts.Game.KING;
import static draughts.Game.MAN;
import static draughts.PositionBoard.EMPTY;
import static draughts.SquareBoard.GRID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * MinMax
 * 
 * Minimax with alfa beta pruning
 * extend HashMap (position, value) -> prevent recalculations
 * 1 level = 2 moves (alfa beta)
 * 
 * -valueOf -> 1 moves, maxCapture
 *             2 evaluation (depth=0 -> continues searching while maxCapture > 0)
 *             3 alfa beta (pruning)
 * -getPosition -> position after move (key)
 * -getAIMove -> best move
 * 
 * enum Node -> evaluation (alfa beta)
 * enum Direction -> move in 4 directions (bitboards)
 * 
 * Special Thanx to Logic Crazy Chess
 * 
 * @author Naardez
 */

class MinMax extends HashMap<String, Integer> {
    //static constants
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int MIN = Integer.MIN_VALUE;//beta
    final private static int MAX = Integer.MAX_VALUE;//alfa
    
    //center squares (0<x<9 & 0<y<9)
    private static long middle = 0l;
    
    //alfaBeta evaluation
    private static enum Node {
        ALFA {//ai
            @Override
            int toAlfaBeta(int alfaBeta, int value) {
                return Math.max(alfaBeta, value);
            }

            @Override
            int valueOf(int value) {
                return value;
            }
        }, 
        BETA {//player
            @Override
            int toAlfaBeta(int alfaBeta, int value) {
                return Math.min(alfaBeta, value);
            }

            @Override
            int valueOf(int value) {
                return -value;
            }
        };

        abstract int toAlfaBeta(int alfaBeta, int value);
        abstract int valueOf(int value);//turn-opponent

    }

    //move in 4 directions
    private static enum Diagonal {
        MIN_LEFT(COLUMN, 0, -COLUMN) {//-- -> square[0]
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_RIGHT(COLUMN - 1, 0, -COLUMN + 1) {//+- -> square[4]
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_LEFT(COLUMN, ROW, COLUMN) {//-+ -> square[44]
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        }, 
        PLUS_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++ -> square[49]
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        };

        final private int column;
        final private int row;
        final private int step;

        Diagonal(int column, int row, int step) {
            this.column = column;
            this.row = row;
            this.step = step;
        }

        //can move?
        boolean canStep(int index) {
            return index % GRID != column && index / COLUMN != row;
        }

        //next square
        long getStep(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        //king steps
        abstract long getKingSteps(int index, long occupied, long from);

        //diagonals
        final private static long[] LEFT_TO_RIGHT = new long[GRID];//->
        final private static long[] RIGHT_TO_LEFT = new long[GRID - 1];//<-

        static {
            for (int i = 0; i < LEFT_TO_RIGHT.length; i++) {
                LEFT_TO_RIGHT[i] = 0l;

                //bit: 4-0, 5-45
                //bitCount: 1-9, 9-1
                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, bitCount = 0; bitCount < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bitCount++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    LEFT_TO_RIGHT[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < RIGHT_TO_LEFT.length; i++) {
                RIGHT_TO_LEFT[i] = 0l;

                //bit: 0-4-44
                //bitCount: 2-10-2
                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, bitCount = 0; bitCount < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bitCount++, bit += COLUMN - bit / COLUMN % 2) {
                    RIGHT_TO_LEFT[i] ^= 1l << bit;
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
    
    //1: moves and maxCaptue
    //2: evalation
    //3: pruning
    //like Game.turn (don't invent the wheel twice)
    private int valueOf(char[] position, long turn, long opponent, MinMax minMax, int[] alfaBeta, int level) {
        //1 (moves and maxCaptue)
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
    
        //pieces
        for (long empty = ~(turn ^ opponent), pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int index = Long.numberOfTrailingZeros(pieces);
            boolean isKing = position[index] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;

            //2x2
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_LEFT, Diagonal.PLUS_LEFT}, {Diagonal.MIN_RIGHT, Diagonal.PLUS_RIGHT}}) {//-+
                for (Diagonal vertical : horizontal) {//-+ [WB] (man step forward)
                    if (vertical.canStep(index)) {
                        //first step
                        long move = vertical.getStep(index);

                        //king steps
                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getKingSteps(index, ~empty, move);
                        }
                        
                        //capture
                        long capture = move & opponent;
                        
                        //jumpable
                        if ((capture & middle) != 0l) {
                            //square after capture
                            long step = vertical.getStep(Long.numberOfTrailingZeros(capture));
                            
                            //empty square
                            if ((step & empty) == step) {
                                //empty king steps
                                if (isKing && (step & middle) == step) {
                                    step = vertical.getKingSteps(index, ~empty, step) & empty;
                                }
                                
                                //captures to check
                                LinkedList<Long> captureMoves = new LinkedList(Arrays.asList(new Long[] {capture ^ step}));//captureMove
                                
                                //piece off board
                                empty ^= 1l << index;
                                
                                //check for extra captures
                                do {
                                    //captureMove to check
                                    move = captureMoves.removeFirst();

                                    //captured pieces
                                    long captures = move & opponent;
                
                                    //legal move
                                    if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();
                                            maxCapturePiece = Long.bitCount(captures);
                                        }
                                        
                                        movesPiece.add(move);
                                    }
                                    
                                    //empty square(s)  last captures
                                    for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {
                                        //empty square
                                        int to = Long.numberOfTrailingZeros(destination);

                                        //1x4
                                        for (Diagonal diagonal : Diagonal.values()) {
                                            if (diagonal.canStep(to)) {
                                                //first square diagonal                                                
                                                step = diagonal.getStep(to);
                                                
                                                //king steps
                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getKingSteps(to, ~empty, step);
                                                }

                                                //no dubbles
                                                if ((step & move) == 0l) {
                                                    //extra capture
                                                    capture = step & opponent;

                                                    //jumpable
                                                    if ((capture & middle) != 0l) {
                                                        //square after capture
                                                        step = diagonal.getStep(Long.numberOfTrailingZeros(capture));

                                                        //square after capture is empty
                                                        if ((step & empty) == step) {
                                                            //empty king steps
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getKingSteps(to, ~empty, step) & empty;
                                                            }

                                                            //extra captureMove
                                                            captureMoves.addLast(captures ^ capture ^ step);//captureMove
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all capture moves checked
                                
                                //piece on board
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
        
        //2 (evaluation)
        //game over
        if (moves.isEmpty()) {
            return alfaBeta[node.ordinal()];
        //pruning
        } else if (level > 0) {
            level -= node.ordinal();//ALFA-0 BETA-1
        //<-turn-opponent
        } else if (maxCapture == 0) {
            return node.valueOf(Long.bitCount(turn) - Long.bitCount(opponent));
        }
        
        //3 (pruning)
        pruning : for (int from : moves.keySet()) {
            char piece = position[from];

            //piece off board
            position[from] = EMPTY;

            //moves piece
            for (long move : moves.get(from)) {
                long capture = move & opponent;
                LinkedList<Integer> captures = new LinkedList();

                //capture->captures
                for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                    captures.add(Long.numberOfTrailingZeros(copy));
                }

                //empty square(s)
                for (long destination = move ^ capture; destination != 0l; destination ^= Long.lowestOneBit(destination)) {
                    //empty square
                    int to = Long.numberOfTrailingZeros(destination);                    
                    //position after move
                    String key = String.valueOf(getPosition(color, position.clone(), piece, captures, to));

                    //look up?
                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), opponent ^ capture, turn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), level));//search
                    }

                    //alfaBeta
                    alfaBeta[node.ordinal()] = node.toAlfaBeta(alfaBeta[node.ordinal()], get(key));

                    //max>=min
                    if (alfaBeta[Node.ALFA.ordinal()] >= alfaBeta[Node.BETA.ordinal()]) {
                        break pruning;
                    }
                }
            }

            //piece on board
            position[from] = piece;
        }

        //<-alfaBeta
        return alfaBeta[node.ordinal()];
    }
    
     //position->key
    private static char[] getPosition(int color, char[] position, char piece, LinkedList<Integer> captures, int to) {
        if (piece == MAN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        position[to] = piece;
        
        captures.forEach(index -> position[index] = EMPTY);
        
        //<-key
        return position;
    }
    
    static LinkedList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, LinkedList<Integer>[]> moves, int level) {
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

        ArrayList<LinkedList<Integer>> alfaMoves = new ArrayList();
        //max
        int alfa = MIN;

        //moveable pieces
        for (int from : moves.keySet()) {
            char piece = position[from];
            
            //piece off board
            position[from] = EMPTY;
            
            //moves
            for (LinkedList<Integer> move : moves.get(from)) {
                int to = move.removeLast();
                long capture = 0l;
                
                //captures->capture
                for (int index : move) {
                    capture ^= 1l << index;
                }

                //min
                int beta = new MinMax(Node.BETA, player).valueOf(getPosition(ai, position.clone(), piece, move, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new MinMax(Node.ALFA, ai), new int[] {MIN, MAX}, level);
                
                //min>=max
                if (beta >= alfa) {
                    if (beta > alfa) {
                        alfaMoves.clear();
                        
                        alfa = beta;
                    }
                    
                    move.addFirst(from);
                    move.addLast(to);
                    
                    alfaMoves.add(move);
                }
            }
            
            //piece on board
            position[from] = piece;
        }

        //<-ai move
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
    }
    
    static {//middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
