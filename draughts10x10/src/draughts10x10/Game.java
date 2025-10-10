package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.HINT;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import draughts10x10.ai.MinMax;
import draughts10x10.board.Board;
import draughts10x10.board.HintBoard;
import static draughts10x10.board.HintBoard.NOT_SELECTED;
import draughts10x10.board.PositionBoard;
import static draughts10x10.board.PositionBoard.EMPTY;
import static draughts10x10.board.PositionBoard.PIECE;
import static draughts10x10.board.PositionBoard.WB;
import static draughts10x10.board.SquareBoard.GRID;
import static draughts10x10.board.SquareBoard.x;
import static draughts10x10.board.SquareBoard.y;
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
 * Game
 * 
 * Gameloop, logic and move animation
 * 
 * -turn(color) -> get moves -> gameover or move (player or AI)
 * -actionPerformed (undo move)
 * 
 * class BoardMove -> animation (move, promotion, capture)
 * enum Direction -> move in 4 directions (x, y)
 * 
 * @author Naardeze
 */

final public class Game extends JLayeredPane implements ActionListener {
    //man, king
    final public static char[] MAN = WB.toCharArray();//{w, b}
    final public static char[] KING = WB.toUpperCase().toCharArray();//{W, B}

    //boards
    final private PositionBoard positionBoard = new PositionBoard(SQUAREBOARD.getSquares());
    final private HintBoard hintBoard = new HintBoard(SQUAREBOARD.getSquares(), HINT.isSelected());

    //undo
    final private Stack<String> positions = new Stack();
    
    //color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];//pieces WHITE & BLACK
    private HashMap<Integer, ArrayList<Integer>[]> moves;//moves p piece
    private int maxCapture;//total captures
    
