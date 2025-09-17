package draughts10x10;

import static draughts10x10.PositionBoard.IMAGE;
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
import javax.imageio.ImageIO;

/* SquareBoard
 *
 * board (wood) with squares
 *
 *-x (static) column
 *-y (static) row
*/

final class SquareBoard extends Board implements ActionListener {
    //grid
    final static int SIZE = 10;
    
    //square color
    final private static Color DARK = new Color(166, 121, 81);
    
    //background
    private static Image wood;
    
    SquareBoard() {
        super(new Rectangle[SIZE * SIZE / 2]);
        
        for (int i = 0; i < square.length; i++) {
            square[i] = new Rectangle();
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //square[0] top left
                for (int i = 0; i < square.length; i++) {
                    square[i].setBounds(x(i) * getWidth() / SIZE, y(i) * getHeight() / SIZE, getWidth() / SIZE, getHeight() / SIZE);
                }
                
                //scale square size
                for (char piece : WB.toCharArray()) {
                    IMAGE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + ".png").getScaledInstance(getWidth() / SIZE, getHeight() / SIZE, Image.SCALE_SMOOTH);//pawn
                    IMAGE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + "" + piece + ".png").getScaledInstance(getWidth() / SIZE, getHeight() / SIZE, Image.SCALE_SMOOTH);//king
                }
            }
        });

        setForeground(DARK);
        setLayout(new BorderLayout());//add(game, CENTER)
    }

    //column
    static int x(int index) {
        return index % (SIZE / 2) * 2 + 1 - index / (SIZE / 2) % 2;
    }

    //row
    static int y(int index) {
        return index / (SIZE / 2);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(wood, 0, 0, getWidth(), getHeight(), null);
        
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
            wood = ImageIO.read(new File("wood.png"));
        } catch (Exception ex) {}
    }
}
