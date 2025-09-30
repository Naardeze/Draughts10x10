package draughts10x10;

import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.SquareBoard.GRID;
import static draughts10x10.SquareBoard.paintSquare;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * PositionBoard
 * 
 * position (begin)
 * move (yellow, green squares)
 * 
 * @author Naardeze
 */

final class PositionBoard extends JComponent {
    //pawns
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    //"wb"
    final static String WB = W + "" + B;

    //empty square
    final static char EMPTY = '_';

    //piece images (2x2)
    final static Image[][] PIECE = new Image[2][WB.length()];

    //board
    private char[] position = new char[SQUAREBOARD.square.length];
    //highlighted squares
    private ArrayList<Integer> move = new ArrayList();//captures=yellow, piece=green
    
    PositionBoard() {
        //begin position
        for (int i = 0; i < position.length / 2 - GRID / 2; i++) {//0-19
            position[i] = B;
        }

        for (int i = position.length / 2 - GRID / 2; i < position.length / 2 + GRID / 2; i++) {//20-29
            position[i] = EMPTY;
        }

        for (int i = position.length / 2 + GRID / 2; i < position.length; i++) {//30-49
            position[i] = W;
        }
    }
    
    char getIndex(int index) {
        return position[index];
    }
    
    void setIndex(int index, char piece) {
        position[index] = piece;
    }
    
    String getPosition() {
        return String.valueOf(position);
    }
    
    void setPosition(String position) {
        this.position = position.toCharArray();
    }
    
    ArrayList<Integer> getMove() {
        return move;
    }
            
    void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //move
        for (int index : move) {
            g.setColor(new Color[] {Color.yellow, Color.green}[(move.indexOf(index) + 1) / move.size()]);//<0..0, 1>
            
            paintSquare(g, SQUAREBOARD.square[index]);
        }

        //position
        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                g.drawImage(PIECE[(Character.toLowerCase(position[i]) + "" + Character.toUpperCase(position[i])).indexOf(position[i])][WB.indexOf(Character.toLowerCase(position[i]))], SQUAREBOARD.square[i].x, SQUAREBOARD.square[i].y, this);
            }
        }
    }
    
    static {
        for (char piece : WB.toCharArray()) {
            PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + ".png");//pawn
            PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + "" + piece + ".png");//king
        }
    }
    
}
