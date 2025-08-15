package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import draughts10x10.ai.MinMax;
import java.awt.Cursor;
import java.awt.Graphics;
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
import java.util.Stack;
import javax.swing.JLayeredPane;
import draughts10x10.board.AbstractBoard;
import draughts10x10.board.HintBoard;
import static draughts10x10.board.HintBoard.NONE;
import draughts10x10.board.PieceBoard;
import static draughts10x10.board.PieceBoard.EMPTY;
import static draughts10x10.board.SquareBoard.x;
import static draughts10x10.board.SquareBoard.y;
import static draughts10x10.board.PieceBoard.W;
import static draughts10x10.board.PieceBoard.B;
import static draughts10x10.board.PieceBoard.W_KING;
import static draughts10x10.board.PieceBoard.B_KING;
import static draughts10x10.board.PieceBoard.WB;
import static draughts10x10.board.SquareBoard.SIZE;
import java.awt.Image;
import java.awt.Point;
import java.util.HashSet;
import static draughts10x10.board.PieceBoard.IMAGE;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import java.awt.BorderLayout;

/**
 * game loop, logic, io and move animation
 * 
 * undo player move
 * 
 * @author Naardeze
 */

public class Game extends JLayeredPane implements ActionListener {
    //pawn, king pieces
    final public static char[] PAWN = {W, B};//w b
    final public static char[] KING = {W_KING, B_KING};//W B

    //used in animation
    final private static int FRAMES = 25;//steps p square
    final private static int MILLI = 6;//milliseconds p step
    final private static int DELAY = 140;//milliseconds delay time
    
    //player color        
    final private int player;
    
    //boards
    final private PieceBoard pieceBoard = new PieceBoard(SQUAREBOARD.getSquares());
    final private HintBoard hintBoard = new HintBoard(SQUAREBOARD.getSquares());

    //player boards -> undo
    final private Stack<String> boards = new Stack();

    //used in turn
    private int color;
    private HashSet<Integer>[] position = new HashSet[WB.length()];
    private HashMap<Integer, Move[]> moves;
    private int maxCapture;
    
