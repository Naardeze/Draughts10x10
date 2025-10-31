package draughts; 

import static draughts.Board10x10.GRID;
import static draughts.Draughts.LEVEL;
import static draughts.Game.KING;
import static draughts.Game.PAWN;
import static draughts.PieceBoard.EMPTY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


final class MinMax extends HashMap<String, Integer> {
    final private static int ALFA =  Integer.MAX_VALUE;
    final private static int BETA = Integer.MIN_VALUE;
    
    private static enum Node {
        ALFA {//ai
            @Override
            int toAlfaBeta(int alfaBeta, int value) {//alfa
                return Math.max(alfaBeta, value);
            }

            @Override
            int valueOf(int value) {//+
                return value;
            }
        },
        BETA {//player
            @Override
            int toAlfaBeta(int alfaBeta, int value) {//beta
                return Math.min(alfaBeta, value);
            }

            @Override
            int valueOf(int value) {//-
                return -value;
            }
        };

        abstract int toAlfaBeta(int alfaBeta, int value);
        abstract int valueOf(int value);
    }
    
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    private static enum Diagonal {
        DOWN_LEFT_TO_RIGHT(COLUMN, 0, -COLUMN) {//--
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        DOWN_RIGHT_TO_LEFT(COLUMN - 1, 0, -COLUMN + 1) {//+-
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        UP_RIGHT_TO_LEFT(COLUMN, ROW, COLUMN) {//-+
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = RIGHT_TO_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        }, 
        UP_LEFT_TO_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = LEFT_TO_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
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

        final private static long[] LEFT_TO_RIGHT = new long[GRID];
        final private static long[] RIGHT_TO_LEFT = new long[GRID - 1];

        static {
            for (int i = 0; i < LEFT_TO_RIGHT.length; i++) {
                LEFT_TO_RIGHT[i] = 0l;

                //bit: 4-0, 5-45 (first), bitCount: 1-9, 9-1 (tiles)
                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID, bitCount = 0; bitCount < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; bitCount++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    LEFT_TO_RIGHT[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < RIGHT_TO_LEFT.length; i++) {
                RIGHT_TO_LEFT[i] = 0l;

                //bit: 0-4-44 (first), bitCount: 2-10-2 (tiles)
                for (int bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID, bitCount = 0; bitCount < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; bitCount++, bit += COLUMN - bit / COLUMN % 2) {
                    RIGHT_TO_LEFT[i] ^= 1l << bit;
                }
            }
        }
    }

    private static long middle = 0l;//x>0 & x<9 & y>0 & y<9

    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //1 moves maxCapture
    //2 alfa beta pruning
    private int valueOf(char[] board, long turn, long opponent, MinMax minMax, int[] alfaBeta, int value, int depth) {
        //1 moves maxCapture
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
    
        for (long empty = ~(turn ^ opponent), pieces = turn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int from = Long.numberOfTrailingZeros(pieces);
            boolean isKing = board[from] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;

            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.DOWN_LEFT_TO_RIGHT, Diagonal.UP_RIGHT_TO_LEFT}, {Diagonal.DOWN_RIGHT_TO_LEFT, Diagonal.UP_LEFT_TO_RIGHT}}) {//2x2
                for (Diagonal vertical : horizontal) {//[WB]
                    if (vertical.hasNext(from)) {
                        long move = vertical.getNext(from);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(from, ~empty, move);
                        }
                       
                        long capture = move & opponent;
                        
                        if ((capture & middle) != 0l) {
                            long step = vertical.getNext(Long.numberOfTrailingZeros(capture));//tile after capture
                            
                            if ((step & empty) == step) {
                                if (isKing && (step & middle) == step) {
                                    step = vertical.getLine(from, ~empty, step) & empty;
                                }
                                
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ step}));//captureMove
                                
                                empty ^= 1l << from;
                                
                                do {//check moves for extra captures
                                    move = captureMoves.remove(0);

                                    long captures = move & opponent;
                
                                    if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();
                                            maxCapturePiece++;
                                        }
                                        
                                        movesPiece.add(move);//legal move
                                    }
                                    
                                    for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {//empty
                                        int to = Long.numberOfTrailingZeros(destination);

                                        for (Diagonal diagonal : Diagonal.values()) {//1x4
                                            if (diagonal.hasNext(to)) {
                                                step = diagonal.getNext(to);
                                                
                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getLine(to, ~empty, step);
                                                }

                                                if ((step & move) == 0l) {//no dubbels
                                                    capture = step & opponent;

                                                    if ((capture & middle) != 0l) {
                                                        step = diagonal.getNext(Long.numberOfTrailingZeros(capture));//tile after capture

                                                        if ((step & empty) == step) {
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getLine(to, ~empty, step) & empty;
                                                            }

                                                            captureMoves.add(captures ^ capture ^ step);//captureMove
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all moves checked
                                
                                empty ^= 1l << from;
                            }
                        }
            
                        //empty tiles
                        if (maxCapturePiece == 0 && (isKing || vertical == horizontal[color])) {
                            move &= empty;

                            if (move != 0l) {//legal move
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
                
                moves.put(from, movesPiece);
            }
        }
        
        //2 alfa beta pruning
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//continue
            depth -= node.ordinal();//ALFA-0 BETA-
        } else if (maxCapture == 0) {
            return value;
        }

        value += node.valueOf(maxCapture);

        for (int from : moves.keySet()) {
            char piece = board[from];

            board[from] = EMPTY;

            for (long move : moves.get(from)) {
                long captures = move & opponent;
                ArrayList<Integer> captured = new ArrayList();

                for (long l = captures; l != 0l; l ^= Long.lowestOneBit(l)) {//captures -> captured
                    captured.add(Long.numberOfTrailingZeros(l));
                }

                for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {//empty
                    int to = Long.numberOfTrailingZeros(destination);                    
                    String key = String.valueOf(getBoard(color, board.clone(), piece, captured, to));

                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), opponent ^ captures, turn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), value, depth));
                    }

                    alfaBeta[node.ordinal()] = node.toAlfaBeta(alfaBeta[node.ordinal()], get(key));

                    if (alfaBeta[Node.ALFA.ordinal()] >= alfaBeta[Node.BETA.ordinal()]) {//alfa>=beta
                        return alfaBeta[node.ordinal()];
                    }
                }
            }

            board[from] = piece;
        }
        
        //<-value
        return alfaBeta[node.ordinal()];
    }
    
    private static char[] getBoard(int color, char[] board, char piece, ArrayList<Integer> captured, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        board[to] = piece;        

        captured.forEach(capture -> board[capture] = EMPTY);
        
        //<-key
        return board;
    }
    
    static ArrayList<Integer> getAIMove(int ai, char[] board, HashSet<Integer>[] pieces, HashMap<Integer, ArrayList<Integer>[]> moves, int maxCapture) {
        int player = 1 - ai;//opponent
        
        long turn = 0l;//ai
        long opponent = 0l;//player

        for (int index : pieces[ai]) {
            turn ^= 1l << index;
        }
        
        for (int index : pieces[player]) {
            opponent ^= 1l << index;
        }
            
        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        int alfa = BETA;
        
        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;
            
            for (ArrayList<Integer> move : moves.get(from)) {
                int to = move.remove(maxCapture);
                long captures = 0l;
                
                for (int capture : move) {
                    captures ^= 1l << capture;
                }

                int beta = new MinMax(Node.BETA, player).valueOf(getBoard(ai, board.clone(), piece, move, to), opponent ^ captures, turn ^ (1l << from ^ 1l << to), new MinMax(Node.ALFA, ai), new int[] {BETA, ALFA}, maxCapture, LEVEL.getValue());
                
                if (beta >= alfa) {
                    if (beta > alfa) {
                        alfaMoves.clear();                        
                        alfa = beta;
                    }
                    
                    move.add(0, from);
                    move.add(to);
                    
                    alfaMoves.add(move);
                }
            }
            
            board[from] = piece;
        }
       
        //<-ai move
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
    }
    
    static {//middle (Diagonal.values() -> canStep(i) = true (4xtrue))
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<i<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
