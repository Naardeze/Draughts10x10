package draughts10x10;

import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import static draughts10x10.HintBoard.NOT_SELECTED;
import static draughts10x10.PositionBoard.EMPTY;
import static draughts10x10.PositionBoard.PIECE;
import static draughts10x10.PositionBoard.WB;
import static draughts10x10.SquareBoard.GRID;
import static draughts10x10.SquareBoard.x;
import static draughts10x10.SquareBoard.y;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
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
 * loop logic and animation
 * 
 * enum Direction -> move in 4 directions (x, y)
 *
 * -turn(color) -> game loop and logic
 * -actionPerformed -> undo move
 * 
 * PieceMove -> animation
 * 
 * @author Naardeze
 */

final class Game extends JLayeredPane implements ActionListener {
    //pawn, king
    final static char[] PAWN = WB.toCharArray();//{w, b}
    final static char[] KING = WB.toUpperCase().toCharArray();//{W, B}

    //boards
    final private PositionBoard positionBoard = new PositionBoard();
    final private HintBoard hintBoard = new HintBoard();

    //player positions (used for undo move)
    final private Stack<String> positions = new Stack();
    
    //player color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];//pieces WHITE & BLACK
    private HashMap<Integer, Move[]> moves;//moves p piece
    private int maxCapture;//captures move
    
