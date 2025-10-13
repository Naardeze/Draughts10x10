package draughts10x10;

import static draughts10x10.PositionBoard.PIECE;
import static draughts10x10.PositionBoard.WB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * SquareBoard
 * 
 * squares (50) -> used by all boards
 * 
 * -x (column)
 * -y (row)
 * -getSquares -> used by all boards
 * -paintSquare (g.fillRect(square))
 * -actionPerformed (rotate)
 * 
 * @author Naardeze
*/

final class SquareBoard extends JPanel implements ActionListener {    
    //grid size
    final static int GRID = 10;
    
    //square color
    final private static Color DARK = new Color(160, 110, 70);
    
    //background
    private static Image wood;

    final Rectangle[] square = new Rectangle[GRID * GRID / 2];
    
    SquareBoard() {
        super(new BorderLayout());

        setForeground(DARK);//dark squares
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //square size
                int width = getWidth() / GRID;
                int height = getHeight() / GRID;
                
                //bound squares (square[0] top left)
                for (int i = 0; i < square.length; i++) {
                    square[i] = new Rectangle(x(i) * width, y(i) * height, width, height);
                }
                
                //scale images
                for (char piece : WB.toCharArray()) {
                    PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//man
                    PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + "" + piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//king
                }
            }
        });
    }

    //<-column
    static int x(int index) {
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    //<-row
    static int y(int index) {
       return index / (GRID / 2);
    }
    
    //paint board with squares
    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(wood, 0, 0, getWidth(), getHeight(), null);
        
        for (Rectangle square : square) {
            paintSquare(g, square);
        }
    }

    //rotate board
    @Override
    public void actionPerformed(ActionEvent e) {
        for (Rectangle square : square) {
            square.setLocation(getWidth() - square.x - square.width, getHeight() - square.y - square.height);
        }
        
        repaint();
    }
    
    static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
    static {
        try {
            wood = ImageIO.read(new File("wood.jpg"));
        } catch (IOException ex) {}
    }

}
