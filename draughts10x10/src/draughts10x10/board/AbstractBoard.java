package draughts10x10.board;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * abstract super board class
 * contains squares
 * 
 * paintSquare (static)
 * 
 * @author Naardeze
 */

abstract public class AbstractBoard extends JComponent {
    final protected Rectangle[] square;
    
    protected AbstractBoard(Rectangle[] square) {
        this.square = square;
    }
    
    protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
}
