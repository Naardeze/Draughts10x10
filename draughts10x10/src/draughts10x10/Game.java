package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import static draughts10x10.HintBoard.NONE;
import static draughts10x10.PieceBoard.B;
import static draughts10x10.PieceBoard.B_KING;
import static draughts10x10.PieceBoard.EMPTY;
import static draughts10x10.PieceBoard.IMAGE;
import static draughts10x10.PieceBoard.W;
import static draughts10x10.PieceBoard.WB;
import static draughts10x10.PieceBoard.W_KING;
import static draughts10x10.SquareBoard.SIZE;
import static draughts10x10.SquareBoard.x;
import static draughts10x10.SquareBoard.y;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import javax.swing.JLayeredPane;

/**
 * game loop, logic, etc
 * 
 * turn(color) -> 1 get state (position, moves, maxCapture)
 *                2 evaluation
 *                
 * undo player move (player has turn or game over)
 * 
 * BoardMove -> move animation
 * 
 * @author Naardeze
 */

final class Game extends JLayeredPane implements ActionListener {
    //pawn, king
    final static char[] PAWN = {W, B};//w b
    final static char[] KING = {W_KING, B_KING};//W B

    //animation constants
    final private static int FRAMES = 25;//frames p square
    final private static int MILLI = 6;//milliseconds p frame
    final private static int DELAY = 140;//milliseconds delay time
    
    private static enum Direction {
        MIN_X_MIN_Y(-1, -1),
        PLUS_X_MIN_Y(1, -1),
        MIN_X_PLUS_Y(-1, 1),
        PLUS_X_PLUS_Y(1, 1);

        final int x;
        final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean hasNext(int index) {//can move?
            int x = x(index) + this.x;
            int y = y(index) + this.y;

            return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
        }

        int getNext(int index) {//to
            return (x(index) + x) / 2 + (y(index) + y) * (SIZE / 2);
        }

        static Direction getDirection(int from, int to) {//from -> to
            if (x(from) > x(to)) {//-x
                if (from > to) {//-y
                    return MIN_X_MIN_Y;
                } else {//+y
                    return MIN_X_PLUS_Y;
                }
            } else {//+x
                if (from > to) {//-y
                    return PLUS_X_MIN_Y;
                } else {//+y
                    return PLUS_X_PLUS_Y;
                }
            }
        }
    }
    
    //boards
    final private PieceBoard pieceBoard = new PieceBoard(SQUAREBOARD.getSquares());
    final private HintBoard hintBoard = new HintBoard(SQUAREBOARD.getSquares());

    ///player boards
    final private Stack<String> boards = new Stack();
    
    //player color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] position = new HashSet[WB.length()];
    private HashMap<Integer, Move[]> moves;
    private int maxCapture;
    
