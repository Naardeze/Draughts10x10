package ai;

import static board.PositionBoard.EMPTY;
import static board.PositionBoard.WB;
import static board.SquareBoard.SIZE;
import static draughts.Game.KING;
import static draughts.Game.PAWN;
import draughts.Move;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
    hashmap stores position values
    
    minimax pruning

    returns best move
*/

public class MinMax extends HashMap<String, Integer> {
    //squares p row, promotion row
    final public static int ROW = SIZE / 2;//5
    final public static int PROMOTION = SIZE - 1;//9

    //alfa beta values
    final private static int ALFA = Integer.MAX_VALUE;
    final private static int BETA = Integer.MIN_VALUE;

    //center squares
    private static long boardCenter = 0l;
    
    //min max-> alfa beta
    private enum Node {
        MIN {
            @Override
            public boolean isAlfaBeta(int value, int alfaBeta) {//alfa
                return value < alfaBeta;
            }
            @Override
            public int valueOf(int value) {//noTurn - hasTurn
                return -value;
            }
        },
        MAX {
            @Override
            public boolean isAlfaBeta(int value, int alfaBeta) {//beta
                return value > alfaBeta;
            }
            @Override
            public int valueOf(int value) {//hasTurn - noTurn
                return value;
            }
        };

        public abstract boolean isAlfaBeta(int value, int alfaBeta);
        public abstract int valueOf(int value);
    }
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }

    //move result
    //looks like turn methode in Game class (don't invent the wheel twice)
    private int valueOf(char[] board, long hasTurn, long noTurn, MinMax minMax, int[] alfaBeta, int depth) {
        HashMap<Integer, ArrayList<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        for (long occupied = hasTurn ^ noTurn, empty = ~occupied, captureMiddle = noTurn & boardCenter, player = hasTurn; player != 0l; player ^= Long.lowestOneBit(player)) {
            int index = Long.numberOfTrailingZeros(player);
            boolean isKing = board[index] == KING[color];
            ArrayList<Long> pieceMoves = new ArrayList();
            
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}}) {
                for (Diagonal vertical : horizontal) {
                    if (vertical.hasNext(index)) {
                        long move = vertical.getNext(index);

                        //king steps
                        if (isKing && (move & boardCenter & empty) == move) {
                            move = vertical.getKingSteps(index, occupied, move);
                        }
                        
                        //capture
                        long capture = move & captureMiddle;
                        
                        if (capture != 0l) {
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            if ((next & empty) == next) {
                                //king steps
                                if (isKing && (next & boardCenter) == next) {
                                    next = vertical.getKingSteps(index, occupied, next) & empty;
                                }
                                
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture | next}));
                                
                                occupied ^= 1l << index;
                                empty = ~occupied;
                                
                                //extra captures
                                do {
                                    move = captureMoves.remove(0);
                                 
                                    long captures = move & captureMiddle;
                                    
                                    if (Long.bitCount(captures) >= maxCapture) {
                                        if (Long.bitCount(captures) > maxCapture) {
                                            pieceMoves.clear();
                                            moves.clear();
                                            
                                            maxCapture++;
                                        }
                                        
                                        pieceMoves.add(move);
                                    }
                                    
                                    if (captures != captureMiddle) {//? are pieces left to capture
                                        for (long destinations = move & empty; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                                            int step = Long.numberOfTrailingZeros(destinations);
                                            
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                if (diagonal.hasNext(step)) {
                                                    next = diagonal.getNext(step);
                                                    
                                                    //king steps
                                                    if (isKing && (next & boardCenter & empty) == next) {
                                                        next = diagonal.getKingSteps(step, occupied, next);
                                                    }
                                                    
                                                    if ((next & move) == 0l) {
                                                        //extra capture
                                                        capture = next & captureMiddle;
                                                        
                                                        if (capture != 0l) {
                                                            next = diagonal.getNext(Long.numberOfTrailingZeros(capture));
                                
                                                            if ((next & empty) == next) {                                                                
                                                                //king steps
                                                                if (isKing && (next & boardCenter) == next) {
                                                                    next = diagonal.getKingSteps(step, occupied, next) & empty;
                                                                }
                                                                
                                                                captureMoves.add(captures | capture | next);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());
                                
                                occupied ^= 1l << index;
                                empty = ~occupied;
                            }
                        }
                        
                        //empty square (no  captures)
                        if (maxCapture == 0 && (isKing || vertical == horizontal[color])) {
                            move &= empty;
                            
                            if (move != 0l) {
                                pieceMoves.add(move);
                            }
                        }
                    }
                }
            }
            
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves);
            }
        }
        
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//continue searching
            depth--;
        } else if (maxCapture == 0) {//keep searching while board has captures
            return node.valueOf(Long.bitCount(hasTurn) - Long.bitCount(noTurn));
        }

        //pruning
        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;
            
            for (long move : moves.get(from)) {
                long capture = move & noTurn;
                ArrayList<Integer> captures = new ArrayList();
                
                for (long bitBoard = capture; bitBoard != 0l; bitBoard ^= Long.lowestOneBit(bitBoard)) {
                    captures.add(Long.numberOfTrailingZeros(bitBoard));
                }
                
                for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                    int to = Long.numberOfTrailingZeros(destinations);                    
                    String key = String.valueOf(getBoard(color, board.clone(), piece, captures, to));
                    
                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth));
                    }
                    
                    //move result
                    int value = get(key);
                    
                    if (node.isAlfaBeta(value, alfaBeta[node.ordinal()])) {
                        alfaBeta[node.ordinal()] = value;
                        
                        //prune
                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            return value;
                        }
                    }
                }
            }
            
            board[from] = piece;
        }
        
        return alfaBeta[node.ordinal()];
    }

    //board move
    private static char[] getBoard(int color, char[] board, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / ROW == color * PROMOTION) {
            piece = KING[color];
        }
        
        board[to] = piece;
        captures.forEach(capture -> board[capture] = EMPTY);
        
        return board;
    }

    //best move
    public static ArrayList<Integer> getMove(int ai, char[] board, HashMap<Integer, Move[]>  moves, int depth) {
        //isTurn & noTurn bitboards
        long[] player = {0l, 0l};
        
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                player[WB.indexOf(Character.toLowerCase(board[i]))] ^= 1l << i;
            }
        }
        
        int opponent = 1 - ai;
        int max = BETA;
        
        ArrayList<ArrayList<Integer>> best = new ArrayList();

        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;
            
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;
                
                for (int index : captures) {
                    capture |= 1l << index;
                }
                
                int to = move.getDestination();                
                int min = new MinMax(Node.MIN, opponent).valueOf(getBoard(ai, board.clone(), piece, captures, to), player[opponent] ^ capture, player[ai] ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, depth);
                
                if (min >= max) {
                    if (min > max) {
                        best.clear();
                        
                        max = min;
                    }
                    
                    best.add(move.getPieceMove(from));
                }
            }
            
            board[from] = piece;
        }
        
        //best move (random)
        return best.get((int) (Math.random() * best.size()));
    }
    
    static {
        //middle
        for (int i = ROW; i < PROMOTION * ROW; i++) {
            if (i % SIZE != ROW - 1 && i % SIZE != ROW) {
                boardCenter ^= 1l << i;
            }
        }
    }
}
