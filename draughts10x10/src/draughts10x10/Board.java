package draughts10x10;

import static draughts10x10.PositionBoard.WB;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * Board
 * 
 * parent board (abstract)
 * 
 * PIECE images
 * squares
 * 
 * -paintSquare (static)
 * 
 * @author Naardeze
 */

abstract class Board extends JComponent {
    final protected static Image[][] PIECE = new Image[2][WB.length()];

    final protected Rectangle[] square;
    
    protected Board(Rectangle[] square) {
        this.square = square;
    }
    
    //paint square
    protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
}
