package draughts10x10;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PositionBoard.EMPTY;
import static draughts10x10.SquareBoard.SIZE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/* MinMax
 *
 * minimax with alfa beta pruning
 * 
 * enum Node -> evaluation
 * enum Diagonal -> move in 4 directions (bitboards)
 *
 * -valueOf -> move result
 * -getBoard (static) -> moved board
 * -getAIMove (static) -> best move
 * 
 * Special Thanx to Logic Crazy Chess
 */

final class MinMax extends HashMap<String, Integer> {//<position, value>    
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
    
    private static enum Diagonal {
        DOWN_DIAGONAL(COLUMN, 0, -COLUMN) {//--
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / SIZE];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        DOWN_ANTI_DIAGONAL(COLUMN - 1, 0, -COLUMN + 1) {//+-
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = ANTI_DIAGONAL[index % COLUMN + index / SIZE];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        UP_ANTI_DIAGONAL(COLUMN, ROW, COLUMN) {//-+
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = ANTI_DIAGONAL[index % COLUMN + index / SIZE];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        }, 
        UP_DIAGONAL(COLUMN - 1, ROW, COLUMN + 1) {//++
            @Override
            long getLine(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / SIZE];

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
            return index % SIZE != column && index / COLUMN != row;
        }

        long getNext(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        abstract long getLine(int index, long occupied, long from);

        //mask -+
        final static long[] DIAGONAL = new long[SIZE];//-+
        final static long[] ANTI_DIAGONAL = new long[SIZE - 1];//+-

        static {
            for (int i = 0; i < DIAGONAL.length; i++) {
                DIAGONAL[i] = 0l;

                //4-0, 5-45
                for (int bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * SIZE, j = 0; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; j++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    DIAGONAL[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < ANTI_DIAGONAL.length; i++) {
                ANTI_DIAGONAL[i] = 0l;
        
                //0-4-44
                for (int j = 0, bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * SIZE; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; j++, bit += COLUMN - bit / COLUMN % 2) {
                    ANTI_DIAGONAL[i] ^= 1l << bit;
                }
            }
        }
    }
    
    //static constants
    final private static int COLUMN = SIZE / 2;//5
    final private static int ROW = SIZE - 1;//9

    //move in 4 directions (x>0 & x<9 & y>0 & y<9)
    private static long middle = 0l;
    
    //min max
    final private static int ALFA = Integer.MAX_VALUE;
    final private static int BETA = Integer.MIN_VALUE;

    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //like Game.turn (don't invent the wheel twice)
    //1: moves and maxCapture
    //2: evaluation
    //3: pruning
    private int valueOf(char[] position, long hasTurn, long noTurn, MinMax minMax, int[] alfaBeta, int depth) {
        //1 moves and maxCapture
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        for (long empty = ~(hasTurn ^ noTurn), captureMiddle = noTurn & middle, turn = hasTurn; turn != 0l; turn ^= Long.lowestOneBit(turn)) {
            int index = Long.numberOfTrailingZeros(turn);
            boolean isKing = position[index] == KING[color];
            HashSet<Long> pieceMoves = new HashSet();
            int pieceMaxCapture = maxCapture;
            
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.DOWN_DIAGONAL, Diagonal.UP_ANTI_DIAGONAL}, {Diagonal.DOWN_ANTI_DIAGONAL, Diagonal.UP_DIAGONAL}}) {//{{WB}, {WB}}
                for (Diagonal vertical : horizontal) {//{WB}
                    if (vertical.hasNext(index)) {
                        long move = vertical.getNext(index);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(index, ~empty, move);
                        }

                        long capture = move & captureMiddle;
                        //capture
                        if (capture != 0l) {
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            if ((next & empty) == next) {
                                if (isKing && (next & middle) == next) {
                                    next = vertical.getLine(index, ~empty, next) & empty;
                                }
                                
                                //capturemoves to check for extra captures
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ next}));//<capture, empty squares>
                                
                                empty ^= 1l << index;
                               
                                //extra captures
                                do {
                                    move = captureMoves.remove(0);
                                 
                                    long captures = move & captureMiddle;
                                    
                                    //maxCapturePiece +1
                                    if (Long.bitCount(captures) >= pieceMaxCapture) {
                                        if (Long.bitCount(captures) > pieceMaxCapture) {
                                            pieceMoves.clear();
                                            pieceMaxCapture++;
                                        }
                                        
                                        pieceMoves.add(move);
                                    }
                                    
                                    //not all captured
                                    if (captures != captureMiddle) {
                                        for (long destinations = move & empty; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {//empty square(s)
                                            int to = Long.numberOfTrailingZeros(destinations);
                                            
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                if (diagonal.hasNext(to)) {
                                                    next = diagonal.getNext(to);
                                                    
                                                    if (isKing && (next & middle & empty) == next) {
                                                        next = diagonal.getLine(to, ~empty, next);
                                                    }

                                                    //no dubbel steps
                                                    if ((next & move) == 0l) {
                                                        capture = next & captureMiddle;
                                                        
                                                        if (capture != 0l) {
                                                            next = diagonal.getNext(Long.numberOfTrailingZeros(capture));

                                                            if ((next & empty) == next) {
                                                                if (isKing && (next & middle) == next) {
                                                                    next = diagonal.getLine(to, ~empty, next) & empty;
                                                                }

                                                                captureMoves.add(captures ^ capture ^ next);//<captures, extra capture, empty squares>
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all captures are checked
                                
                                empty ^= 1l << index;
                            }
                        }
                        
                        //0 captures
                        if (pieceMaxCapture == 0 && (isKing || vertical == horizontal[color])) {
                            move &= empty;
                            
                            if (move != 0l) {
                                pieceMoves.add(move);
                            }
                        }
                    }
                }
            }
            
            //moveable
            if (!pieceMoves.isEmpty()) {
                if (pieceMaxCapture > maxCapture) {
                    moves.clear();
                    maxCapture = pieceMaxCapture;
                }
                
                moves.put(index, pieceMoves);
            }
        }
        
        //2 evaluation
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//extra search
            depth--;
        } else if (maxCapture == 0) {//move result
            return node.valueOf(Long.bitCount(hasTurn) - Long.bitCount(noTurn));
        }

        //3 pruning
        pruning : for (int from : moves.keySet()) {
            char piece = position[from];

            position[from] = EMPTY;

            for (long move : moves.get(from)) {
                long capture = move & noTurn;
                ArrayList<Integer> captures = new ArrayList();

                //capture -> <captures>
                for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                    captures.add(Long.numberOfTrailingZeros(copy));
                }
                
                for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                    int to = Long.numberOfTrailingZeros(destinations);                    
                    String key = String.valueOf(getPosition(color, position.clone(), piece, captures, to));

                    //deja vu?
                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth));
                    }
                    
                    //move result
                    int value = get(key);

                    if (node.isAlfaBeta(alfaBeta[node.ordinal()], value)) {
                        alfaBeta[node.ordinal()] = value;

                        //prune
                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            break pruning;
                        }
                    }
                }
            }

