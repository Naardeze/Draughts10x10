package draughts10x10.board;

import static draughts10x10.Draughts10x10.WHITE;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * board (wood) with squares.
 * squares are used by all boards.
 * 
 * @author Naardeze
*/

public class SquareBoard extends AbstractBoard implements ActionListener {    
    final public static int SIZE = 10;//squares (light and dark)
    final public static int SQUARES = SIZE * SIZE / 2;//50 (dark squares)
    
    //square color
    final private static Color DARK = new Color(175, 130, 90);
    
    //background
    private static Image wood;
    
    public SquareBoard() {
        super(new Rectangle[SQUARES]);
        
        for (int i = 0; i < square.length; i++) {
            square[i] = new Rectangle();
        }
        
        setLayout(new BorderLayout());//adding game
        setForeground(DARK);

        //resized once -> square[0] top left (default player=WHITE)
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
    public static int x(int index) {
        return index % (SIZE / 2) * 2 + 1 - index / (SIZE / 2) % 2;
    }

    //row
    public static int y(int index) {
        return index / (SIZE / 2);
    }
    
    public Rectangle[] getSquares() {
        return square;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(wood, 0, 0, getWidth(), getHeight(), null);
        
        for (Rectangle square : square) {
            paintSquare(g, square);
        }
    }

    //rotate
    @Override
    public void actionPerformed(ActionEvent e) {
        for (Rectangle square : square) {
            square.setLocation(getWidth() - square.x - square.width, getHeight() - square.y - square.height);
        }
        
        repaint();
    }
    
    //wood background image (.jpg)
    static {
        try {
            wood = ImageIO.read(new File("wood.jpg"));
        } catch (Exception ex) {}
    }
}
