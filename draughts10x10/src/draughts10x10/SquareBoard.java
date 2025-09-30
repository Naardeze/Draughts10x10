package draughts10x10;

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
import javax.swing.JPanel;

/**
 * SquareBoard
 * 
 * board with squares (50)
 * 
 * -x(index) (static) -> column
 * -y(index) (static) -> row
 * 
 * @author Naardeze
*/

final class SquareBoard extends JPanel implements ActionListener {    
    //grid size
    final static int GRID = 10;
    
    //background
    final private static Image WOOD = Toolkit.getDefaultToolkit().getImage("wood.png");
    
    //dark squares
    final Rectangle[] square = new Rectangle[GRID * GRID / 2];
    
    SquareBoard() {
        super(new BorderLayout());//add(game, "Center");
    
        //square color
        setForeground(new Color(183, 133, 91));

        //bound squares (square[0] top left)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                for (int width = getWidth() / GRID, height = getHeight() / GRID, i = 0; i < square.length; i++) {
                    square[i] = new Rectangle(x(i) * width, y(i) * height, width, height);
                }
            }
        });
    }

    //column
    static int x(int index) {
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    //row
    static int y(int index) {
        return index / (GRID / 2);
    }

    @Override
    public void paintComponent(Graphics g) {
        //background (wood)
        g.drawImage(WOOD, 0, 0, this);
        
        //paint squares
        for (Rectangle square : square) {
            paintSquare(g, square);
        }
    }

    //rotate squares
    @Override
    public void actionPerformed(ActionEvent e) {
        for (Rectangle square : square) {
            square.setLocation(getWidth() - square.x - square.width, getHeight() - square.y - square.height);
        }
        
        repaint();
    }
    
    //paint square
    static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }

}

