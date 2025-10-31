package draughts;

import static draughts.Draughts10x10.ARROW;
import static draughts.Draughts10x10.BLACK;
import static draughts.Draughts10x10.GAME_OVER;
import static draughts.Draughts10x10.TILEBOARD;
import static draughts.Draughts10x10.WHITE;
import static draughts.HintBoard.NONE;
import static draughts.PieceBoard.EMPTY;
import static draughts.PieceBoard.PIECE;
import static draughts.PieceBoard.WB;
import static draughts.TileBoard.GRID;
import static draughts.TileBoard.x;
import static draughts.TileBoard.y;
import java.awt.BorderLayout;
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
 * 
 * Gameloop, logic and move animation
 * 
 * enum Direction -> move in 4 directions (x, y)
 * 
 * pieceBoard -> pieces and move
 * hintBoard -> selected or moveable player pieces, mouseadapter for player move
 * 
 * -turn(color) -> 1: pieces, moves, maxCapture
 *                 2: evaluation -> gameover or do move (player (mouse) or ai (minimax))
 * -actionPerformed -> undo move
 * 
 * class BoardMove -> move animation
 * 
 * @author Naardeze
 */

final class Game extends JLayeredPane implements ActionListener {
    final static char[] PAWN = WB.toCharArray();//{'w', 'b'}
    final static char[] KING = WB.toUpperCase().toCharArray();//{'W', 'B'}

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

    final private PieceBoard pieceBoard = new PieceBoard();//pieces, move
    final private HintBoard hintBoard = new HintBoard();//pressed, moveable

    final private Stack<String> boards = new Stack();//undo
    
