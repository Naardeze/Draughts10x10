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
 * Board10x10
 
 board with squares (50)
 
 -x(index) (static) -> column
 * -y(index) (static) -> row
 * -paintSquare(g, square) (static) -> g.fillRect(square)
 * -actionPerformed -> rotate
 * 
 * @author Naardeze
*/

final class Board10x10 extends JPanel implements ActionListener {    
    //grid size
    final static int GRID = 10;
    
    //background
    private static Image wood;
    
    //squares
    final Rectangle[] square = new Rectangle[GRID * GRID / 2];
    
    Board10x10() {
        super(new BorderLayout());//add(game, "Center");
    
        setForeground(new Color(174, 124, 84));//square color
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //square size
                int width = getWidth() / GRID;
                int height = getHeight() / GRID;
                
                //bound squares
                for (int i = 0; i < square.length; i++) {
                    square[i] = new Rectangle(x(i) * width, y(i) * height, width, height);
                }
                
                //scale images
                for (char piece : WB.toCharArray()) {
                    PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//pawn
                    PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + "" + piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//king
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
    
    //fill square
    static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
    @Override
    public void paintComponent(Graphics g) {
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
            wood = ImageIO.read(new File("wood.jpg"));
        } catch (IOException ex) {}
    }

}