    Game(int player) {
        this.player = player;

        //GUI
        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //player move
        hintBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < SQUAREBOARD.square.length; index++) {
                    if (SQUAREBOARD.square[index].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination
                        if (selected != NOT_SELECTED && !positionBoard.getMove().isEmpty() && (positionBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(positionBoard.getMove());
                            int step = captures.remove(captures.size() - 1);
                            
                            //x==y (diagonal)
                            if (index != step && Math.abs(x(index) - x(step)) == Math.abs(y(index) - y(step))) {
                                Direction direction = Direction.getDirection(step, index);
                             
                                step = direction.getStep(step);
                              
                                if (positionBoard.getIndex(selected) == KING[player]) {
                                    while (step != index && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getStep(step);
                                    }
                                }

                                if (pieces[1 - player].contains(step) && !captures.contains(step)) {
                                    captures.add(step);

                                    step = direction.getStep(step);
                                    
                                    if (positionBoard.getIndex(selected) == KING[player]) {
                                        while (step != index && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getStep(step);
                                        }
                                    }
                                    
                                    if (step == index) {
                                        //move
                                        if (captures.size() == maxCapture) {
                                            new Thread(new PieceMove(player, new Move(captures, index).getPieceMove(selected))).start();
                                        //continue
                                        } else {
                                            captures.add(step);
                                            
                                            positionBoard.setMove(captures);

                                            repaint();
                                        }
                                    }
                                }
                            }
                        //index is occupied
                        } else if (positionBoard.getIndex(index) != EMPTY) {
                            positionBoard.getMove().clear();
                            
                            if (moves.containsKey(index)) {
                                Move[] move = moves.get(index);
                                
                                if (move.length == 1) {//1 option
                                    new Thread(new PieceMove(player, move[0].getPieceMove(index))).start();
                                } else {//select
                                    hintBoard.setSelected(index);
                        
                                    //multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int to = move[i].getTo(), j = 0; j < i; j++) {
                                            if (move[j].getTo() == to) {
                                                positionBoard.getMove().add(index);
                                                
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else {
                                hintBoard.setSelected(NOT_SELECTED);
                            }
                        
                            repaint();
                        //selected, index is empty
                        } else if (moves.containsKey(selected)) {
                            for (Move move : moves.get(selected)) {
                                if (move.getTo() == index) {
                                    new Thread(new PieceMove(player, move.getPieceMove(selected))).start();
                                }
                            }
                        }                        
                        
                        break;
                    }
                }
            }
        });
        
        //positionBoard on top
        add(positionBoard, new Integer(1));
        add(hintBoard);
        
        //size boards and begin game (first turn)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                hintBoard.setSize(getSize());
                positionBoard.setSize(getSize());
   
                //2 begin game
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
            HashSet<Move> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;
            
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//-+
                for (Direction vertical : horizontal) {//[WB]
                    if (vertical.canStep(index)) {
                        int step = vertical.getStep(index);
                        
                        //empty
                        if(position[step] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                            if (maxCapturePiece == 0) {
                                movesPiece.add(new Move(step));
                            }

                            if (piece == KING[color] && vertical.canStep(step)) {
                                do {
                                    step = vertical.getStep(step);

                                    if (maxCapturePiece == 0 && position[step] == EMPTY) {
                                        movesPiece.add(new Move(step));
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
                                    ArrayList<Integer> move = captureMoves.remove(0);//<<captures>, <empty>>
                                    ArrayList<Integer> captures = new ArrayList();

                                    do {
                                        captures.add(move.remove(0));
                                    } while (pieces[1 - color].contains(move.get(0)));

                                    if (captures.size() > maxCapturePiece) {
                                        movesPiece.clear();                                       
                                        maxCapturePiece++;
                                    }

                                    for (int to : move) {//empty
                                        if (captures.size() == maxCapturePiece) {
                                            movesPiece.add(new Move(captures, to));
                                        }

                                        for (Direction diagonal : Direction.values()) {
                                            if (diagonal.canStep(to)) {
                                                step = diagonal.getStep(to);                                                
                                                
                                                if (piece == KING[color] && !move.contains(step)) {
                                                    while (position[step] == EMPTY && diagonal.canStep(step)) {
                                                        step = diagonal.getStep(step);
                                                    }
                                                }

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
            
            if (!movesPiece.isEmpty()) {
                if (maxCapturePiece > maxCapture) {
                    moves.clear();
                    maxCapture = maxCapturePiece;
                }

                moves.put(index, movesPiece.toArray(new Move[movesPiece.size()]));
            }
        }
        
        //2 evaluation
        if (SQUAREBOARD.isAncestorOf(this)) {//continue only if this (game) is on SQUAREBOARD
            if (moves.isEmpty()) {//game over
                GAME_OVER.setVisible(true);
            } else if (color == player) {//player
                hintBoard.setHint(moves.keySet());
            } else {//ai
                new Thread(){
                    @Override
                    public void run() {
                        positionBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...

                        new PieceMove(color, MinMax.getAIMove(color, position, pieces, moves)).run();
                    }
                }.start();
            }

            //UNDO
            if (moves.isEmpty() || color == player) {
                UNDO.setEnabled(!positions.isEmpty());
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
    final private static int FRAMES = 35;//frames p square
    final private static int MILLI = 4;//milliseconds p frame
    final private static int DELAY = 124;//milliseconds delay time
    
    //animation: move, promotion, captures
    private class PieceMove extends Component implements Runnable {
        int color;
        int index;
        char piece;
        Image image;
        Point point;
        
        PieceMove(int color, ArrayList<Integer> move) {
            this.color = color;
            
            if (color == player) {
                hintBoard.setVisible(false);
                UNDO.setEnabled(false);
                positions.push(positionBoard.getPosition());
            } else {
                positionBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
            }

            index = move.remove(0);
            piece = positionBoard.getIndex(index);
            image = PIECE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            point = SQUAREBOARD.square[index].getLocation();
  
            setSize(positionBoard.getSize());

            positionBoard.setMove(move);
            positionBoard.setIndex(index, EMPTY);
            positionBoard.add(this);
        }
        
        //animation
        @Override
        public void run() {
            Direction direction = Direction.getDirection(index, positionBoard.getMove().get(0));

            for (int step : positionBoard.getMove()) {
                do {
                    index = direction.getStep(index);
                    
                    //point->square[index]
                    for (int horizontal = SQUAREBOARD.square[index].x - point.x, vertical = SQUAREBOARD.square[index].y - point.y, i = FRAMES - 1; i >= 0; i--) {
                        point.setLocation(SQUAREBOARD.square[index].x - (int) (i * (float) horizontal / FRAMES), SQUAREBOARD.square[index].y - (int) (i * (float) vertical / FRAMES));

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
            if (piece == PAWN[color] && ((color == WHITE && index < GRID / 2) || (color == BLACK && index >= SQUAREBOARD.square.length - GRID / 2))) {
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
    
    private enum Direction {
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

        boolean canStep(int index) {
            int x = x(index) + this.x;
            int y = y(index) + this.y;

            return x >= 0 && x < GRID && y >= 0 && y < GRID;
        }

        int getStep(int index) {
            return (x(index) + x) / 2 + (y(index) + y) * (GRID / 2);
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

}


