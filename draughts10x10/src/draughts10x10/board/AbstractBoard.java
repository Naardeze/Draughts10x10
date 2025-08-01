package draughts10x10.board;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/*
    super board class, containing squares
*/

abstract public class AbstractBoard extends JPanel {
    final protected Rectangle[] square;
    
    protected AbstractBoard(Rectangle[] square) {
        this.square = square;
    }
    
    protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
    protected static void paintPiece(Graphics g, BufferedImage image, Rectangle bounds) {
        g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
    }
}
