package draughts10x10.board;

import static draughts10x10.Draughts10x10.DIMENSION;
import static draughts10x10.board.SquareBoard.SIZE;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * boear with pieces and highlighted move squares
 * 
 * start position by default
 * 
 * @author Naardeze
 */

public class PieceBoard extends AbstractBoard {
    //pieces
    final public static char W = 'w';//w(hite) pawn
    final public static char W_KING = Character.toUpperCase(W);//W king
    final public static char B = 'b';//b(lack) pawn
    final public static char B_KING = Character.toUpperCase(B);//B king

    //empty square
    final public static char EMPTY = ' ';

    //"wb"
    final public static String WB = W + "" + B;

    //piece images [wb][WB]
    final public static Image[][] IMAGE = new Image[2][WB.length()];

    //move colors
    final private static Color[] COLOR = {Color.yellow, Color.green};

    private char[] board;
    private ArrayList<Integer> move = new ArrayList();
    
    public PieceBoard(Rectangle[] square) {
        super(square);
        
        board = new char[square.length];

        //start position
        //1-20 -> 'b'
        for (int i = 0; i < board.length / 2 - SIZE / 2; i++) {
            board[i] = B;
        }

        //21-30 -> ' '
        for (int i = board.length / 2 - SIZE / 2; i < board.length / 2 + SIZE / 2; i++) {
            board[i] = EMPTY;
        }

        //31-50 -> 'w'
        for (int i = board.length / 2 + SIZE / 2; i < board.length; i++) {
            board[i] = W;
        }
        
        setLayout(new BorderLayout());//adding boardmove
    }
    
    public char getIndex(int index) {
        return board[index];
    }
    
    public void setIndex(int index, char piece) {
        board[index] = piece;
    }
    
    public String getBoard() {
        return String.valueOf(board);
    }
    
    public void setBoard(String board) {
        this.board = board.toCharArray();
    }
    
    public ArrayList<Integer> getMove() {
        return move;
    }
            
    public void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //move
        for (int index : move) {
            //only the last green
            g.setColor(COLOR[(move.indexOf(index) + 1) / move.size()]);
            
            paintSquare(g, square[index]);
        }

        //board
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                g.drawImage(IMAGE[(Character.toLowerCase(board[i]) + "" + Character.toUpperCase(board[i])).indexOf(board[i])][WB.indexOf(Character.toLowerCase(board[i]))], square[i].x, square[i].y, null);
            }
        }
    }
    
    static {
        //image size (game size / squares)
        int size = DIMENSION / SIZE;
        
        //piece images (.png) and scale size (smooth)
        for (char piece : WB.toCharArray()) {
            try {
                IMAGE[0][WB.indexOf(piece)] = ImageIO.read(new File(piece + ".png")).getScaledInstance(size, size, Image.SCALE_SMOOTH);//pawn
                IMAGE[1][WB.indexOf(piece)] = ImageIO.read(new File(piece + "k.png")).getScaledInstance(size, size, Image.SCALE_SMOOTH);//king
            } catch (IOException ex) {}
        }
        
    }

}