    Game(int player) {
        this.player = player;
        
        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //mouse move (player)
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = SQUAREBOARD.getSquares();
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < square.length; index++) {
                    if (square[index].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination
                        if (selected != NONE && !pieceBoard.getMove().isEmpty() && (pieceBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(pieceBoard.getMove());
                            int next = captures.remove(captures.size() - 1);
                            
                            //correct diagonal
                            if (index != next && Math.abs(x(index) - x(next)) == Math.abs(y(index) - y(next))) {
                                Direction direction = Direction.getDirection(next, index);
                                
                                next = direction.getNext(next);
                                
                                //king steps
                                if (pieceBoard.getIndex(selected) == KING[color]) {
                                    while (next != index && (pieceBoard.getIndex(next) == EMPTY || next == selected)) {
                                        next = direction.getNext(next);
                                    }
                                }

                                //capture    
                                if (position[1 - color].contains(next) && !captures.contains(next)) {
                                    captures.add(next);
                                    next = direction.getNext(next);
                                    
                                    //king steps
                                    if (pieceBoard.getIndex(selected) == KING[color]) {
                                        while (next != index && (pieceBoard.getIndex(next) == EMPTY || next == selected)) {
                                            next = direction.getNext(next);
                                        }
                                    }
                                    
                                    if (next == index) {
                                        if (captures.size() == maxCapture) {//boardMove
                                            new Thread(new BoardMove(new Move(captures, index).getBoardMove(selected))).start();
                                        } else {//extra capture
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
                            
                            if (moves.containsKey(index)) {
                                Move[] move = moves.get(index);
                                
                                if (move.length == 1) {//1 move (no options) -> boardmove
                                    new Thread(new BoardMove(move[0].getBoardMove(index))).start();
                                } else {//2+ -> selected (index)
                                    hintBoard.setSelected(index);
                        
                                    //check for multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int j = 0; j < i; j++) {
                                            if (move[j].getDestination() == move[i].getDestination()) {
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
                            for (Move move : moves.get(selected)) {
                                if (move.getDestination() == index) {
                                    new Thread(new BoardMove(move.getBoardMove(selected))).start();
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
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                hintBoard.setSize(getSize());
                pieceBoard.setSize(getSize());
                
                //first turn when all boards are sized
                //prevent: player = black & AI = 1 (or 2) and boardmove starts before boards are sized (doesn't look nice)
                turn(WHITE);//WHITE begins game
            }
        });
    }
    
    //turn of color
    private void turn(int color) {
        char[] board = pieceBoard.getBoard().toCharArray();
        
        position[WHITE] = new HashSet();
        position[BLACK] = new HashSet();
        
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                position[WB.indexOf(Character.toLowerCase(board[i]))].add(i);
            }
        }
        
        moves = new HashMap();
        maxCapture = 0;
        
        for (int index : position[color]) {
            char piece = board[index];
            HashSet<Move> pieceMoves = new HashSet();

            //horizontal x vertical
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//-x -> +x
                for (Direction vertical : horizontal) {//-y -> +y
                    if (vertical.hasNext(index)) {
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

                        //capture
                        if (position[1 - color].contains(next) && vertical.hasNext(next) && board[vertical.getNext(next)] == EMPTY) {
                            int capture = next;

                            next = vertical.getNext(capture);

                            //move to check for extra captures
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

                            //moves to check for extra captures
                            ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));

                            //piece off board
                            board[index] = EMPTY;

                            //check for extra captures
                            do {
                                ArrayList<Integer> move = captureMoves.remove(0);
                                ArrayList<Integer> captures = new ArrayList();

                                //separate captures from empty
                                do {
                                    captures.add(move.remove(0));
                                } while (position[1 - color].contains(move.get(0)));

                                //maxCapture
                                if (captures.size() > maxCapture) {
                                    pieceMoves.clear();
                                    moves.clear();

                                    maxCapture++;
                                }

                                for (int to : move) {//empty square(s)
                                    //legal move
                                    if (captures.size() == maxCapture) {
                                        pieceMoves.add(new Move(captures, to));
                                    }

                                    //check all directions
                                    for (Direction diagonal : Direction.values()) {
                                        if (diagonal.hasNext(to)) {
                                            next = diagonal.getNext(to);                                                

                                            //king steps
                                            if (piece == KING[color] && !move.contains(next)) {
                                                while (board[next] == EMPTY && diagonal.hasNext(next)) {
                                                    next = diagonal.getNext(next);
                                                }
                                            }

                                            //extra capture
                                            if (position[1 - color].contains(next) && !captures.contains(next) && diagonal.hasNext(next) && board[diagonal.getNext(next)] == EMPTY) {
                                                capture = next;
                                                next = diagonal.getNext(capture);

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

            //moveable piece
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
            }
        }
        
        this.color = color;

        //gameover
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(true);
        //player move (mouse)
        } else if (color == player) {
            hintBoard.setHint(moves.keySet());
            hintBoard.setVisible(true);
        //computer move (AI)
        } else {
            new Thread(){
                @Override
                public void run() {
                    //wait please
                    pieceBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            
                    new BoardMove(MinMax.getAIMove(color, position, board, moves, AI.getValue())).run();
                }
            }.start();
        }
        
        if (moves.isEmpty() || color == player) {
            UNDO.setEnabled(!boards.isEmpty());
        }
    }
    
    //undo player move
    @Override
    public void actionPerformed(ActionEvent e) {
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        pieceBoard.getMove().clear();
        pieceBoard.setBoard(boards.pop());
        pieceBoard.repaint();

        //player turn
        turn(player);
    }
    
    //move: animation, promotion, captures
    private class BoardMove extends AbstractBoard implements Runnable {
        int index;
        char piece;
        Image image;
        Point location;
        
        BoardMove(ArrayList<Integer> move) {
            super(SQUAREBOARD.getSquares());
            
            if (player == color) {
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

            //loop through move
            for (int step : pieceBoard.getMove()) {//<<captures>, destination>
                do {
                    index = direction.getNext(index);
      
                    //location -> square[index]
                    for (int horizontal = square[index].x - location.x, vertical = square[index].y - location.y, i = FRAMES - 1; i >= 0; i--) {
                        location.setLocation(square[index].x - (int) (i * (double) horizontal / FRAMES), square[index].y - (int) (i * (double) vertical / FRAMES));
                        
                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                //index->step != step->index
                } while (direction.x * (x(step) - x(index)) != direction.y * (y(index) - y(step)));//step=index (0=-0) or 90 degree (x=-y)
    
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
            
            //capture
            for (int i = 0; i < maxCapture; i++) {
                pieceBoard.setIndex(pieceBoard.getMove().remove(0), EMPTY);
                pieceBoard.repaint();
                        
                try {
                    Thread.sleep(DELAY);
                } catch (Exception ex) {}
            }

            //turn opponent
            turn(1 - color);
        }
        
        @Override
        public void paint(Graphics g) {
            g.drawImage(image, location.x, location.y, null);
        }
    }
    
}