    Game(int player) {
        this.player = player;
        
        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //player move
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = SQUAREBOARD.getSquares();
            
            @Override
            public void mousePressed(MouseEvent e) {
                //loop squares
                for (int index = 0; index < square.length; index++) {
                    //pressed square
                    if (square[index].contains(e.getPoint())) {
                        //selected square
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination
                        if (selected != NONE && !pieceBoard.getMove().isEmpty() && (pieceBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(pieceBoard.getMove());
                            int next = captures.remove(captures.size() - 1);
                            
                            //x==y
                            if (index != next && Math.abs(x(index) - x(next)) == Math.abs(y(index) - y(next))) {
                                Direction direction = Direction.getDirection(next, index);
                             
                                //pawn step
                                next = direction.getNext(next);
                                
                                //king line
                                if (pieceBoard.getIndex(selected) == KING[player]) {
                                    while (next != index && (pieceBoard.getIndex(next) == EMPTY || next == selected)) {
                                        next = direction.getNext(next);
                                    }
                                }

                                //capture
                                if (position[1 - player].contains(next) && !captures.contains(next)) {
                                    captures.add(next);
                                    //pawn step
                                    next = direction.getNext(next);
                                    
                                    //king line
                                    if (pieceBoard.getIndex(selected) == KING[player]) {
                                        while (next != index && (pieceBoard.getIndex(next) == EMPTY || next == selected)) {
                                            next = direction.getNext(next);
                                        }
                                    }
                                    
                                    //legal index
                                    if (next == index) {
                                        if (captures.size() == maxCapture) {//move
                                            new Thread(new BoardMove(player, new Move(captures, index).getBoardMove(selected))).start();
                                        } else {//continue
                                            captures.add(index);
                                            pieceBoard.setMove(captures);

                                            repaint();
                                        }
                                    }
                                }
                            }
                        //index = occupied
                        } else if (pieceBoard.getIndex(index) != EMPTY) {
                            pieceBoard.getMove().clear();
                            
                            //moveable piece
                            if (moves.containsKey(index)) {
                                Move[] move = moves.get(index);
                                
                                if (move.length == 1) {//1 move
                                    new Thread(new BoardMove(player, move[0].getBoardMove(index))).start();
                                } else {//set selected (index)
                                    hintBoard.setSelected(index);
                        
                                    //check for multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int j = 0; j < i; j++) {
                                            if (move[j].getTo() == move[i].getTo()) {
                                                pieceBoard.getMove().add(index);
                                                
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (selected != NONE) {
                                hintBoard.setSelected(NONE);
                            }
                        
                            repaint();
                        //selected, index = empty
                        } else if (moves.containsKey(selected)) {
                            //loop moves selected
                            for (Move move : moves.get(selected)) {
                                if (move.getTo() == index) {
                                    new Thread(new BoardMove(player, move.getBoardMove(selected))).start();
                                }
                            }
                        }                        
                        
                        break;
                    }
                }
            }
        });
        
        add(hintBoard);
        add(pieceBoard, new Integer(1));
        
        //size boards
        //first turn
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                hintBoard.setSize(getSize());
                pieceBoard.setSize(getSize());
                
                //prevent: player = black & AI = 1 (or 2) and boardmove starts before boards are sized (doesn't look nice)
                turn(WHITE);//WHITE begins game
            }
        });
    }
    
    //turn of color
    //1 get state (position, moves and maxCapture)
    //2 evaluation
    private void turn(int color) {
        //1 get state (position, moves and maxCapture)
        char[] board = pieceBoard.getBoard().toCharArray();

        //get positions
        position[WHITE] = new HashSet();
        position[BLACK] = new HashSet();
        
        //loop board
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                position[WB.indexOf(Character.toLowerCase(board[i]))].add(i);
            }
        }
        
        //get moves and maxCapture
        moves = new HashMap();
        maxCapture = 0;
        
        //pieces
        for (int index : position[color]) {
            char piece = board[index];
            HashSet<Move> pieceMoves = new HashSet();

            //horizontal x ertical
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//x -+
                for (Direction vertical : horizontal) {//y -+
                    //can step
                    if (vertical.hasNext(index)) {
                        //pawn step
                        int next = vertical.getNext(index);

                        //empty square
                        if(board[next] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                            //legal move
                            if (maxCapture == 0) {
                                pieceMoves.add(new Move(next));
                            }

                            //king steps
                            if (piece == KING[color] && vertical.hasNext(next)) {
                                do {
                                    next = vertical.getNext(next);

                                    //legal move
                                    if (board[next] == EMPTY && maxCapture == 0) {
                                        pieceMoves.add(new Move(next));
                                    }
                                } while (board[next] == EMPTY && vertical.hasNext(next));
                            }
                        }

                        //legal capture
                        if (position[1 - color].contains(next) && vertical.hasNext(next) && board[vertical.getNext(next)] == EMPTY) {
                            //capture
                            int capture = next;

                            //pawn step (empty)
                            next = vertical.getNext(capture);

                            //capturemove to check for extra captures
                            ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, next}));

                            //king steps
                            if (piece == KING[color] && vertical.hasNext(next)) {
                                do {
                                    next = vertical.getNext(next);

                                    if (board[next] == EMPTY) {
                                        captureMove.add(next);
                                    }
                                } while (board[next] == EMPTY && vertical.hasNext(next));
                            }

                            //capturemoves to check for extra captures
                            ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));

                            //piece off board
                            board[index] = EMPTY;

                            //check for extra captures
                            do {
                                ArrayList<Integer> move = captureMoves.remove(0);
                                ArrayList<Integer> captures = new ArrayList();

                                //captures <-> empty
                                do {
                                    captures.add(move.remove(0));
                                } while (position[1 - color].contains(move.get(0)));

                                //maxCapture
                                if (captures.size() > maxCapture) {
                                    pieceMoves.clear();
                                    moves.clear();

                                    maxCapture++;
                                }

                                //empty square(s)
                                for (int to : move) {
                                    //legal move
                                    if (captures.size() == maxCapture) {
                                        pieceMoves.add(new Move(captures, to));
                                    }
                                    
                                    //4 directions
                                    for (Direction diagonal : Direction.values()) {
                                        //can step
                                        if (diagonal.hasNext(to)) {
                                            //pawn step
                                            next = diagonal.getNext(to);                                                

                                            //king steps
                                            if (piece == KING[color] && !move.contains(next)) {
                                                while (board[next] == EMPTY && diagonal.hasNext(next)) {
                                                    next = diagonal.getNext(next);
                                                }
                                            }

                                            //legal capture
                                            if (position[1 - color].contains(next) && !captures.contains(next) && diagonal.hasNext(next) && board[diagonal.getNext(next)] == EMPTY) {
                                                //capture
                                                capture = next;
                                                //pawn step (empty)
                                                next = diagonal.getNext(capture);

                                                //capturemove
                                                captureMove = new ArrayList(captures);
                                                captureMove.addAll(Arrays.asList(new Integer[] {capture, next}));
                                                
                                                //king steps
                                                if (piece == KING[color] && diagonal.hasNext(next)) {
                                                    do {
                                                        next = diagonal.getNext(next);

                                                        if (board[next] == EMPTY) {
                                                            captureMove.add(next);
                                                        }
                                                    } while (board[next] == EMPTY && diagonal.hasNext(next));
                                                }

                                                //capturemove -> capturemoves
                                                captureMoves.add(captureMove);
                                            }
                                        }
                                    }
                                }
                            } while (!captureMoves.isEmpty());

                            //piece on board
                            board[index] = piece;
                        }
                    }
                }
            }
            
            //piece is moveable
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
            }
        }

        //2 evaluation
        //continue only if this (game) is on SQUAREBOARD
        if (SQUAREBOARD.isAncestorOf(this)) {
            //gameover
            if (moves.isEmpty()) {
                GAME_OVER.setVisible(true);
            //player move
            } else if (color == player) {
                hintBoard.setHint(moves.keySet());
                hintBoard.setVisible(true);
            //ai move
            } else {
                new Thread(){
                    @Override
                    public void run() {
                        pieceBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment please

                        new BoardMove(color, MinMax.getAIMove(color, board, position, moves, AI.getValue())).run();
                    }
                }.start();
            }

            //enable undo
            if (moves.isEmpty() || (color == player && !boards.isEmpty())) {
                UNDO.setEnabled(true);
            }
        }
    }
    
    //undo player move -> gameover or player has turn
    @Override
    public void actionPerformed(ActionEvent e) {
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        //set previous board
        pieceBoard.getMove().clear();
        pieceBoard.setBoard(boards.pop());
        pieceBoard.repaint();

        //player turn
        turn(player);
    }
    
    //animation, promotion, captures
    private class BoardMove extends AbstractBoard implements Runnable {
        int color;
        int index;
        char piece;
        Image image;
        Point location;
        
        BoardMove(int color, ArrayList<Integer> move) {
            super(SQUAREBOARD.getSquares());
            
            this.color = color;
            
            if (color == player) {//player
                UNDO.setEnabled(false);
                hintBoard.setVisible(false);
                boards.push(pieceBoard.getBoard());
            } else {//ai
                pieceBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            index = move.remove(0);
            piece = pieceBoard.getIndex(index);
            image = IMAGE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            location = square[index].getLocation();
            
            pieceBoard.setMove(move);
            pieceBoard.setIndex(index, EMPTY);
            pieceBoard.add(this, BorderLayout.CENTER);
            pieceBoard.validate();
        }
        
        @Override
        public void run() {
            //animation 
            Direction direction = Direction.getDirection(index, pieceBoard.getMove().get(0));

            //loop move
            for (int step : pieceBoard.getMove()) {//<<captures>, destination>
                do {
                    //square to move to
                    index = direction.getNext(index);
      
                    //location -> square[index]
                    //loop frames + -> -
                    for (int horizontal = square[index].x - location.x, vertical = square[index].y - location.y, i = FRAMES - 1; i >= 0; i--) {
                        location.setLocation(square[index].x - (int) (i * (float) horizontal / FRAMES), square[index].y - (int) (i * (float) vertical / FRAMES));
                        
                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (x(step) - x(index)) != -direction.y * (y(step) - y(index)));//x!=-y -> step=index (0=-0) or 90 degree (x=-y)
    
                //90 degree
                if (index != step) {
                    direction = Direction.getDirection(index, step);
                }
            }            
            
            //promotion
            if (piece == W && index < SIZE / 2) {
                piece = W_KING;
            } else if (piece == B && index >= square.length - SIZE / 2) {
                piece = B_KING;
            }
            
            pieceBoard.remove(this);
            pieceBoard.setIndex(index, piece);
            pieceBoard.repaint();
            
            try {
                Thread.sleep(DELAY);
            } catch (Exception ex) {}
            
            //captures
            for (int i = 0; i < maxCapture; i++) {
                pieceBoard.setIndex(pieceBoard.getMove().remove(0), EMPTY);
                pieceBoard.repaint();
                        
                try {
                    Thread.sleep(DELAY);
                } catch (Exception ex) {}
            }

            //turn opponent of color
            turn(1 - color);
        }
        
        @Override
        public void paint(Graphics g) {
            paintPiece(g, image, location);
        }
    }

}
