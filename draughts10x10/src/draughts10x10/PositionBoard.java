package draughts10x10;

import static draughts10x10.Draughts10x10.SQUAREBOARD;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import static draughts10x10.SquareBoard.GRID;

/**
 * PositionBoard
 * 
 * position (begin)
 * move (yellow, green squares)
 * 
 * @author Naardeze
 */

final class PositionBoard extends Board {
    //pawns
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    //empty square
    final static char EMPTY = '_';

    //"wb"
    final static String WB = W + "" + B;

    //square colors
    final private static Color[] MOVE = {Color.yellow, Color.green};

    private char[] position;
    private ArrayList<Integer> move = new ArrayList();
    
    PositionBoard() {
        super(SQUAREBOARD.square);
        
        position = new char[square.length];

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
            g.setColor(MOVE[(move.indexOf(index) + 1) / move.size()]);//<0..0, 1>
            
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
