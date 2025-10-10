package draughts10x10.ai;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.MAN;
import static draughts10x10.board.PositionBoard.EMPTY;
import static draughts10x10.board.SquareBoard.GRID;
import java.util.Arrays;
import java.util.ArrayList;
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

public class MinMax extends HashMap<String, Integer> {
    final static int COLUMN = GRID / 2;//5
    final static int ROW = GRID - 1;//9
    
    final private static int ALFA = Integer.MAX_VALUE;//max
    final private static int BETA = Integer.MIN_VALUE;//min
    
    //all squares with 4 neighbour squares (0<x<9 & 0<y<9)
    private static long middle = 0l;
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //1: moves and maxCaptue
    //2: pruning
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
                                
                                //piece off board
                                empty ^= 1l << index;
                                
                                //check captureMoves for extra captures
                                do {
                                    //captureMove
                                    move = captureMoves.remove(0);

                                    //captured
                                    long captures = move & opponent;
                
                                    //extra captures
                                    ArrayList<Long> extraCaptures = new ArrayList();
                                    
                                    //empty square(s)
                                    for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {
                                        int to = Long.numberOfTrailingZeros(destination);

                                        //1x4
                                        for (Diagonal diagonal : Diagonal.values()) {
                                            if (diagonal.canStep(to)) {
                                                step = diagonal.getStep(to);

                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getLine(to, ~empty, step);
                                                }

                                                //capture (no dubbles)
                                                if ((step & move) == 0l) {
                                                    capture = step & opponent;

                                                    if ((capture & middle) != 0l) {
                                                        step = diagonal.getStep(Long.numberOfTrailingZeros(capture));

                                                        if ((step & empty) == step) {
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getLine(to, ~empty, step) & empty;
                                                            }

                                                            extraCaptures.add(captures ^ capture ^ step);//captureMove
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
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
            
            //pieces
            for (int from : moves.keySet()) {
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
        }        

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
    
    public static ArrayList<Integer> getAIMove(int ai, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, ArrayList<Integer>[]> moves, int maxCapture, int depth) {
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
            
            position[from] = EMPTY;
            
            //moves
            for (ArrayList<Integer> move : moves.get(from)) {
                int to = move.remove(maxCapture);
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
