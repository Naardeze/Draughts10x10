package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.IMAGE;
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
import draughts10x10.board.PositionBoard;
import static draughts10x10.board.PositionBoard.EMPTY;
import draughts10x10.board.SquareBoard;
import static draughts10x10.board.SquareBoard.x;
import static draughts10x10.board.SquareBoard.y;
import static draughts10x10.board.PositionBoard.W;
import static draughts10x10.board.PositionBoard.B;
import static draughts10x10.board.PositionBoard.W_KING;
import static draughts10x10.board.PositionBoard.B_KING;
import static draughts10x10.board.SquareBoard.SIZE;
import java.awt.Image;
import java.awt.Point;
import java.util.HashSet;

/*

game class handles game loop, logic and io
turn (color) -> get moves -> gameover or player or AI move

move animation
undo player moves

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
    final private SquareBoard squareBoard;
    final private PositionBoard positionBoard;
    final private HintBoard hintBoard;

    //player positions -> undo
    final private Stack<String> positions = new Stack();

    //used in turn
    private int color;
    private HashMap<Integer, Move[]> moves;
    private int maxCapture;
    
    Game(int player) {
        this.player = player;
        
        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        squareBoard = new SquareBoard(player);
        positionBoard = new PositionBoard(squareBoard.getSquares());
        hintBoard = new HintBoard(squareBoard.getSquares());

        //mouse move (player)
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = squareBoard.getSquares();
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < square.length; index++) {
                    if (square[index].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination
                        if (selected != NONE && !positionBoard.getMove().isEmpty() && (positionBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(positionBoard.getMove());
                            int next = captures.remove(captures.size() - 1);
                            
                            //correct diagonal
                            if (index != next && Math.abs(x(index) - x(next)) == Math.abs(y(index) - y(next))) {
                                Direction direction = Direction.getDirection(next, index);
                                
                                next = direction.getNext(next);
                                
                                //king steps
                                if (positionBoard.getIndex(selected) == KING[player]) {
                                    while (next != index && (positionBoard.getIndex(next) == EMPTY || next == selected)) {
                                        next = direction.getNext(next);
                                    }
                                }

                                //capture    
                                if (isColor(1 - player, positionBoard.getIndex(next)) && !captures.contains(next)) {
                                    captures.add(next);
                                    next = direction.getNext(next);
                                    
                                    //king steps
                                    if (positionBoard.getIndex(selected) == KING[player]) {
                                        while (next != index && (positionBoard.getIndex(next) == EMPTY || next == selected)) {
                                            next = direction.getNext(next);
                                        }
                                    }
                                    
                                    if (next == index) {
                                        if (captures.size() == maxCapture) {//boardMove
                                            new Thread(new BoardMove(new Move(captures, index).getBoardMove(selected))).start();
                                        } else {//extra capture
                                            captures.add(index);
                                            positionBoard.setMove(captures);

                                            repaint();
                                        }
                                    }
                                }
                            }

                        //index = occupied
                        } else if (positionBoard.getIndex(index) != EMPTY) {
                            positionBoard.getMove().clear();
                            
                            if (moves.containsKey(index)) {
                                Move[] move = moves.get(index);
                                
                                if (move.length == 1) {//1 option -> move
                                    new Thread(new BoardMove(move[0].getBoardMove(index))).start();
                                } else {//selected on
                                    hintBoard.setSelected(index);
                        
                                    //check for multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int destination = move[i].getDestination(), j = 0; j < i; j++) {
                                            if (move[j].getDestination() == destination) {
                                                positionBoard.getMove().add(index);
                                                
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (selected != NONE) {
                                hintBoard.setSelected(NONE);
                            }
                        
                            repaint();
                        //selected on, index = empty
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
        
        add(squareBoard);
        add(hintBoard, new Integer(1));
        add(positionBoard, new Integer(2));
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                squareBoard.setSize(getSize());
                hintBoard.setSize(getSize());
                positionBoard.setSize(getSize());
            }
        });
        
        //white starts
        turn(WHITE);
    }
    
    //turn of color
    private void turn(int color) {
        this.color = color;

        char[] board = positionBoard.getPosition().toCharArray();
        
        moves = new HashMap();
        maxCapture = 0;

        for (int index = 0; index < board.length; index++) {
            //piece of color (turn)
            if (isColor(color, board[index])) {
                char piece = board[index];
                HashSet<Move> pieceMoves = new HashSet();

                //horizontal x vertical
                for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//-x -> +x
                    for (Direction vertical : horizontal) {//-y -> +y
                        if (vertical.hasNext(index)) {
                            int next = vertical.getNext(index);

                            //empty square
                            if(board[next] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                                if (maxCapture == 0) {
                                    pieceMoves.add(new Move(next));
                                }

                                //king steps
                                if (piece == KING[color] && vertical.hasNext(next)) {
                                    do {
                                        next = vertical.getNext(next);

                                        if (maxCapture == 0 && board[next] == EMPTY) {
                                            pieceMoves.add(new Move(next));
                                        }
                                    } while (board[next] == EMPTY && vertical.hasNext(next));
                                }
                            }

                            //capture
                            if (isColor(1 - color, board[next]) && vertical.hasNext(next) && board[vertical.getNext(next)] == EMPTY) {
                                int capture = next;

                                next = vertical.getNext(next);

                                //capture to check
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

                                //captures to check
                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));

                                //piece off board
                                board[index] = EMPTY;

                                //check for extra captures
                                do {
                                    ArrayList<Integer> move = captureMoves.remove(0);
                                    ArrayList<Integer> captures = new ArrayList();

                                    do {
                                        captures.add(move.remove(0));
                                    } while (board[move.get(0)] != EMPTY);

                                    if (captures.size() > maxCapture) {
                                        pieceMoves.clear();
                                        moves.clear();

                                        maxCapture++;
                                    }
                                    
                                    for (int to : move) {//empty squares
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
                                                if (isColor(1 - color, board[next]) && !captures.contains(next) && diagonal.hasNext(next) && board[diagonal.getNext(next)] == EMPTY) {
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

                if (!pieceMoves.isEmpty()) {
                    moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
                }
            }
        }
        
        //gameover
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(true);
        //player (mouse)
        } else if (color == player) {
            hintBoard.setHint(moves.keySet());
            hintBoard.setVisible(true);
        //computer (AI)
        } else {
            //wait please
            positionBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            

            new Thread(){
                @Override
                public void run() {
                    new BoardMove(MinMax.getAIMove(color, board, moves, AI.getValue())).run();
                }
            }.start();
        }
        
        if (moves.isEmpty() || color == player) {
            UNDO.setEnabled(!positions.isEmpty());
        }
    }
    
    //piece of color
    private static boolean isColor(int color, char piece) {
        return piece == PAWN[color] || piece == KING[color];
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
        
        positionBoard.getMove().clear();
        positionBoard.setPosition(positions.pop());
        positionBoard.repaint();
        
        turn(player);
    }
    
    //move: animation, promotion, captures
    private class BoardMove extends AbstractBoard implements Runnable {
        int index;
        char piece;
        Image image;
        Point location;
        
        BoardMove(ArrayList<Integer> move) {
            super(squareBoard.getSquares());
            
            setSize(positionBoard.getSize());
            
            if (player == color) {
                UNDO.setEnabled(false);
                hintBoard.setVisible(false);
                positions.push(positionBoard.getPosition());
            } else {
                positionBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            index = move.remove(0);
            piece = positionBoard.getIndex(index);
            image = IMAGE[(String.valueOf(PAWN[color]) + String.valueOf(KING[color])).indexOf(piece)][color];
            location = square[index].getLocation();
            
            positionBoard.setMove(move);
            positionBoard.setIndex(index, EMPTY);
            positionBoard.add(this);
        }

        @Override
        public void run() {
            //animation
            //piece direction
            Direction direction = Direction.getDirection(index, positionBoard.getMove().get(0));

            //loop through move
            for (int step : positionBoard.getMove()) {//<captures + destination>
                do {
                    index = direction.getNext(index);
      
                    //move piece (location) to square[index]
                    for (int horizontal = square[index].x - location.x, vertical = square[index].y - location.y, i = FRAMES - 1; i >= 0; i--) {
                        location.setLocation(square[index].x - (int) (i * (double) horizontal / FRAMES), square[index].y - (int) (i * (double) vertical / FRAMES));
                        
                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (x(step) - x(index)) != -direction.y * (y(step) - y(index)));
    
                //90 degree angle
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
            
            positionBoard.remove(this);
            positionBoard.setIndex(index, piece);
            positionBoard.repaint();
            
            try {
                Thread.sleep(DELAY);
            } catch (Exception ex) {}
            
            //capture
            for (int i = 0; i < maxCapture; i++) {
                positionBoard.setIndex(positionBoard.getMove().remove(0), EMPTY);
                positionBoard.repaint();
                        
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
