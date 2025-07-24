package draughts;

import ai.MinMax;
import board.AbstractBoard;
import board.HintBoard;
import board.PositionBoard;
import static board.PositionBoard.B;
import static board.PositionBoard.B_KING;
import static board.PositionBoard.EMPTY;
import static board.PositionBoard.W;
import static board.PositionBoard.W_KING;
import board.SquareBoard;
import static board.SquareBoard.SIZE;
import static board.SquareBoard.x;
import static board.SquareBoard.y;
import static draughts.Draughts.AI;
import static draughts.Draughts.WHITE;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

/*
    game class handles logic and io
*/

public class Game extends JLayeredPane implements ActionListener {
    //undo move button, gameover text label
    final public static JButton UNDO = new JButton("\u25c0");
    final public static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);

    //pawn, king pieces
    final public static char[] PAWN = {W, B};//w b
    final public static char[] KING = {W_KING, B_KING};//W B

    //animation constants
    final private static int FRAMES = 20;
    final private static int MILLI = 8;
    final private static int DELAY = 140;

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
    
    public Game(int player) {
        this.player = player;

        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);
        
        squareBoard = new SquareBoard(player);
        positionBoard = new PositionBoard(squareBoard.getSquares());
        hintBoard = new HintBoard(squareBoard.getSquares());

        //player move
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = squareBoard.getSquares();
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < square.length; index++) {
                    if (square[index].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination
                        if (selected != HintBoard.NONE && !positionBoard.getMove().isEmpty() && (positionBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(positionBoard.getMove());
                            int next = captures.remove(captures.size() - 1);
                            
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
                                if (isColor(1 - color, positionBoard.getIndex(next)) && !captures.contains(next)) {
                                    captures.add(next);
                                    next = direction.getNext(next);
                                    
                                    //king steps
                                    if (positionBoard.getIndex(selected) == KING[player]) {
                                        while (next != index && (positionBoard.getIndex(next) == EMPTY || next == selected)) {
                                            next = direction.getNext(next);
                                        }
                                    }
                                    
                                    if (next == index) {
                                        if (captures.size() == maxCapture) {//move
                                            new Thread(new PieceMove(new Move(captures, index).getPieceMove(selected))).start();
                                        } else {//extra capture(s)
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
                                    new Thread(new PieceMove(move[0].getPieceMove(index))).start();
                                } else {//selected on
                                    hintBoard.setSelected(index);
                        
                                    //check for multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int j = 0; j < i; j++) {
                                            if (move[i].getDestination() == move[j].getDestination()) {
                                                positionBoard.getMove().add(index);
                                                
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else {
                                hintBoard.setSelected(HintBoard.NONE);
                            }
                            
                            repaint();
                        
                        //selected on, index = empty
                        } else if (moves.containsKey(selected)) {
                            for (Move move : moves.get(selected)) {
                                if (move.getDestination() == index) {
                                    new Thread(new PieceMove(move.getPieceMove(selected))).start();
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
        //get moves
        char[] position = positionBoard.getPosition().toCharArray();
        
        moves = new HashMap();
        maxCapture = 0;
        
        for (int index = 0; index < position.length; index++) {
            if (isColor(color, position[index])) {
                char piece = position[index];
                ArrayList<Move> pieceMoves = new ArrayList();
                
                for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {
                    for (Direction vertical : horizontal) {
                        if (vertical.hasNext(index)) {
                            int next = vertical.getNext(index);
    
                            //empty square
                            if(position[next] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                                if (maxCapture == 0) {
                                    pieceMoves.add(new Move(next));
                                }
                            
                                //king steps
                                if (piece == KING[color] && vertical.hasNext(next)) {
                                    do {
                                        next = vertical.getNext(next);
                                        
                                        if (maxCapture == 0 && position[next] == EMPTY) {
                                            pieceMoves.add(new Move(next));
                                        }
                                    } while (position[next] == EMPTY && vertical.hasNext(next));
                                }
                            }
                            
                            //capture
                            if (vertical.hasNext(next) && isColor(1 - color, position[next]) && position[vertical.getNext(next)] == EMPTY) {
                                int capture = next;
                                
                                next = vertical.getNext(next);
                                
                                ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, next}));
                            
                                //king steps
                                if (piece == KING[color]) {
                                    while (vertical.hasNext(next) && position[vertical.getNext(next)] == EMPTY) {
                                        next = vertical.getNext(next);
                                        captureMove.add(next);
                                    }
                                }

                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));
                                
                                position[index] = EMPTY;
                                
                                //extra captures
                                do {
                                    ArrayList<Integer> move = captureMoves.remove(0);
                                    ArrayList<Integer> captures = new ArrayList();
                                    
                                    do {
                                        captures.add(move.remove(0));
                                    } while (isColor(1 - color, position[move.get(0)]));
                                    
                                    if (captures.size() > maxCapture) {
                                        pieceMoves.clear();
                                        moves.clear();
                                        
                                        maxCapture++;
                                    }
                                    
                                    for (int to : move) {
                                        if (captures.size() == maxCapture) {
                                            pieceMoves.add(new Move(captures, to));
                                        }
                                        
                                        for (Direction diagonal : Direction.values()) {
                                            if (diagonal.hasNext(to)) {
                                                next = diagonal.getNext(to);                                                
                                                
                                                //king steps
                                                if (piece == KING[color] && !move.contains(next)) {
                                                    while (position[next] == EMPTY && diagonal.hasNext(next)) {
                                                        next = diagonal.getNext(next);
                                                    }
                                                }
                                
                                                //extra capture
                                                if (diagonal.hasNext(next) && isColor(1 - color, position[next]) && !captures.contains(next) && position[diagonal.getNext(next)] == EMPTY) {
                                                    capture = next;
                                                    next = diagonal.getNext(capture);
                                                    
                                                    captureMove = new ArrayList(captures);
                                                    captureMove.addAll(Arrays.asList(new Integer[] {capture, next}));
                                                
                                                    //king steps
                                                    if (piece == KING[color]) {
                                                        while (diagonal.hasNext(next) && position[diagonal.getNext(next)] == EMPTY) {
                                                            next = diagonal.getNext(next);
                                                            captureMove.add(next);
                                                        }
                                                    }
                                                    
                                                    captureMoves.add(captureMove);
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());
                                
                                position[index] = piece;
                            }
                        }
                    }
                }
                
                if (!pieceMoves.isEmpty()) {
                    moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
                }
            }
        }
        
        this.color = color;
        
        if (moves.isEmpty()) {//gameover
            GAME_OVER.setVisible(true);
        } else if (color == player) {//player move
            hintBoard.setKeySet(moves.keySet());
            hintBoard.setVisible(true);
        } else {//ai move
            positionBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            
            new Thread(new PieceMove(MinMax.getMove(color, position, moves, AI.getValue()))).start();
        }
        
        if (color == player || moves.isEmpty()) {
            UNDO.setEnabled(!positions.isEmpty());
        }
    }
    
    //? piece of color
    private static boolean isColor(int color, char piece) {
        return piece == PAWN[color] || piece == KING[color];
    }
    
    //undo
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
    private class PieceMove extends AbstractBoard implements Runnable {
        int index;
        char piece;
        BufferedImage image;
        Point location;
        
        PieceMove(ArrayList<Integer> move) {
            super(squareBoard.getSquares());
            
            if (color == player) {
                UNDO.setEnabled(false);
                hintBoard.setVisible(false);
                positions.push(positionBoard.getPosition());
            } else {
                positionBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            index = move.remove(0);
            piece = positionBoard.getIndex(index);
            image = PositionBoard.IMAGE[(String.valueOf(PAWN[color]) + String.valueOf(KING[color])).indexOf(piece)][color];
            location = square[index].getLocation();
            
            setOpaque(false);
            setSize(positionBoard.getSize());
            
            positionBoard.setMove(move);
            positionBoard.setIndex(index, EMPTY);
            positionBoard.add(this);
            positionBoard.repaint();
        }

        @Override
        public void run() {
            //animation
            Direction direction = Direction.getDirection(index, positionBoard.getMove().get(0));
            
            for (int step : positionBoard.getMove()) {
                do {
                    index = direction.getNext(index);
                    
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
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.drawImage(image, location.x, location.y, square[index].width, square[index].height, null);
        }
    }
    
}
