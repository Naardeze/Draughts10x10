package draughts10x10.board;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import javax.imageio.ImageIO;

/*

board (wood) with squares.
squares are used by all boards.

*/

public class SquareBoard extends AbstractBoard {    
    final public static int SIZE = 10;//squares (light and dark)
    final public static int SQUARES = SIZE * SIZE / 2;//50 (dark squares)
    
    //square color
    final private static Color DARK = new Color(170, 125, 85);
    
    //background
    private static Image wood;
    
    public SquareBoard(int color) {
        super(new Rectangle[SQUARES]);
        
        for (int i = 0; i < square.length; i++) {
            square[i] = new Rectangle();
        }
        
        setForeground(DARK);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                for (int i = 0; i < square.length; i++) {
                    square[new int[] {i, square.length - 1 - i}[color]].setBounds(x(i) * getWidth() / SIZE, y(i) * getHeight() / SIZE, getWidth() / SIZE, getHeight() / SIZE);
                }
                
                repaint();
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
    public void paint(Graphics g) {
        g.drawImage(wood, 0, 0, getWidth(), getHeight(), null);
        
        for (Rectangle square : square) {
            paintSquare(g, square);
        }
    }
    
    static {
        try {
            wood = ImageIO.read(new File("wood.jpg"));
        } catch (Exception ex) {}
    }
    
}

