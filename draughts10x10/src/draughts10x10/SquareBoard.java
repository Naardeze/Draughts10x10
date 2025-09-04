package draughts10x10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * board (wood) with squares
 * squares are used by all boards
 * 
 * @author Naardeze
*/

final class SquareBoard extends AbstractBoard implements ActionListener {    
    //board size
    final static int SIZE = 10;
    
    //square color
    final private static Color DARK = new Color(166, 121, 81);
    
    //background
    private static BufferedImage wood;
    
    SquareBoard() {
        super(new Rectangle[SIZE * SIZE / 2]);
        
        for (int i = 0; i < square.length; i++) {
            square[i] = new Rectangle();
        }
        
        setLayout(new BorderLayout());//adding game
        setForeground(DARK);

        //sized once (default player=WHITE) -> square[0] top left
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                for (int i = 0; i < square.length; i++) {
                    square[i].setBounds(x(i) * getWidth() / SIZE, y(i) * getHeight() / SIZE, getWidth() / SIZE, getHeight() / SIZE);
                }
            }
        });
    }

    //column
    static int x(int index) {
        return index % (SIZE / 2) * 2 + 1 - index / (SIZE / 2) % 2;
    }

    //row
    static int y(int index) {
        return index / (SIZE / 2);
    }

    Rectangle[] getSquares() {
        return square;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(wood, 0, 0, getWidth(), getHeight(), this);
        
        for (Rectangle square : square) {
            paintSquare(g, square);
        }
    }

    //rotation
    @Override
    public void actionPerformed(ActionEvent e) {
        for (Rectangle square : square) {
            square.setLocation(getWidth() - square.x - square.width, getHeight() - square.y - square.height);
        }
        
        repaint();
    }

    static {
        try {
            wood = ImageIO.read(new File("wood.jpg"));
        } catch (IOException ex) {}
    }
}
