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
 * -turn(color) -> 1 get state (position, moves, maxCapture)
 *                 2 evaluation
 *                
 * -actionPerformed: undo move (turn player or game over)
 * 
 * enum Direction -> move in 4 directions
 * 
 * PieceMove -> move animation
 * 
 * @author Naardeze
 */

final class Game extends JLayeredPane implements ActionListener {
    //pawn, king
    final static char[] PAWN = {W, B};//{w, b}
    final static char[] KING = {W_KING, B_KING};//{W, B}

    //animation constants
    final private static int FRAMES = 24;//frames p square
    final private static int MILLI = 6;//milliseconds p frame
    final private static int DELAY = 140;//milliseconds delay time
    
    //boards (uses squares of SQUARTEBOARD)
    final private PieceBoard pieceBoard = new PieceBoard(SQUAREBOARD.getSquares());
    final private HintBoard hintBoard = new HintBoard(SQUAREBOARD.getSquares());

    //player positoins (undo move)
    final private Stack<String> positions = new Stack();
    
    //player color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];
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
                //squares
                for (int index = 0; index < square.length; index++) {
                    //pressed square
                    if (square[index].contains(e.getPoint())) {
                        //selected square
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination
                        if (selected != NONE && !pieceBoard.getMove().isEmpty() && (pieceBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(pieceBoard.getMove());
                            int step = captures.remove(captures.size() - 1);
                            
                            //x==y
                            if (index != step && Math.abs(x(index) - x(step)) == Math.abs(y(index) - y(step))) {
                                Direction direction = Direction.getDirection(step, index);
                             
                                //pawn step
                                step = direction.getNext(step);
                                
                                //king steps
                                if (pieceBoard.getIndex(selected) == KING[player]) {
                                    while (step != index && (pieceBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getNext(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !captures.contains(step)) {
                                    captures.add(step);
                                    //pawn step
                                    step = direction.getNext(step);
                                    
                                    //king steps
                                    if (pieceBoard.getIndex(selected) == KING[player]) {
                                        while (step != index && (pieceBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getNext(step);
                                        }
                                    }
                                    
                                    if (step == index) {
                                        if (captures.size() == maxCapture) {//move
                                            new Thread(new PieceMove(player, new Move(captures, index).getPieceMove(selected))).start();
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
                                    new Thread(new PieceMove(player, move[0].getPieceMove(index))).start();
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
                            //moves selected
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
        
        add(hintBoard);
        add(pieceBoard, new Integer(1));
        
        //size boards
        //first turn (WHITE)
        addComponentListener(new ComponentAdapter() {
            //size of SQUAREBOARD
            @Override
            public void componentResized(ComponentEvent e) {
                hintBoard.setSize(getSize());
                pieceBoard.setSize(getSize());
                
                //prevent: player = black & AI = 1 (or 2) and boardmove starts before boards are sized (doesn't look nice)
                turn(WHITE);//WHITE begins game
            }
        });
    }
    
    //turn color
    //1 get state (pieces, moves and maxCapture)
    //2 evaluation
    private void turn(int color) {
        //1 get state (pieces[2], moves and maxCapture)
        char[] board = pieceBoard.getBoard().toCharArray();

        //get pieces
        pieces[WHITE] = new HashSet();
        pieces[BLACK] = new HashSet();
        
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                pieces[WB.indexOf(Character.toLowerCase(board[i]))].add(i);
            }
        }
        
        //get moves and maxCapture
        moves = new HashMap();
        maxCapture = 0;
        
        //pieces (color)
        for (int index : pieces[color]) {
            char piece = board[index];
            HashSet<Move> pieceMoves = new HashSet();

            //2x2 directions
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//-x, +x
                for (Direction vertical : horizontal) {//-y, +y
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

                        //legal capture: piece opponent and empty square
                        if (pieces[1 - color].contains(next) && vertical.hasNext(next) && board[vertical.getNext(next)] == EMPTY) {
                            int capture = next;
                            
                            next = vertical.getNext(capture);//empty

                            //capturemove to check for extra captures
                            ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, next}));

                            //king steps
                            if (piece == KING[color]) {
                                while (vertical.hasNext(next) && board[vertical.getNext(next)] == EMPTY) {
                                    next = vertical.getNext(next);
                                    captureMove.add(next);
                                }
                            }

                            //capturemoves to check for extra captures
                            ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));

                            //piece off board
                            board[index] = EMPTY;

                            //check capturemoves
                            do {
                                ArrayList<Integer> move = captureMoves.remove(0);//<<captures>, <empty>>
                                ArrayList<Integer> captures = new ArrayList();

                                //captures <-> empty
                                do {
                                    captures.add(move.remove(0));
                                } while (pieces[1 - color].contains(move.get(0)));

                                //maxCapture +1
                                if (captures.size() > maxCapture) {
                                    pieceMoves.clear();
                                    moves.clear();

                                    maxCapture++;
                                }

                                //empty square(s)
                                for (int step : move) {
                                    //legal move
                                    if (captures.size() == maxCapture) {
                                        pieceMoves.add(new Move(captures, step));
                                    }
                                    
                                    //4 directions
                                    for (Direction diagonal : Direction.values()) {
                                        if (diagonal.hasNext(step)) {
                                            //pawn step
                                            next = diagonal.getNext(step);                                                

                                            //king steps
                                            if (piece == KING[color] && !move.contains(next)) {
                                                while (board[next] == EMPTY && diagonal.hasNext(next)) {
                                                    next = diagonal.getNext(next);
                                                }
                                            }

                                            //legal capture: not captured piece opponent and empty square
                                            if (pieces[1 - color].contains(next) && !captures.contains(next) && diagonal.hasNext(next) && board[diagonal.getNext(next)] == EMPTY) {
                                                capture = next;
                                                next = diagonal.getNext(capture);//empty

                                                captureMove = new ArrayList(captures);
                                                captureMove.addAll(Arrays.asList(new Integer[] {capture, next}));
                                                
                                                //king steps
                                                if (piece == KING[color]) {
                                                    while (diagonal.hasNext(next) && board[diagonal.getNext(next)] == EMPTY) {
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

                            //piece on board
                            board[index] = piece;
                        }
                    }
                }
            }
            
            //piece can move
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
            }
        }

        //2 evaluation
        //continue only if this (game) is on SQUAREBOARD
        if (isShowing()) {
            //gameover
            if (moves.isEmpty()) {
                GAME_OVER.setVisible(true);
            //player move
            } else if (color == player) {
                hintBoard.setHint(moves.keySet());
            //ai move
            } else {
                new Thread(){
                    @Override
                    public void run() {
                        pieceBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...please

                        new PieceMove(color, MinMax.getAIMove(color, board, pieces, moves, AI.getValue())).run();
                    }
                }.start();
            }

            //enable undo
            if (moves.isEmpty() || color == player) {
                UNDO.setEnabled(!positions.isEmpty());
            }
        }
    }
    
    //undo player move -> gameover or player has turn
    @Override
    public void actionPerformed(ActionEvent e) {
        //quit current turn
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        //prepare next (=previous) turn
        pieceBoard.getMove().clear();
        pieceBoard.setBoard(positions.pop());
        pieceBoard.repaint();

        //turn player
        turn(player);
    }
    
    //move in 4 directions
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

    //animation, promotion, captures
    private class PieceMove extends AbstractBoard implements Runnable {
        int color;
        int index;
        char piece;
        Image image;
        Point location;
        
        PieceMove(int color, ArrayList<Integer> move) {
            super(SQUAREBOARD.getSquares());
            
            this.color = color;
            
            //'finish' turn
            if (color == player) {//player
                hintBoard.setVisible(false);
                UNDO.setEnabled(false);
                positions.push(pieceBoard.getBoard());
            } else {//ai
                pieceBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            index = move.remove(0);
            piece = pieceBoard.getIndex(index);
            image = IMAGE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            location = square[index].getLocation();
            
            //prepare animation
            pieceBoard.setMove(move);
            pieceBoard.setIndex(index, EMPTY);
            pieceBoard.add(this, BorderLayout.CENTER);
            pieceBoard.validate();
        }
        
        @Override
        public void run() {
            //animation 
            Direction direction = Direction.getDirection(index, pieceBoard.getMove().get(0));

            //move
            for (int step : pieceBoard.getMove()) {//<<captures>, destination>
                do {
                    //square piece moves to
                    index = direction.getNext(index);
                    
                    //location -> square[index]
                    for (int horizontal = square[index].x - location.x, vertical = square[index].y - location.y, i = FRAMES - 1; i >= 0; i--) {
                        //time in milliseconds
                        long time = System.currentTimeMillis();
                        
                        location.setLocation(square[index].x - (int) (i * (float) horizontal / FRAMES), square[index].y - (int) (i * (float) vertical / FRAMES));
                        
                        repaint();
                        
                        try {
                            Thread.sleep(Math.max(0, time + MILLI - System.currentTimeMillis()));
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

            //finish animation
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

            //turn opponent
            turn(1 - color);
        }
        
        @Override
        public void paint(Graphics g) {
            g.drawImage(image, location.x, location.y, this);
        }
    }

}

