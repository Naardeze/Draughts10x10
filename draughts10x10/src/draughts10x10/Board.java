package draughts10x10;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

/* Board
 *
 * parent board class
 * 
 * square
 * 
 * -paintSquare (static)
 */

abstract class Board extends JComponent {
    final protected Rectangle[] square;
    
    protected Board(Rectangle[] square) {
        this.square = square;
    }
    
    protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
}
