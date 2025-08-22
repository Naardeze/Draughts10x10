package draughts10x10;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * abstract super board class
 * contains squares
 * 
 * paintSquare (static)
 * paintPiece (static)
 * 
 * @author Naardeze
 */

abstract class AbstractBoard extends JComponent {
    final protected Rectangle[] square;
    
    protected AbstractBoard(Rectangle[] square) {
        this.square = square;
    }
    
    //fill the square
    protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
    //paint piece image
    protected static void paintPiece(Graphics g, Image image, Point location) {
        g.drawImage(image, location.x, location.y, null);
    }

}
