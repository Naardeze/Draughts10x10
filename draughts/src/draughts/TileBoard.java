package draughts;

import static draughts.PieceBoard.PIECE;
import static draughts.PieceBoard.WB;
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
 * TileBoard
 * 
 * Board (wood) with dark tiles
 * Scale pieces (tile size)
 * 
 * tile[50]
 *
 *-x (column)
 *-y (row)
 *-paintTile (g.fillRect(tile))
 *-actionPerformed (rotate)
 * 
 * @author Naardeze
*/

final class TileBoard extends JPanel implements ActionListener {    
    final static int GRID = 10;//10x10

    final private static Color DARK = new Color(165, 115, 75);//tile color
    
    private static Image wood;//background
    
    final Rectangle[] tile = new Rectangle[GRID * GRID / 2];//50
    
    TileBoard() {
        super(new BorderLayout());
        
        setForeground(DARK);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {//At start program
                int width = getWidth() / GRID;
                int height = getHeight() / GRID;
                
                for (int i = 0; i < tile.length; i++) {//tile[0] -> (1,0)...tile[49] -> (8, 9); top left to bottom right
                    tile[i] = new Rectangle(x(i) * width, y(i) * height, width, height);
                }
                
                for (char piece : WB.toCharArray()) {
                    PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//pawm
                    PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + "" + piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//king
                }
            }
        });
    }

    static int x(int index) {
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    static int y(int index) {
       return index / (GRID / 2);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(wood, 0, 0, getWidth(), getHeight(), null);
        
        for (Rectangle tile : tile) {
            paintTile(g, tile);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Rectangle tile : tile) {
            tile.setLocation(getWidth() - tile.x - tile.width, getHeight() - tile.y - tile.height);
        }
        
        repaint();
    }
    
    static void paintTile(Graphics g, Rectangle tile) {
        g.fillRect(tile.x, tile.y, tile.width, tile.height);
    }
    
    static {
        try {
            wood = ImageIO.read(new File("wood.jpg"));
        } catch (IOException ex) {}
    }

}


