package draughts10x10;

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

/**
 * SquareBoard
 * 
 * board (wood) with squares (used in positionBoard and hintBoard)
 * sizes squares and piece images
 * 
 * @author Naardeze
*/

final class SquareBoard extends Board implements ActionListener {    
    //grid  size
    final static int GRID = 10;
    
    //square color
    final private static Color DARK = new Color(182, 132, 90);
    
    //background
    private static Image wood;
    
    SquareBoard() {
        super(new Rectangle[GRID * GRID / 2]);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth() / GRID;//square width
                int height = getHeight() / GRID;//square height
                
                //square[0] top left
                for (int i = 0; i < square.length; i++) {
                    square[i] = new Rectangle(x(i) * width, y(i) * height, width, height);
                }
                
                //images scaled square size
                for (char piece : WB.toCharArray()) {
                    PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//pawn
                    PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + "" + piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//king
                }
            }
        });
        
        
        setForeground(DARK);
        setLayout(new BorderLayout());//adding game
    }

    //<-column
    static int x(int index) {
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    //<-row
    static int y(int index) {
        return index / (GRID / 2);
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

    static {
        try {
            wood = ImageIO.read(new File("wood.png"));
        } catch (IOException ex) {}
    }
}

