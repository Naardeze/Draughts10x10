package draughts10x10.board;

import static draughts10x10.board.PositionBoard.WB;
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

final public class SquareBoard extends Board implements ActionListener {    
    //grid size
    final public static int GRID = 10;
    
    //square color
    final private static Color DARK = new Color(175, 125, 85);
    
    //background
    private static Image wood;
    
    public SquareBoard() {
        super(new Rectangle[GRID * GRID / 2]);

        setLayout(new BorderLayout());//add(game, "Center");
        setForeground(DARK);
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

    //column
    public static int x(int index) {
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    //row
    public static int y(int index) {
        return index / (GRID / 2);
    }
    
    public Rectangle[] getSquares() {
        return square;
    }
    
//    //fill square
  //  static void paintSquare(Graphics g, Rectangle square) {
    //    g.fillRect(square.x, square.y, square.width, square.height);
   // }
    
    //paint board
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
