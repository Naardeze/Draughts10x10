package draughts10x10.ai;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import draughts10x10.Move;
import static draughts10x10.board.PieceBoard.EMPTY;
import static draughts10x10.board.SquareBoard.SIZE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * minimax with alfa beta pruning
 * 1 depth = 2 moves (1 min & 1 max)
 * 
 * depth = 0 -> captureMax = 0 -> return value
 *              captureMax != 0 -> extra search
 * uses bitboards (Diagonal)
 * 
 * @author Naardeze
 */

public class MinMax extends HashMap<String, Integer> {//<board, value>    
    final public static int COLUMN = SIZE / 2;//5 (horizontal)
    final public static int ROW = SIZE - 1;//9 (vertical)
    
    //-x-y -> -x+y & +x-y -> +x+y
    final private static Diagonal[][] HORIZONTAL = {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_ANTI_DIAGONAL}, {Diagonal.MIN_ANTI_DIAGONAL, Diagonal.PLUS_DIAGONAL}};

    //alfa beta
    final private static int ALFA = Integer.MIN_VALUE;
    final private static int BETA = Integer.MAX_VALUE;

    //middle squares -> x>0 & x<9 & y>0 & y<9
    private static long middle = 0l;
    
    //used for evaluation and pruning
    private enum Node {
        MAX {//beta
            @Override
            boolean isAlfaBeta(int value, int alfaBeta) {
                return value > alfaBeta;
            }

            @Override
            int valueOf(int value) {
                return value;
            }
        },
        MIN {//alfa
            @Override
            boolean isAlfaBeta(int value, int alfaBeta) {
                return value < alfaBeta;
            }

            @Override
            int valueOf(int value) {
                return -value;
            }
        };

        abstract boolean isAlfaBeta(int value, int alfaBeta);//>beta or <alfa
        abstract int valueOf(int value);//hasTurn-noTurn
    }
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }

    //like Game.turn (don't invent the wheel twice)
    //
    //get moves and captures
    //moves=0 -> return alfa/beta
    //depth=0 & no captures -> return value
    //pruning
    private int valueOf(char[] board, long hasTurn, long noTurn, MinMax minMax, int[] alfaBeta, int depth) {
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        //occupied -> white & black
        //empty -> !occupied
        //captureMiddle -> noTurn pieces on center squares
        //turn -> hasTurn
        for (long occupied = hasTurn ^ noTurn, empty = ~occupied, captureMiddle = noTurn & middle, turn = hasTurn; turn != 0l; turn ^= Long.lowestOneBit(turn)) {
            int index = Long.numberOfTrailingZeros(turn);
            boolean isKing = board[index] == KING[color];
            HashSet<Long> pieceMoves = new HashSet();
            
            for (Diagonal[] horizontal : HORIZONTAL) {//-x +x
                for (Diagonal vertical : horizontal) {//-y +y
                    if (vertical.hasNext(index)) {
                        long move = vertical.getNext(index);

                        //king steps
                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getKingSteps(index, occupied, move);
                        }
                        
                        //capture
                        if ((move & captureMiddle) != 0l) {
                            long capture = move & captureMiddle;
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            //legal capture
                            if ((next & empty) == next) {
                                //king steps
                                if (isKing && (next & middle) == next) {
                                    next = vertical.getKingSteps(index, occupied, next) & empty;
                                }
                                
                                //capturemoves to check for extra captures
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ next}));
                                
                                //piece off board
                                occupied ^= 1l << index;
                                empty = ~occupied;
                                
                                //look for extra captures
                                do {
                                    move = captureMoves.remove(0);
                                 
                                    long captures = move & captureMiddle;
                                    
                                    if (Long.bitCount(captures) >= maxCapture) {
                                        if (Long.bitCount(captures) > maxCapture) {
                                            pieceMoves.clear();
                                            moves.clear();
                                            
                                            maxCapture++;
                                        }
                                        
                                        //legal move
                                        pieceMoves.add(move);
                                    }
                                    
                                    if (captures != captureMiddle) {//not all captured
                                        //empty squares
                                        for (long destinations = move ^ captures; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                                            int step = Long.numberOfTrailingZeros(destinations);
                                            
                                            //check all directions
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                if (diagonal.hasNext(step)) {
                                                    next = diagonal.getNext(step);
                                                    
                                                    //king steps
                                                    if (isKing && (next & middle & empty) == next) {
                                                        next = diagonal.getKingSteps(step, occupied, next);
                                                    }

                                                    //capture
                                                    if ((next & move) == 0l && (next & captureMiddle) != 0l) {
                                                        capture = next & captureMiddle;
                                                        next = diagonal.getNext(Long.numberOfTrailingZeros(capture));

                                                        //legal capture
                                                        if ((next & empty) == next) {
                                                            //king steps
                                                            if (isKing && (next & middle) == next) {
                                                                next = diagonal.getKingSteps(step, occupied, next) & empty;
                                                            }

                                                            captureMoves.add(captures ^ capture ^ next);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());
                                
                                //piece on board
                                occupied ^= 1l << index;
                                empty = ~occupied;
                            }
                        }
                        
                        //no captures
                        if (maxCapture == 0 && (isKing || vertical == horizontal[color])) {
                            //empty squares
                            move &= empty;
                            
                            //legal move
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
        } else if (depth > 0) {//continue
            depth -= node.ordinal();
        } else if (maxCapture == 0) {//depth=0
            return node.valueOf(Long.bitCount(hasTurn) - Long.bitCount(noTurn));//move result
        }

        //pruning
        //loop through pieces
        for (int from : moves.keySet()) {
            char piece = board[from];

            //piece off board
            board[from] = EMPTY;

            //loop through moves (capture ^ destinations)
            for (long move : moves.get(from)) {
                long capture = move & noTurn;
                ArrayList<Integer> captures = new ArrayList();

                //capture -> captures
                for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                    captures.add(Long.numberOfTrailingZeros(copy));
                }

                //empty squares
                for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                    int to = Long.numberOfTrailingZeros(destinations);                    
                    String key = String.valueOf(getBoard(color, board.clone(), piece, captures, to));

                    //move result
                    int value;
                    
                    if (containsKey(key)) {
                        value = get(key);
                    } else {
                        value = minMax.valueOf(key.toCharArray(), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth);
                        
                        put(key, value);
                    }
                        
                    //alfabeta
                    if (node.isAlfaBeta(value, alfaBeta[node.ordinal()])) {
                        alfaBeta[node.ordinal()] = value;

                        //prune
                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            return value;
                        }
                    }
                }
            }

            //piece on board
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

    //AI move
    public static ArrayList<Integer> getAIMove(int ai, HashSet<Integer>[] position, char[] board, HashMap<Integer, Move[]> moves, int depth) {
        //player
        int opponent = 1 - ai;
        
        //bitboards
        long hasTurn = 0l;//ai
        long noTurn = 0l;//opponent

        //position[ai] -> hasTurn
        for (int index : position[ai]) {
            hasTurn ^= 1l << index;
        }
        
        //position[opponent] -> noTurn
        for (int index : position[opponent]) {
            noTurn ^= 1l << index;
        }

        //boardmoves
        ArrayList<ArrayList<Integer>> bestMoves = new ArrayList();
        //alfa
        int max = ALFA;

        //loop through pieces
        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;
            
            //loop through moves
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                int to = move.getDestination();                

                //bitboard of captures
                long capture = 0l;
                
                //captures -> capture
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                //beta
                int min = new MinMax(Node.MIN, opponent).valueOf(getBoard(ai, board.clone(), piece, captures, to), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, depth);
                
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
        
        //<- best move (random)
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }
    
    static {
        //middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {// 5 < 45
            if (i % SIZE != COLUMN - 1 && i % SIZE != COLUMN) {// !=4 & !=5
                middle ^= 1l << i;
            }
        }
    }
}
