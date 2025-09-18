package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import static draughts10x10.HintBoard.NOT_SELECTED;
import static draughts10x10.PositionBoard.EMPTY;
import static draughts10x10.PositionBoard.IMAGE;
import static draughts10x10.PositionBoard.WB;
import static draughts10x10.SquareBoard.SIZE;
import static draughts10x10.SquareBoard.x;
import static draughts10x10.SquareBoard.y;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
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
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import java.awt.Point;

/* Game
 *
 * -turn(color) -> 1 pieces, moves, maxCapture
 *                 2 evaluation
 * -actionPerformed: undo move
 * 
 * class PieceMove -> animation (move, promotion, capture)
 *
 * enum Direction -> move in 4 directions
*/

final class Game extends JLayeredPane implements ActionListener {
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

        boolean hasNext(int index) {
            int x = x(index) + this.x;
            int y = y(index) + this.y;

            return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
        }

        int getNext(int index) {
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

    //pawn, king
    final static char[] PAWN = WB.toCharArray();//{w, b}
    final static char[] KING = WB.toUpperCase().toCharArray();//{W, B}

    //boards
    final private PositionBoard positionBoard = new PositionBoard();
    final private HintBoard hintBoard = new HintBoard();

    //player positions (undo move)
    final private Stack<String> positions = new Stack();
    
    //player color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];//pieces WHITE and BLACK
    private HashMap<Integer, Move[]> moves;//legal moves
    private int maxCapture;//captures p move
    
    Game(int player) {
        this.player = player;

        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //player move
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = hintBoard.square;
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < square.length; index++) {
                    if (square[index].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();                        

                        //multiple moves with same destination
                        if (selected != NOT_SELECTED && !positionBoard.getMove().isEmpty() && (positionBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(positionBoard.getMove());
                            int step = captures.remove(captures.size() - 1);
                            
                            //x==y
                            if (index != step && Math.abs(x(index) - x(step)) == Math.abs(y(index) - y(step))) {
                                Direction direction = Direction.getDirection(step, index);
                             
                                step = direction.getNext(step);
                                
                                if (positionBoard.getIndex(selected) == KING[player]) {
                                    while (step != index && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getNext(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !captures.contains(step)) {
                                    captures.add(step);
                                    step = direction.getNext(step);
                                    
                                    if (positionBoard.getIndex(selected) == KING[player]) {
                                        while (step != index && (positionBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getNext(step);
                                        }
                                    }
        
                                    if (step == index) {
                                        if (captures.size() == maxCapture) {//pieceMove
                                            new Thread(new PieceMove(player, new Move(captures, index).getPieceMove(selected))).start();
                                        } else {//continue
                                            captures.add(step);
                                            positionBoard.setMove(captures);

                                            repaint();
                                        }
                                    }
                                }
                            }
                        //index = occupied
                        } else if (positionBoard.getIndex(index) != EMPTY) {
                            positionBoard.getMove().clear();
                            
                            //moveable
                            if (moves.containsKey(index)) {
                                Move[] move = moves.get(index);
                                
                                if (move.length == 1) {//1 option -> pieceMove
                                    new Thread(new PieceMove(player, move[0].getPieceMove(index))).start();
                                } else {//selected = index
                                    hintBoard.setSelected(index);
                        
                                    //check for multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int to = move[i].getTo(), j = 0; j < i; j++) {
                                            if (move[j].getTo() == to) {
                                                positionBoard.getMove().add(index);
                                                
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (selected != NOT_SELECTED) {
                                hintBoard.setSelected(NOT_SELECTED);
                            }
                        
                            repaint();
                        //selected, index = empty
                        } else if (moves.containsKey(selected)) {
                            for (Move move : moves.get(selected)) {
                                if (move.getTo() == index) {//pieceMove
                                    new Thread(new PieceMove(player, move.getPieceMove(selected))).start();
                                }
                            }
                        }                        
                        
                        break;
                    }
                }
            }
        });
        
        add(hintBoard);
        add(positionBoard, new Integer(1));
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                hintBoard.setSize(getSize());
                positionBoard.setSize(getSize());
                
                //first turn (WHITE) when boards are sized
                //prevent: player=BLACK and piecemove starts before boards are sized (doesn't look nice)
                turn(WHITE);
            }
        });
    }
    
    //1: pieces, moves and maxCapture
    //2: evaluation
    private void turn(int color) {
        //1 (pieces, moves and maxCapture)
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
            HashSet<Move> pieceMoves = new HashSet();
            int pieceMaxCapture = maxCapture;

            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//{{WB}, {WB}}
                for (Direction vertical : horizontal) {//{WB}
                    if (vertical.hasNext(index)) {
                        int next = vertical.getNext(index);

                        //empty
                        if(position[next] == EMPTY) {
                            if (pieceMaxCapture == 0 && (piece == KING[color] || vertical == horizontal[color])) {
                                pieceMoves.add(new Move(next));
                            }

                            if (piece == KING[color] && vertical.hasNext(next)) {
                                do {
                                    next = vertical.getNext(next);

                                    if (pieceMaxCapture == 0 && position[next] == EMPTY) {
                                        pieceMoves.add(new Move(next));
                                    }
                                } while (position[next] == EMPTY && vertical.hasNext(next));
                            }
                        }

                        //capture
                        if (pieces[1 - color].contains(next) && vertical.hasNext(next)) {
                            int capture = next;
                            
                            next = vertical.getNext(capture);
                          
                            if (position[next] == EMPTY) {
                                ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, next}));//<capture, next>

                                if (piece == KING[color] && vertical.hasNext(next)) {
                                    do {
                                        next = vertical.getNext(next);
                                        
                                        if (position[next] == EMPTY) {
                                            captureMove.add(next);
                                        }
                                    } while (position[next] == EMPTY && vertical.hasNext(next));
                                }

                                //captureMoves to check for extra captures
                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));//<captureMove>

                                position[index] = EMPTY;

                                //check captureMoves
                                do {
                                    ArrayList<Integer> move = captureMoves.remove(0);//<<captureMove>, <empty>>
                                    ArrayList<Integer> captures = new ArrayList();

                                    //captures <-> empty
                                    do {
                                        captures.add(move.remove(0));
                                    } while (pieces[1 - color].contains(move.get(0)));

                                    //maxCapture +1
                                    if (captures.size() > pieceMaxCapture) {
                                        pieceMoves.clear();
                                        pieceMaxCapture++;
                                    }

                                    for (int to : move) {
                                        if (captures.size() == pieceMaxCapture) {
                                            pieceMoves.add(new Move(captures, to));
                                        }

                                        for (Direction diagonal : Direction.values()) {
                                            if (diagonal.hasNext(to)) {
                                                next = diagonal.getNext(to);                                                

                                                if (piece == KING[color] && !move.contains(next)) {
                                                    while (position[next] == EMPTY && diagonal.hasNext(next)) {
                                                        next = diagonal.getNext(next);
                                                    }
                                                }

                                                //extra capture
                                                if (pieces[1 - color].contains(next) && !captures.contains(next) && diagonal.hasNext(next)) {
                                                    capture = next;
                                                    next = diagonal.getNext(capture);

                                                    if (position[next] == EMPTY) {
                                                        captureMove = new ArrayList(captures);//<captures>
                                                        captureMove.addAll(Arrays.asList(new Integer[] {capture, next}));//<capture, next>

                                                        if (piece == KING[color] && diagonal.hasNext(next)) {
                                                            do {
                                                                next = diagonal.getNext(next);
                                                                
                                                                if (position[next] == EMPTY) {
                                                                    captureMove.add(next);
                                                                }
                                                            } while (position[next] == EMPTY && diagonal.hasNext(next));
                                                        }

                                                        captureMoves.add(captureMove);
                                                    }
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
            }
            
            //moveable
            if (!pieceMoves.isEmpty()) {
                if (pieceMaxCapture > maxCapture) {
                    moves.clear();
                    maxCapture = pieceMaxCapture;
                }
                
                moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
            }
        }

        //2 (evaluation)
        if (getParent() == SQUAREBOARD) {//continue only if this (game) is on SQUAREBOARD
            if (moves.isEmpty()) {//gameover
                GAME_OVER.setVisible(true);
            } else if (color == player) {//player
                hintBoard.setSelected(NOT_SELECTED);
                hintBoard.setHint(moves.keySet());
                hintBoard.setVisible(true);
            } else {//ai
                new Thread(){
                    @Override
                    public void run() {
                        positionBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...

                        new PieceMove(color, MinMax.getAIMove(color, position, pieces, moves, AI.getValue())).run();
                    }
                }.start();
            }

            //undo button
            if (moves.isEmpty() || (color == player && !positions.isEmpty())) {
                UNDO.setEnabled(true);
            }
        }
    }
    
    //undo
    @Override
    public void actionPerformed(ActionEvent e) {
        //quit turn
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        //previous position
        positionBoard.getMove().clear();
        positionBoard.setPosition(positions.pop());
        positionBoard.repaint();

        turn(player);
    }
    
    //static animation constants
    final private static int FRAMES = 34;//frames p square
    final private static int MILLI = 4;//milliseconds p frame
    final private static int DELAY = 120;//milliseconds delay time
    
    //animation: move, promotion, capture
    private class PieceMove extends Board implements Runnable {
        int color;
        int index;
        char piece;
        Image image;
        Point point;
        
        PieceMove(int color, ArrayList<Integer> move) {
            super(positionBoard.square);
            
            this.color = color;
            
            if (color == player) {
                hintBoard.setVisible(false);
                UNDO.setEnabled(false);
                positions.push(positionBoard.getPosition());
            } else {//ai
                positionBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            index = move.remove(0);
            piece = positionBoard.getIndex(index);
            image = IMAGE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            point = square[index].getLocation();
            
            setSize(positionBoard.getSize());
            
            positionBoard.setMove(move);
            positionBoard.setIndex(index, EMPTY);
            positionBoard.add(this);
        }
        
        @Override
        public void run() {
            //move
            Direction direction = Direction.getDirection(index, positionBoard.getMove().get(0));
            
            for (int step : positionBoard.getMove()) {//<<captures>, destination>
                do {
                    index = direction.getNext(index);
                    
                    //this -> square[to]
                    for (int horizontal = square[index].x - point.x, vertical = square[index].y - point.y, i = FRAMES - 1; i >= 0; i--) {
                        point.setLocation(square[index].x - (int) (i * (float) horizontal / FRAMES), square[index].y - (int) (i * (float) vertical / FRAMES));
                        
                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (x(step) - x(index)) != -direction.y * (y(step) - y(index)));//x!=-y -> step=index (0=-0) or 90 degree angle (x=-y)
    
                //90 degree angle
                if (index != step) {
                    direction = Direction.getDirection(index, step);
                    
                    try {
                        Thread.sleep(DELAY);
                    } catch (Exception ex) {}
                }
            }            
            
            //promotion
            if (piece == PAWN[color] && ((color == WHITE && index < SIZE / 2) || (color == BLACK && index >= square.length - SIZE / 2))) {
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
