package draughts10x10.board;

import static draughts10x10.board.PositionBoard.WB;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * Board
 * 
 * parent board class
 * 
 * square -> holds squares used by all boards
 * 
 * paintSquare -> fill square
 * 
 * @author Naardeze
 */

public class Board extends JComponent {
    final protected static Image[][] PIECE = new Image[2][WB.length()];

    final protected Rectangle[] square;
    
    protected Board(Rectangle[] square) {
        this.square = square;
    }
    
    //fill square
    protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
}
