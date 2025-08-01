package draughts10x10.ai;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import draughts10x10.Move;
import static draughts10x10.board.PositionBoard.EMPTY;
import static draughts10x10.board.PositionBoard.WB;
import static draughts10x10.board.SquareBoard.SIZE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/*
    hashmap stores position values    
    minimax with alfa beta pruning
*/

public class MinMax extends HashMap<String, Integer> {
    //squares p row, promotion row
    final public static int COLUMN = SIZE / 2;//5
    final public static int ROW = SIZE - 1;//9

    //alfa beta values
    final private static int ALFA = Integer.MIN_VALUE;
    final private static int BETA = Integer.MAX_VALUE;

    //squares with 4 connecting squares
    private static long middle = 0l;
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }

    //move value
    //looks like turn methode in Game class (don't invent the wheel twice)
    private int valueOf(char[] board, long hasTurn, long noTurn, MinMax minMax, int[] alfaBeta, int depth) {
        HashMap<Integer, ArrayList<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        //occupied -> all pieces white and black
        //empty -> all squares not occupied
        //captureMiddle -> all pieces on center squares
        //turn -> hasTurn
        for (long occupied = hasTurn ^ noTurn, empty = ~occupied, captureMiddle = noTurn & middle, turn = hasTurn; turn != 0l; turn ^= Long.lowestOneBit(turn)) {
            int index = Long.numberOfTrailingZeros(turn);
            boolean isKing = board[index] == KING[color];
            ArrayList<Long> pieceMoves = new ArrayList();
            
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_DIAGONAL}}) {
                for (Diagonal vertical : horizontal) {
                    if (vertical.hasNext(index)) {
                        long move = vertical.getNext(index);

                        //king steps
                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getKingSteps(index, occupied, move);
                        }
                        
                        //capture
                        long capture = move & captureMiddle;
                        
                        if (capture != 0l) {
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            if ((next & empty) == next) {
                                //king steps
                                if (isKing && (next & middle) == next) {
                                    next = vertical.getKingSteps(index, occupied, next) & empty;
                                }
                                
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture | next}));
                                
                                occupied ^= 1l << index;
                                empty = ~occupied;
                                
                                //extra captures
                                do {
                                    move = captureMoves.remove(0);
                                 
                                    long captures = move & captureMiddle;
                                    int bitCount = Long.bitCount(captures);
                                    
                                    if (bitCount >= maxCapture) {
                                        if (bitCount > maxCapture) {
                                            pieceMoves.clear();
                                            moves.clear();
                                            
                                            maxCapture++;
                                        }
                                        
                                        pieceMoves.add(move);
                                    }
                                    
                                    if (captures != captureMiddle) {//? are pieces left to capture
                                        for (long to = move & empty; to != 0l; to ^= Long.lowestOneBit(to)) {
                                            int step = Long.numberOfTrailingZeros(to);
                                            
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                if (diagonal.hasNext(step)) {
                                                    next = diagonal.getNext(step);
                                                    
                                                    //king steps
                                                    if (isKing && (next & middle & empty) == next) {
                                                        next = diagonal.getKingSteps(step, occupied, next);
                                                    }
                                                    
                                                    if ((next & move) == 0l) {
                                                        //extra capture
                                                        capture = next & captureMiddle;
                                                        
                                                        if (capture != 0l) {
                                                            next = diagonal.getNext(Long.numberOfTrailingZeros(capture));
                                
                                                            if ((next & empty) == next) {                                                                
                                                                //king steps
                                                                if (isKing && (next & middle) == next) {
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
                        
                        //no  captures
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
            depth -= node.ordinal();
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
                    int value;
                    
                    if (!containsKey(key)) {
                        value = minMax.valueOf(key.toCharArray(), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth);
                        
                        put(key, value);
                    } else {
                        value = get(key);
                    }
                    
                    if (node.isAlfaBeta(alfaBeta[node.ordinal()], value)) {
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

    //moved board
    private static char[] getBoard(int color, char[] board, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }
        
        board[to] = piece;

        captures.forEach(capture -> board[capture] = EMPTY);
        
        return board;
    }

    //best move
    public static ArrayList<Integer> getMove(int ai, char[] board, HashMap<Integer, Move[]>  moves, int depth) {
        long[] player = {0l, 0l};
        
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                player[WB.indexOf(Character.toLowerCase(board[i]))] ^= 1l << i;
            }
        }

        ArrayList<ArrayList<Integer>> bestMoves = new ArrayList();

        int opponent = 1 - ai;
        int max = ALFA;

        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;
            
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;
                
                for (int index : captures) {
                    capture |= 1l << index;
                }
                
                int to = move.getTo();                
                int min = new MinMax(Node.MIN, opponent).valueOf(getBoard(ai, board.clone(), piece, captures, to), player[opponent] ^ capture, player[ai] ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, depth);
                
                if (min >= max) {
                    if (min > max) {
                        bestMoves.clear();
                        
                        max = min;
                    }
                    
                    bestMoves.add(move.getBoardMove(from));
                }
            }
            
            board[from] = piece;
        }
        
        //best move (random)
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }
    
    static {
        //boardCenter
        for (int i = COLUMN; i < ROW * COLUMN; i++) {
            if (i % SIZE != COLUMN - 1 && i % SIZE != COLUMN) {
                middle ^= 1l << i;
            }
        }
    }
}
