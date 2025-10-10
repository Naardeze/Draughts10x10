package draughts10x10.board;

import static draughts10x10.board.SquareBoard.GRID;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * PositionBoard
 * 
 * position (begin)
 * move (yellow, green)
 * 
 * @author Naardeze
 */

final public class PositionBoard extends Board {
    //pawn
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    //"wb"
    final public static String WB = W + "" + B;

    //empty square
    final public static char EMPTY = '_';

    //move colors
    final private static Color[] MOVE = {Color.yellow, Color.green};//<<captures>, piece>

    private char[] position;
    private ArrayList<Integer> move = new ArrayList();
    
    public PositionBoard(Rectangle[] square) {
        super(square);
        
        //begin position
        position = new char[square.length];
  
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
        move.clear();
        
        this.position = position.toCharArray();
    }
    
    public ArrayList<Integer> getMove() {
        return move;
    }
            
    public void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    //paint board
    @Override
    public void paintComponent(Graphics g) {
        //move
        for (int index : move) {
            g.setColor(MOVE[(move.indexOf(index) + 1) / move.size()]);//[0..0, 1]
            
            paintSquare(g, square[index]);
        }

        //position
        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                g.drawImage(PIECE[(Character.toLowerCase(position[i]) + "" + Character.toUpperCase(position[i])).indexOf(position[i])][WB.indexOf(Character.toLowerCase(position[i]))], square[i].x, square[i].y, this);
            }
        }
    }
    
}
