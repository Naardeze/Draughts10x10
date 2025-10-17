package draughts10x10;

import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.SquareBoard.GRID;
import static draughts10x10.SquareBoard.paintSquare;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.JComponent;

/**
 * PositionBoard
 * 
 * position (begin)
 * move (yellow, green)
 * 
 * @author Naardeze
 */

final class PositionBoard extends JComponent {
    //pawn
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    //"wb"
    final static String WB = W + "" + B;

    //empty square
    final static char EMPTY = '_';

    //move colors
    final private static Color[] MOVE = {Color.yellow, Color.green};//<<captures>, piece>

    //images
    final static Image[][] PIECE = new Image[2][WB.length()];

    private char[] position = new char[SQUAREBOARD.square.length];
    private LinkedList<Integer> move = new LinkedList();
    
    PositionBoard() {
        //begin position
        Arrays.fill(position, 0, position.length / 2 - GRID / 2, B);
        Arrays.fill(position, position.length / 2 - GRID / 2, position.length / 2 + GRID / 2, EMPTY);
        Arrays.fill(position, position.length / 2 + GRID / 2, position.length, W);
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
        move.clear();
        
        this.position = position.toCharArray();
    }
    
    LinkedList<Integer> getMove() {
        return move;
    }
            
    void setMove(LinkedList<Integer> move) {
        this.move = move;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        //move
        for (int index : move) {
            g.setColor(MOVE[(move.indexOf(index) + 1) / move.size()]);//[0..0, 1]
            
            paintSquare(g, SQUAREBOARD.square[index]);
        }

        //pieces
        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                g.drawImage(PIECE[(Character.toLowerCase(position[i]) + "" + Character.toUpperCase(position[i])).indexOf(position[i])][WB.indexOf(Character.toLowerCase(position[i]))], SQUAREBOARD.square[i].x, SQUAREBOARD.square[i].y, this);
            }
        }
    }
    
}
