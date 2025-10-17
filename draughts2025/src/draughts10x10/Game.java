package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import static draughts10x10.PlayerBoard.NONE;
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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
 * 
 * @author Naardeze
 */

final class Game extends JLayeredPane implements ActionListener {
    //man, king
    final static char[] MAN = WB.toCharArray();//{w, b}
    final static char[] KING = WB.toUpperCase().toCharArray();//{W, B}

    private static enum Direction {
        MIN_X_MIN_Y(-1, -1),
        PLUS_X_MIN_Y(1, -1),
        MIN_X_PLUS_Y(-1, 1),
        PLUS_X_PLUS_Y(1, 1);

        final protected int x;
        final protected int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        //can move?
        boolean canStep(int index) {
            int x = x(index) + this.x;
            int y = y(index) + this.y;

            return x >= 0 && x < GRID && y >= 0 && y < GRID;
        }

        //next square
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

    //boards
    final private PositionBoard positionBoard = new PositionBoard();
    final private PlayerBoard playerBoard = new PlayerBoard();

    //undo
    final private Stack<String> positions = new Stack();
    
    //color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];//pieces WHITE & BLACK
    private HashMap<Integer, LinkedList<Integer>[]> moves;//moves p piece
    private int maxCapture;//total captures
    
    Game(int player) {
        this.player = player;

        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //player move
        playerBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = SQUAREBOARD.square;
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int pressed = 0; pressed < square.length; pressed++) {//squares
                    if (square[pressed].contains(e.getPoint())) {//pressed square
                        int selected = playerBoard.getSelected();

                        //moves same destination (click all steps)
                        if (selected != NONE && !positionBoard.getMove().isEmpty() && (positionBoard.getIndex(pressed) == EMPTY || pressed == selected)) {
                            LinkedList<Integer> move = new LinkedList(positionBoard.getMove());//captures
                            int step = move.removeLast();

                            //x==y diagonal
                            if (pressed != step && Math.abs(x(pressed) - x(step)) == Math.abs(y(pressed) - y(step))) {
                                Direction direction = Direction.getDirection(step, pressed);

                                //first step
                                step = direction.getStep(step);

                                //king steps
                                if (positionBoard.getIndex(selected) == KING[player]) {
                                    while (step != pressed && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getStep(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !move.contains(step)) {
                                    move.addLast(step);

                                    //square after capture
                                    step = direction.getStep(step);

                                    //king steps
                                    if (positionBoard.getIndex(selected) == KING[player]) {
                                        while (step != pressed && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getStep(step);
                                        }
                                    }

                                    //pressed
                                    if (step == pressed) {
                                        move.addLast(step);
                                        
                                        if (move.indexOf(step) == maxCapture) {//boardMove
                                            new Thread(new BoardMove(selected, move)).start();
                                        } else {//continue
                                            positionBoard.setMove(move);
                                            positionBoard.repaint();
                                        }
                                    }
                                }
                            }
                        //index is occupied
                        } else if (positionBoard.getIndex(pressed) != EMPTY) {
                            positionBoard.getMove().clear();

                            //moveable
                            if (moves.containsKey(pressed)) {
                                LinkedList<Integer>[] move = moves.get(pressed);

                                if (move.length == 1) {//boardMove
                                    new Thread(new BoardMove(pressed, move[0])).start();
                                } else {//selected=index
                                    playerBoard.setSelected(pressed);

                                    //check for moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int to = move[i].getLast(), j = 0; j < i; j++) {
                                            if (move[j].getLast() == to) {
                                                positionBoard.getMove().addLast(pressed);

                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (selected != NONE) {
                                playerBoard.setSelected(NONE);
                            }
                            
                            playerBoard.repaint();
                        //selected, index is empty
                        } else if (moves.containsKey(selected)) {
                            for (LinkedList<Integer> move : moves.get(selected)) {
                                if (move.getLast() == pressed) {//boardMove
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
        add(playerBoard);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionBoard.setSize(getSize());
                playerBoard.setSize(getSize());
   
                //begin game
                //prevent: player = black and animation starts before boards are sized (doesn't look nice)
                turn(WHITE);//WHITE begins
            }
        });
    }
    
    //1 pieces, moves, maxCapture
    //2 evaluation -> game_over or get move (player (mouse) or ai (minimax))
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
        
        int opponent = 1 - color;

        for (int index : pieces[color]) {
            char piece = position[index];
            HashSet<LinkedList<Integer>> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;
            
            //2x2
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//x
                for (Direction vertical : horizontal) {//y
                    //can move vertical
                    if (vertical.canStep(index)) {
                        //first square vertical
                        int step = vertical.getStep(index);
                        
                        //empty square and king or man forward
                        if(position[step] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                            if (maxCapturePiece == 0) {//legal move
                                movesPiece.add(new LinkedList(Arrays.asList(new Integer[] {step})));
                            }

                            //king steps
                            if (piece == KING[color] && vertical.canStep(step)) {
                                do {
                                    step = vertical.getStep(step);

                                    if (maxCapturePiece == 0 && position[step] == EMPTY) {//legal move
                                        movesPiece.add(new LinkedList(Arrays.asList(new Integer[] {step})));
                                    }
                                } while (position[step] == EMPTY && vertical.canStep(step));
                            }
                        }

                        //piece opponent + square
                        if (pieces[opponent].contains(step) && vertical.canStep(step)) {
                            //capture
                            int capture = step;

                            //square after capture
                            step = vertical.getStep(capture);
                            
                            //empty square -> capture is legal
                            if (position[step] == EMPTY) {
                                //capture to check
                                LinkedList<Integer> captureMove = new LinkedList(Arrays.asList(new Integer[] {capture, step}));

                                //empty king steps
                                if (piece == KING[color] && vertical.canStep(step)) {
                                    do {
                                        step = vertical.getStep(step);

                                        if (position[step] == EMPTY) {
                                            captureMove.addLast(step);
                                        }
                                    } while (position[step] == EMPTY && vertical.canStep(step));
                                }

                                //captures to check
                                LinkedList<LinkedList<Integer>> captureMoves = new LinkedList(Arrays.asList(new LinkedList[] {captureMove}));

                                //piece off board
                                position[index] = EMPTY;

                                //check for extra captures
                                do {
                                    //captureMove
                                    LinkedList<Integer> destination = captureMoves.removeFirst();
                                    //captured pieces
                                    LinkedHashSet<Integer> captures = new LinkedHashSet();//captured pieces

                                    //separate captures from empty squares
                                    do {
                                        captures.add(destination.removeFirst());
                                    } while (pieces[opponent].contains(destination.getFirst()));

                                    //naxCapture+1
                                    if (captures.size() > maxCapturePiece) {
                                        movesPiece.clear();                                       
                                        maxCapturePiece++;
                                    }

                                    //empty square(s) after captures
                                    for (int to : destination) {
                                        if (captures.size() == maxCapturePiece) {//legal move
                                            LinkedList<Integer> move = new LinkedList(captures);
                                            
                                            move.addLast(to);
                                            movesPiece.add(move);
                                        }

                                        //1x4
                                        for (Direction diagonal : Direction.values()) {
                                            //can move diagonal
                                            if (diagonal.canStep(to)) {
                                                //first square diagonal
                                                step = diagonal.getStep(to);                                                
                                                
                                                //king steps
                                                if (piece == KING[color] && !destination.contains(step)) {
                                                    while (position[step] == EMPTY && diagonal.canStep(step)) {
                                                        step = diagonal.getStep(step);
                                                    }
                                                }

                                                //piece oppponent, not captured + square
                                                if (pieces[opponent].contains(step) && !captures.contains(step) && diagonal.canStep(step)) {
                                                    //extra capture
                                                    capture = step;
                                                    
                                                    //square after extra capture
                                                    step = diagonal.getStep(capture);

                                                    //empty square -> extra capture is legal
                                                    if (position[step] == EMPTY) {
                                                        captureMove = new LinkedList(captures);
                                                        captureMove.addAll(Arrays.asList(new Integer[] {capture, step}));

                                                        //empty king steps
                                                        if (piece == KING[color] && diagonal.canStep(step)) {
                                                            do {
                                                                step = diagonal.getStep(step);
                                                                
                                                                if (position[step] == EMPTY) {
                                                                    captureMove.addLast(step);
                                                                }
                                                            } while (position[step] == EMPTY && diagonal.canStep(step));
                                                        }

                                                        //extra captureMove to check
                                                        captureMoves.addLast(captureMove);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all capture moves checked

                                //piece back on board
                                position[index] = piece;
                            }
                        }
                    }
                }
            }
            
            //piece is moveable
            if (!movesPiece.isEmpty()) {
                //maxCapture++
                if (maxCapturePiece > maxCapture) {
                    moves.clear();
                    maxCapture = maxCapturePiece;
                }

                moves.put(index, movesPiece.toArray(new LinkedList[movesPiece.size()]));
            }
        }
        
        //2 evaluation
        if (SQUAREBOARD.isAncestorOf(this)) {//continue only if game (this) is on SQUAREBOARD
            //game over
            if (moves.isEmpty()) {
                GAME_OVER.setVisible(true);
            //player (mouse)
            } else if (color == player) {
                playerBoard.setBoard(moves.keySet());
            //ai (minimax)
            } else {
                new Thread(){
                    @Override
                    public void run() {
                        positionBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...

                        //boardMove
                        new BoardMove(MinMax.getAIMove(color, position, pieces, moves, AI.getValue())).run();
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
        //quit turn
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            playerBoard.setVisible(false);
        }
        
        //previous position
        positionBoard.setPosition(positions.pop());
        positionBoard.repaint();

        //player turn
        turn(player);
    }
   
    //animation constants
    final private static int FRAMES = 31;//frames p square
    final private static int MILLI = 4;//delay p frame
    final private static int DELAY = 124;//delay in animation
    
    //animation: move, promotion, captures
    private class BoardMove extends Component implements Runnable {
        int color;        
        int index;
        char piece;
        Image image;
        Point point;
        
        BoardMove(int index, LinkedList<Integer> move) {//ai
            this(player, index, move);
        }
        BoardMove(LinkedList<Integer> move) {//ai
            this(1 - player, move.removeFirst(), move);
        }
        
        BoardMove(int color, int index, LinkedList<Integer> move) {
            //finish turn
            if (color == player) {
                playerBoard.setVisible(false);
                UNDO.setEnabled(false);
                positions.push(positionBoard.getPosition());
            } else {//ai
                positionBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
            }

            this.color = color;            
            this.index = index;
            
            piece = positionBoard.getIndex(index);
            image = PIECE[(MAN[color] + "" + KING[color]).indexOf(piece)][color];
            point = SQUAREBOARD.square[index].getLocation();
  
            setSize(positionBoard.getSize());

            //piece off board
            positionBoard.setMove(move);
            positionBoard.setIndex(index, EMPTY);
            positionBoard.add(this);
        }
        
        //animation
        @Override
        public void run() {
            //move
            Direction direction = Direction.getDirection(index, positionBoard.getMove().getFirst());

            for (int step : positionBoard.getMove()) {//<<captures>, to
                do {
                    //square to move to
                    index = direction.getStep(index);
                    
                    //point->square[to]
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
            if (piece == MAN[color] && ((color == WHITE && index < GRID / 2) || (color == BLACK && index >= SQUAREBOARD.square.length - GRID / 2))) {
                piece = KING[color];
            }

            //piece back on board
            positionBoard.remove(this);
            positionBoard.setIndex(index, piece);
            positionBoard.repaint();
            
            try {
                Thread.sleep(DELAY);
            } catch (Exception ex) {}
            
            //capture
            for (int i = 0; i < maxCapture; i++) {
                positionBoard.setIndex(positionBoard.getMove().removeFirst(), EMPTY);
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