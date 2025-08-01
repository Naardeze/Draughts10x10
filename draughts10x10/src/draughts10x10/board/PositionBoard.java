package draughts10x10.board;

import static draughts10x10.board.SquareBoard.SIZE;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/*
    board with pieces and highlighted move squares
*/

public class PositionBoard extends AbstractBoard {
    //pieces
    final public static char W = 'w';//w hite
    final public static char W_KING = Character.toUpperCase(W);//W
    final public static char B = 'b';//b lack
    final public static char B_KING = Character.toUpperCase(B);//B

    //empty square
    final public static char EMPTY = ' ';

    //"wb"
    final public static String WB = String.valueOf(new char[] {W, B});

    //images
    final public static BufferedImage[][] IMAGE = new BufferedImage[2][WB.length()];
    
    //move colors
    final private static Color YELLOW = Color.yellow;//capture
    final private static Color GREEN = Color.green;//move

    private char[] position;
    private ArrayList<Integer> move = new ArrayList();
    
    public PositionBoard(Rectangle[] square) {
        super(square);
        
        position = new char[square.length];

        //begin position
        //1-20
        for (int i = 0; i < position.length / 2 - SIZE / 2; i++) {
            position[i] = B;
        }

        //21-30
        for (int i = position.length / 2 - SIZE / 2; i < position.length / 2 + SIZE / 2; i++) {
            position[i] = EMPTY;
        }

        //31-50
        for (int i = position.length / 2 + SIZE / 2; i < position.length; i++) {
            position[i] = W;
        }
        
        setOpaque(false);
        setLayout(null);//for animation
    }
    
    public char getIndex(int index) {
        return position[index];
    }
    
    public void setIndex(int index, char piece) {
        position[index] = piece;
    }
    
    public String getPosition() {
        return String.valueOf(position);
    }
    
    public void setPosition(String position) {
        this.position = position.toCharArray();
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

        //squares
        for (int index : move) {
            g.setColor(new Color[] {YELLOW, GREEN}[(move.indexOf(index) + 1) / move.size()]);
            
            paintSquare(g, square[index]);
        }

        //pieces
        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                paintPiece(g, IMAGE[(String.valueOf(position[i]).toLowerCase() + String.valueOf(position[i]).toUpperCase()).indexOf(position[i])][WB.indexOf(String.valueOf(position[i]).toLowerCase())], square[i]);
            }
        }
    }
    
    //piece images
    static {
        for (char piece : WB.toCharArray()) {
            try {
                IMAGE[0][WB.indexOf(piece)] = ImageIO.read(new File(String.valueOf(piece) + ".png"));
                IMAGE[1][WB.indexOf(piece)] = ImageIO.read(new File(String.valueOf(piece) + String.valueOf(piece) + ".png"));
            } catch (Exception ex) {}
        }
    }
}
