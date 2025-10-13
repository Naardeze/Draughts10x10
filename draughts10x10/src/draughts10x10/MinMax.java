package draughts10x10;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.MAN;
import static draughts10x10.PositionBoard.EMPTY;
import static draughts10x10.SquareBoard.GRID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * MinMax
 * 
 * Minimax with alfa beta pruning
 * extends HashMap (position, value) -> prevent recalculations
 * 
 * @author Naardez
 */

class MinMax extends HashMap<String, Integer> {
    //static constants
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
    
    //all squares with 4 neighbour squares (0<x<9 & 0<y<9)
    private static long middle = 0l;
    
    private static enum Node {
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

    private static enum Diagonal {
        MIN_LEFT(COLUMN, 0, -COLUMN) {//-- -> square[0]
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_RIGHT(COLUMN - 1, 0, -COLUMN + 1) {//+- -> square[4]
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_LEFT(COLUMN, ROW, COLUMN) {//-+ -> square[44]
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        }, 
        PLUS_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++ -> square[49]
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        };

        //canStep
        final private int column;
        final private int row;
        //getStep
        final private int step;

        Diagonal(int column, int row, int step) {
            this.column = column;
            this.row = row;

            this.step = step;
        }

        //has neightbour
        boolean canStep(int index) {
            return index % GRID != column && index / COLUMN != row;
        }

        //man step
        long getStep(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        //king steps
        abstract long getLine(int index, long occupied, long from);

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
    private int valueOf(char[] position, long turn, long opponent, MinMax minMax, int[] alfaBeta, int value, int depth) {
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
                            move = vertical.getLine(index, ~empty, move);
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
                                    step = vertical.getLine(index, ~empty, step) & empty;
                                }
                                
                                //captures to check
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ step}));//captureMove
                                
                                //piece off board
                                empty ^= 1l << index;
                                
                                //check captureMoves for extra captures
                                do {
                                    //captureMove
                                    move = captureMoves.remove(0);

                                    //captured
                                    long captures = move & opponent;
                
                                    //extra captures
                                    HashSet<Long> extraCaptures = new HashSet();
                                    
                                    //empty square(s)
                                    for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {
                                        //empty square
                                        int to = Long.numberOfTrailingZeros(destination);

                                        //1x4
                                        for (Diagonal diagonal : Diagonal.values()) {
                                            if (diagonal.canStep(to)) {
                                                //square next of to
                                                step = diagonal.getStep(to);

                                                //king steps
                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getLine(to, ~empty, step);
                                                }

                                                //extra capture
                                                if ((step & move) == 0l) {//no dubbles
                                                    capture = step & opponent;

                                                    //jumpable
                                                    if ((capture & middle) != 0l) {
                                                        //square after capture
                                                        step = diagonal.getStep(Long.numberOfTrailingZeros(capture));

                                                        //empty square
                                                        if ((step & empty) == step) {
                                                            //empty king steps
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getLine(to, ~empty, step) & empty;
                                                            }

                                                            //extra captureMove
                                                            extraCaptures.add(captures ^ capture ^ step);//captureMove
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    //extra captures to check or legal move
                                    if (!extraCaptures.isEmpty()) {
                                        captureMoves.addAll(extraCaptures);
                                    } else if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();
                                            maxCapturePiece = Long.bitCount(captures);
                                        }
                                        
                                        movesPiece.add(move);
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
        } else if (depth > 0) {
            depth--;
        //<-value
        } else if (maxCapture == 0) {
            return value;//node.valueOf(Long.bitCount(turn) - Long.bitCount(opponent));
        }
        
        value += node.valueOf(maxCapture);
            
        //3 (pruning)
        pruning : for (int from : moves.keySet()) {
            char piece = position[from];

            //piece off board
            position[from] = EMPTY;

            //moves piece
            for (long move : moves.get(from)) {
                long capture = move & opponent;
                ArrayList<Integer> captures = new ArrayList();

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
                        put(key, minMax.valueOf(key.toCharArray(), opponent ^ capture, turn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), value, depth));//search
                    }

                    //alfaBeta
                    alfaBeta[node.ordinal()] = node.toAlfaBeta(alfaBeta[node.ordinal()], get(key));

                    //alfa>=beta
                    if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                        break pruning;
                    }
                }
            }

            //piece on board
            position[from] = piece;
        }

        //<-value
        return alfaBeta[node.ordinal()];
    }
    
     //position->key
    private static char[] getPosition(int color, char[] position, char piece, ArrayList<Integer> captures, int to) {
        if (piece == MAN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        position[to] = piece;
        
        captures.forEach(index -> position[index] = EMPTY);
        
        //<-key
        return position;
    }
    
    static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, ArrayList<Integer>[]> moves, int maxCapture, int depth) {
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
        //alfa
        int max = BETA;

        //moveable pieces
        for (int from : moves.keySet()) {
            char piece = position[from];
            
            //piece off board
            position[from] = EMPTY;
            
            //moves
            for (ArrayList<Integer> move : moves.get(from)) {
                int to = move.remove(maxCapture);//.remove(maxCapture);
                long capture = 0l;
                
                //captures->capture
                for (int index : move) {
                    capture ^= 1l << index;
                }

                //beta
                int min = new MinMax(Node.MIN, player).valueOf(getPosition(ai, position.clone(), piece, move, to), opponent ^ capture, turn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, maxCapture, depth);
                
                //beta>=alfa
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