            position[from] = piece;
        }
        
        //move result
        return alfaBeta[node.ordinal()];
    }

    //position (key) -> promotion, move, captures
    private static char[] getPosition(int color, char[] position, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        position[to] = piece;
        
        captures.forEach(capture -> position[capture] = EMPTY);
        
        return position;
    }

    static ArrayList<Integer> getAIMove(int color, char[] position, HashSet<Integer>[] pieces, HashMap<Integer, Move[]> moves, int ai) {
        int opponent = 1 - color;
        
        //bitboards pieces
        long hasTurn = 0l;//ai
        long noTurn = 0l;//player

        for (int index : pieces[color]) {
            hasTurn ^= 1l << index;
        }
        
        for (int index : pieces[opponent]) {
            noTurn ^= 1l << index;
        }
        
        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        int max = BETA;

        for (int from : moves.keySet()) {
            char piece = position[from];
            
            position[from] = EMPTY;
            
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;
                
                //captures -> capture
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                int to = move.getTo();
                int min = new MinMax(Node.MIN, opponent).valueOf(getPosition(color, position.clone(), piece, captures, to), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, color), new int[] {ALFA, BETA}, ai);
                
                //alfa move
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
        
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
    }
    
    //middle
    static {
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % SIZE != COLUMN - 1 && i % SIZE != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