    Game(int player) {
        this.player = player;

        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //player move
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = SQUAREBOARD.getSquares();
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < square.length; index++) {//squares
                    if (square[index].contains(e.getPoint())) {//pressed square
                        int selected = hintBoard.getSelected();

                        //moves same destination
                        if (selected != NOT_SELECTED && !positionBoard.getMove().isEmpty() && (positionBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> move = new ArrayList(positionBoard.getMove());//captures
                            int step = move.remove(move.size() - 1);//piece

                            //x==y
                            if (index != step && Math.abs(x(index) - x(step)) == Math.abs(y(index) - y(step))) {
                                Direction direction = Direction.getDirection(step, index);

                                step = direction.getStep(step);

                                if (positionBoard.getIndex(selected) == KING[player]) {
                                    while (step != index && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getStep(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !move.contains(step)) {
                                    move.add(step);

                                    //empty
                                    step = direction.getStep(step);

                                    if (positionBoard.getIndex(selected) == KING[player]) {
                                        while (step != index && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getStep(step);
                                        }
                                    }

                                    //pressed
                                    if (step == index) {
                                        move.add(index);

                                        if (move.indexOf(index) == maxCapture) {//move
                                            new Thread(new BoardMove(selected, move)).start();
                                        } else {//continue
                                            positionBoard.setMove(move);
                                            positionBoard.repaint();
                                        }
                                    }
                                }
                            }
                        //index is occupied
                        } else if (positionBoard.getIndex(index) != EMPTY) {
                            positionBoard.getMove().clear();

                            //moveable
                            if (moves.containsKey(index)) {
                                ArrayList<Integer>[] move = moves.get(index);

                                if (move.length == 1) {//move
                                    new Thread(new BoardMove(index, move[0])).start();
                                } else {//selected=index
                                    hintBoard.setSelected(index);

                                    //check for moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int to = move[i].get(maxCapture), j = 0; j < i; j++) {
                                            if (move[j].get(maxCapture) == to) {
                                                positionBoard.getMove().add(index);

                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (selected != NOT_SELECTED) {
                                hintBoard.setSelected(NOT_SELECTED);
                            }
                            
                            hintBoard.repaint();
                        //selected, index is empty
                        } else if (moves.containsKey(selected)) {
                            for (ArrayList<Integer> move : moves.get(selected)) {
                                if (move.get(maxCapture) == index) {//move
                                    new Thread(new BoardMove(selected, move)).start();
                                }
                            }
                        }                        

                        break;
                    }
                }
            }
        });
        
        add(positionBoard, new Integer(1));
        add(hintBoard);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionBoard.setSize(getSize());
                hintBoard.setSize(getSize());
   
                //begin game
                //prevent: player = black and animation starts before boards are sized (doesn't look nice)
                turn(WHITE);//WHITE begins
            }
        });
    }
    
    //1 pieces, moves, maxCapture
    //2 evaluation
    private void turn(int color) {
        //1 (pieces, moves, maxCapture)
        char[] position = positionBoard.getPosition().toCharArray();

        pieces[WHITE] = new HashSet();
        pieces[BLACK] = new HashSet();

        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                pieces[WB.indexOf(Character.toLowerCase(position[i]))].add(i);
            }
        }
        
        moves = new HashMap();
        maxCapture = 0;
        
        for (int index : pieces[color]) {
            char piece = position[index];
            HashSet<ArrayList<Integer>> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;
            
            //2x2
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//-+
                for (Direction vertical : horizontal) {//[WB]
                    if (vertical.canStep(index)) {
                        int step = vertical.getStep(index);
                        
                        //empty
                        if(position[step] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                            if (maxCapturePiece == 0) {
                                movesPiece.add(new ArrayList(Arrays.asList(new Integer[] {step})));
                            }

                            if (piece == KING[color] && vertical.canStep(step)) {
                                do {
                                    step = vertical.getStep(step);

                                    if (maxCapturePiece == 0 && position[step] == EMPTY) {
                                        movesPiece.add(new ArrayList(Arrays.asList(new Integer[] {step})));
                                    }
                                } while (position[step] == EMPTY && vertical.canStep(step));
                            }
                        }

                        //capture
                        if (pieces[1 - color].contains(step) && vertical.canStep(step)) {
                            int capture = step;

                            step = vertical.getStep(capture);
                            
                            if (position[step] == EMPTY) {
                                ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, step}));

                                if (piece == KING[color] && vertical.canStep(step)) {
                                    do {
                                        step = vertical.getStep(step);

                                        if (position[step] == EMPTY) {
                                            captureMove.add(step);
                                        }
                                    } while (position[step] == EMPTY && vertical.canStep(step));
                                }

                                //captures to check
                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));

                                position[index] = EMPTY;

                                //check for extra captures
                                do {
                                    ArrayList<Integer> destination = captureMoves.remove(0);//<<captures>, <empty>>
                                    ArrayList<Integer> captures = new ArrayList();

                                    do {
                                        captures.add(destination.remove(0));
                                    } while (pieces[1 - color].contains(destination.get(0)));

                                    if (captures.size() > maxCapturePiece) {
                                        movesPiece.clear();                                       
                                        maxCapturePiece++;
                                    }

                                    //empty square(s)
                                    for (int to : destination) {
                                        //legal move
                                        if (captures.size() == maxCapturePiece) {
                                            ArrayList<Integer> move = new ArrayList(captures);
                                            
                                            move.add(to);
                                            movesPiece.add(move);
                                        }

                                        //1x4
                                        for (Direction diagonal : Direction.values()) {
                                            if (diagonal.canStep(to)) {
                                                step = diagonal.getStep(to);                                                
                                                
                                                if (piece == KING[color] && !destination.contains(step)) {
                                                    while (position[step] == EMPTY && diagonal.canStep(step)) {
                                                        step = diagonal.getStep(step);
                                                    }
                                                }

                                                //extra capture
                                                if (pieces[1 - color].contains(step) && !captures.contains(step) && diagonal.canStep(step)) {
                                                    capture = step;
                                                    step = diagonal.getStep(capture);

                                                    if (position[step] == EMPTY) {
                                                        captureMove = new ArrayList(captures);
                                                        captureMove.addAll(Arrays.asList(new Integer[] {capture, step}));

                                                        if (piece == KING[color] && diagonal.canStep(step)) {
                                                            do {
                                                                step = diagonal.getStep(step);
                                                                
                                                                if (position[step] == EMPTY) {
                                                                    captureMove.add(step);
                                                                }
                                                            } while (position[step] == EMPTY && diagonal.canStep(step));
                                                        }

                                                        captureMoves.add(captureMove);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all capture moves checked

                                position[index] = piece;
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

                moves.put(index, movesPiece.toArray(new ArrayList[movesPiece.size()]));
            }
        }
        
        //2 evaluation
        if (SQUAREBOARD.isAncestorOf(this)) {//continue only if this (game) is on SQUAREBOARD
            if (moves.isEmpty()) {//game over
                GAME_OVER.setVisible(true);
            } else if (color == player) {//player
                hintBoard.setBoard(moves.keySet());
            } else {//ai
                new Thread(){
                    @Override
                    public void run() {
                        positionBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...

                        new BoardMove(MinMax.getAIMove(color, position, pieces, moves, maxCapture, AI.getValue())).run();
                    }
                }.start();
            }

            //UNDO
            if (moves.isEmpty() || (color == player && !positions.isEmpty())) {
                UNDO.setEnabled(true);
            }
        }
    }
    
    //undo move
    @Override
    public void actionPerformed(ActionEvent e) {
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        positionBoard.setPosition(positions.pop());
        positionBoard.repaint();

        turn(player);
    }
   
    //animation constants
    final private static int FRAMES = 33;//frames p square
    final private static int MILLI = 4;//delay p frame
    final private static int DELAY = 124;//delay in animation
    
    //animation: move, promotion, captures
    private class BoardMove extends Board implements Runnable {
        int color;        
        int index;
        char piece;
        Image image;
        Point point;
        
        BoardMove(int index, ArrayList<Integer> move) {//player
            this(player, index, move);
            
            hintBoard.setVisible(false);
            UNDO.setEnabled(false);
            positions.push(positionBoard.getPosition());
        }
        
        BoardMove(ArrayList<Integer> move) {//ai
            this(1 - player, move.remove(0), move);

            positionBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
        }
        
        BoardMove(int color, int index, ArrayList<Integer> move) {//<<captures>, to>
            super(SQUAREBOARD.getSquares());

            this.color = color;            
            this.index = index;

            piece = positionBoard.getIndex(index);
            image = PIECE[(MAN[color] + "" + KING[color]).indexOf(piece)][color];
            point = square[index].getLocation();
  
            positionBoard.setMove(move);

            setSize(positionBoard.getSize());
        }
        
        //animation
        @Override
        public void run() {
            //move
            positionBoard.setIndex(index, EMPTY);
            positionBoard.add(this);
            
            Direction direction = Direction.getDirection(index, positionBoard.getMove().get(0));
            
            for (int step : positionBoard.getMove()) {//<<captures>, to>
                do {
                    index = direction.getStep(index);
                    
                    //point->square[to], frames = frames p square
                    for (int horizontal = square[index].x - point.x, vertical = square[index].y - point.y, i = FRAMES - 1; i >= 0; i--) {
                        point.setLocation(square[index].x - (int) (i * (float) horizontal / FRAMES), square[index].y - (int) (i * (float) vertical / FRAMES));

                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (x(step) - x(index)) != -direction.y * (y(step) - y(index)));//x!=-y
    
                //90 degree angle
                if (index != step) {
                    direction = Direction.getDirection(index, step);
                    
                    try {
                        Thread.sleep(DELAY);
                    } catch (Exception ex) {}
                }
            }            
            
            //promotion
            if (piece == MAN[color] && ((color == WHITE && index < GRID / 2) || (color == BLACK && index >= square.length - GRID / 2))) {
                piece = KING[color];
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
            g.drawImage(image, point.x, point.y, null);
        }
    }
    
}