    final private int player;//color
    
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];
    private HashMap<Integer, ArrayList<Integer>[]> moves;
    private int maxCapture;
    
    Game(int player) {
        this.player = player;

        ARROW.setEnabled(false);
        GAME_OVER.setVisible(false);

        hintBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (int pressed = 0; pressed < TILEBOARD.tile.length; pressed++) {
                    if (TILEBOARD.tile[pressed].contains(e.getPoint())) {
                        int from = hintBoard.getFrom();

                        //1 multiple moves with same destination (press all steps of move)
                        if (from != NONE && !pieceBoard.getMove().isEmpty() && (pieceBoard.getIndex(pressed) == EMPTY || pressed == from)) {
                            ArrayList<Integer> move = new ArrayList(pieceBoard.getMove());//captured
                            int step = move.remove(move.size() - 1);

                            if (pressed != step && Math.abs(x(pressed) - x(step)) == Math.abs(y(pressed) - y(step))) {//diagonal
                                Direction direction = Direction.getDirection(step, pressed);

                                step = direction.getStep(step);

                                if (pieceBoard.getIndex(from) == KING[player]) {
                                    while (step != pressed && (pieceBoard.getIndex(step) == EMPTY || step == from)) {
                                        step = direction.getStep(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !move.contains(step)) {
                                    move.add(step);

                                    step = direction.getStep(step);

                                    if (pieceBoard.getIndex(from) == KING[player]) {
                                        while (step != pressed && (pieceBoard.getIndex(step) == EMPTY || step == from)) {
                                            step = direction.getStep(step);
                                        }
                                    }

                                    if (step == pressed) {
                                        move.add(step);
                                        
                                        if (move.indexOf(step) == maxCapture) {
                                            new Thread(new BoardMove(from, move)).start();
                                        } else {
                                            pieceBoard.setMove(move);
                                            pieceBoard.repaint();
                                        }
                                    }
                                }
                            }
                        //2 pressed = occupied
                        } else if (pieceBoard.getIndex(pressed) != EMPTY) {
                            pieceBoard.getMove().clear();

                            if (moves.containsKey(pressed)) {
                                ArrayList<Integer>[] move = moves.get(pressed);

                                if (move.length == 1) {
                                    new Thread(new BoardMove(pressed, move[0])).start();
                                } else {
                                    hintBoard.setFrom(pressed);

                                    //check: multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int to = move[i].get(maxCapture), j = 0; j < i; j++) {
                                            if (move[j].get(maxCapture) == to) {
                                                pieceBoard.getMove().add(pressed);

                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (from != NONE) {
                                hintBoard.setFrom(NONE);
                            }
                            
                            hintBoard.repaint();
                        //3 pressed = EMPTY, from != NONE
                        } else if (moves.containsKey(from)) {
                            for (ArrayList<Integer> move : moves.get(from)) {
                                if (move.get(maxCapture) == pressed) {
                                    new Thread(new BoardMove(from, move)).start();
                                }
                            }
                        }                        

                        break;
                    }
                }
            }
        });
         
        add(pieceBoard, new Integer(1));
        add(hintBoard);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pieceBoard.setSize(getSize());
                hintBoard.setSize(getSize());
   
                //prevent: player = black and animation starts before boards are sized (don't look nice)
                turn(WHITE);//WHITE begins
            }
        });
    }
    
    //1 pieces, moves, maxCapture
    //2 evaluation
    private void turn(int color) {
        //1 pieces, moves, maxCapture
        char[] board = pieceBoard.getBoard().toCharArray();

        pieces[WHITE] = new HashSet();
        pieces[BLACK] = new HashSet();

        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                pieces[WB.indexOf(Character.toLowerCase(board[i]))].add(i);
            }
        }
        
        moves = new HashMap();
        maxCapture = 0;
        
        int opponent = 1 - color;

        for (int from : pieces[color]) {
            char piece = board[from];
            HashSet<ArrayList<Integer>> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;
            
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//2x2
                for (Direction vertical : horizontal) {//[WB]
                    if (vertical.canStep(from)) {
                        int step = vertical.getStep(from);
                        
                        //empty
                        if(board[step] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                            if (maxCapturePiece == 0) {//legal move
                                movesPiece.add(new ArrayList(Arrays.asList(new Integer[] {step})));//<step>
                            }

                            if (piece == KING[color] && vertical.canStep(step)) {
                                do {
                                    step = vertical.getStep(step);

                                    if (maxCapturePiece == 0 && board[step] == EMPTY) {//legal move
                                        movesPiece.add(new ArrayList(Arrays.asList(new Integer[] {step})));//<step>
                                    }
                                } while (board[step] == EMPTY && vertical.canStep(step));
                            }
                        }

                        //capture
                        if (pieces[opponent].contains(step) && vertical.canStep(step)) {
                            int capture = step;

                            step = vertical.getStep(capture);
                            
                            if (board[step] == EMPTY) {
                                ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, step}));

                                if (piece == KING[color] && vertical.canStep(step)) {
                                    do {
                                        step = vertical.getStep(step);

                                        if (board[step] == EMPTY) {
                                            captureMove.add(step);
                                        }
                                    } while (board[step] == EMPTY && vertical.canStep(step));
                                }

                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));//<<capture, step(s)>>

                                board[from] = EMPTY;

                                //check moves for extra capture(s)
                                do {
                                    ArrayList<Integer> destination = captureMoves.remove(0);//<captureMove>
                                    ArrayList<Integer> captured = new ArrayList();

                                    //opponent<->empty;
                                    do {
                                        captured.add(destination.remove(0));
                                    } while (pieces[opponent].contains(destination.get(0)));

                                    if (captured.size() > maxCapturePiece) {
                                        movesPiece.clear();                                       
                                        maxCapturePiece++;
                                    }

                                    for (int to : destination) {//empty
                                        if (captured.size() == maxCapturePiece) {//legal move
                                            ArrayList<Integer> move = new ArrayList(captured);
                                            
                                            move.add(to);
                                            movesPiece.add(move);//<<captured>, to>
                                        }

                                        for (Direction diagonal : Direction.values()) {//1x4
                                            if (diagonal.canStep(to)) {
                                                step = diagonal.getStep(to);                                                
                                                
                                                if (piece == KING[color] && !destination.contains(step)) {//no dubbels
                                                    while (board[step] == EMPTY && diagonal.canStep(step)) {
                                                        step = diagonal.getStep(step);
                                                    }
                                                }

                                                //extra capture
                                                if (pieces[opponent].contains(step) && !captured.contains(step) && diagonal.canStep(step)) {
                                                    capture = step;
                                                    step = diagonal.getStep(capture);

                                                    if (board[step] == EMPTY) {
                                                        captureMove = new ArrayList(captured);
                                                        captureMove.addAll(Arrays.asList(new Integer[] {capture, step}));

                                                        if (piece == KING[color] && diagonal.canStep(step)) {
                                                            do {
                                                                step = diagonal.getStep(step);
                                                                
                                                                if (board[step] == EMPTY) {
                                                                    captureMove.add(step);
                                                                }
                                                            } while (board[step] == EMPTY && diagonal.canStep(step));
                                                        }

                                                        captureMoves.add(captureMove);//<<captured>, capture, empty tile(s)>
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all moves checked

                                board[from] = piece;
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

                moves.put(from, movesPiece.toArray(new ArrayList[movesPiece.size()]));
            }
        }
        
        //2 evaluation
        if (TILEBOARD.isAncestorOf(this)) {//continue only if this (game) is on TILEBOARD; prevent enable ARROW by new game during LEVEL search
            if (moves.isEmpty()) {//game over
                GAME_OVER.setVisible(true);
                ARROW.setEnabled(true);
            } else if (color == player) {//player
                hintBoard.setKeySet(moves.keySet());

                if (!boards.isEmpty()) {
                   ARROW.setEnabled(true);
                }
            } else {//ai
                new Thread(){
                    @Override
                    public void run() {
                        pieceBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...

                        new BoardMove(MinMax.getAIMove(color, board, pieces, moves, maxCapture)).run();
                    }
                }.start();
            }
        }
    }
    
    //undo
    @Override
    public void actionPerformed(ActionEvent e) {
        ARROW.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        pieceBoard.getMove().clear();
        pieceBoard.setBoard(boards.pop());

        turn(player);
    }
    
    //animation: move, promotion, capture
    private class BoardMove extends Component implements Runnable {
        final static int FRAMES = 34;//frames p tile
        final static int MILLI = 4;//delay p frame
        final static int DELAY = 124;
    
        int color;        
        char piece;
        Image image;
        Point point;
        int to;
        
        BoardMove(int from, ArrayList<Integer> move) {//player
            this(player, from, move);
        }

        BoardMove(ArrayList<Integer> move) {//ai
            this(1 - player, move.remove(0), move);
        }

        BoardMove(int color, int from, ArrayList<Integer> move) {
            this.color = color; 
            
            if (color == player) {
                hintBoard.setVisible(false);
                ARROW.setEnabled(false);
                boards.push(pieceBoard.getBoard());
            } else {//ai
                pieceBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            piece = pieceBoard.getIndex(from);
            image = PIECE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            point = TILEBOARD.tile[from].getLocation();
            to = from;
  
            setSize(pieceBoard.getSize());

            pieceBoard.setMove(move);
            pieceBoard.setIndex(from, EMPTY);
            pieceBoard.add(this, BorderLayout.CENTER);
        }
        
        //animation
        @Override
        public void run() {
            //move
            Direction direction = Direction.getDirection(to, pieceBoard.getMove().get(0));

            for (int step : pieceBoard.getMove()) {//<<captured>, to>
                do {
                    to = direction.getStep(to);
                    
                    //point -> tile[to]
                    for (int horizontal = TILEBOARD.tile[to].x - point.x, vertical = TILEBOARD.tile[to].y - point.y, i = FRAMES - 1; i >= 0; i--) {
                        point.setLocation(TILEBOARD.tile[to].x - (int) (i * (float) horizontal / FRAMES), TILEBOARD.tile[to].y - (int) (i * (float) vertical / FRAMES));

                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (x(step) - x(to)) != -direction.y * (y(step) - y(to)));//x!=-y
    
                //90 degree angle
                if (to != step) {
                    direction = Direction.getDirection(to, step);
                    
                    try {
                        Thread.sleep(DELAY);
                    } catch (Exception ex) {}
                }
            }            
            
            //promotion
            if (piece == PAWN[color] && ((color == WHITE && to < GRID / 2) || (color == BLACK && to >= TILEBOARD.tile.length - GRID / 2))) {
                piece = KING[color];
            }

            pieceBoard.remove(this);
            pieceBoard.setIndex(to, piece);
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

            turn(1 - color);
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(image, point.x, point.y, null);
        }
    }
    
}





