package draughts10x10;

import static draughts10x10.SquareBoard.SIZE;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import static draughts10x10.Draughts10x10.SQUAREBOARD;

/* PieceBoard
 *
 * position (begin)
 * move
 */

final class PositionBoard extends Board {
    //pawn
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    //"wb"
    final static String WB = W + "" + B;

    //empty square
    final static char EMPTY = '_';

    //[wb][WB]
    final static Image[][] IMAGE = new Image[2][WB.length()];
    
    //move colors
    final private static Color[] COLOR = {Color.yellow, Color.green};//{<captures>, piece}

    private char[] position;
    private ArrayList<Integer> move = new ArrayList();
    
    PositionBoard() {
        super(SQUAREBOARD.square);
        
        position = new char[square.length];

        //begin position
        for (int i = 0; i < position.length / 2 - SIZE / 2; i++) {//0-19
            position[i] = B;
        }

        for (int i = position.length / 2 - SIZE / 2; i < position.length / 2 + SIZE / 2; i++) {//20-29
            position[i] = EMPTY;
        }

        for (int i = position.length / 2 + SIZE / 2; i < position.length; i++) {//30-49
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
        for (int index : move) {//<0..0, 1>
            g.setColor(COLOR[(move.indexOf(index) + 1) / move.size()]);
            
            paintSquare(g, square[index]);
        }

        //position
        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                g.drawImage(IMAGE[(Character.toLowerCase(position[i]) + "" + Character.toUpperCase(position[i])).indexOf(position[i])][WB.indexOf(Character.toLowerCase(position[i]))], square[i].x, square[i].y, this);
            }
        }
    }
    
}
