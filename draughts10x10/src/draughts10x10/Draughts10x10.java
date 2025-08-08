package draughts10x10;

import static draughts10x10.board.PositionBoard.WB;
import static draughts10x10.board.SquareBoard.SIZE;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;

/*

main window

game -> white-black
AI -> 1-7
undo move (only player moves)
hint on/off

*/

public class Draughts10x10 extends JFrame {
    //colors
    final static int WHITE = 0;
    final static int BLACK = 1;
    
    final private static int MIN_DEPTH = 1;//min search depth
    final private static int MAX_DEPTH = 7;//max search depth
    
    final static JSlider AI = new JSlider(MIN_DEPTH, MAX_DEPTH);
    
    final static JButton UNDO = new JButton("\u25c0");//undo move buttton
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);//game over text
    final public static JCheckBox HINT = new JCheckBox("Hint");//hint on/off
    
    //game size
    final private static int DIMENSION = Math.min(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height) / 2 / SIZE * SIZE;

    //piece images [wb][WB]
    final public static Image[][] IMAGE = new Image[2][WB.length()];

    //game (white by default)
    private Game game = new Game(WHITE);
    
    private Draughts10x10() {
        super("Draughts10x10");
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bk.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        
        JPanel center = new JPanel();
        JPanel south = new JPanel(new GridLayout(1, 3));

        JPanel parentUndo = new JPanel();
        JPanel parentHint = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        for (int color : new int[] {WHITE, BLACK}) {
            gameMenu.add(new JMenuItem(new String[] {"White", "Black"}[color], new ImageIcon(IMAGE[0][color].getScaledInstance(24, 24, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                center.remove(game);
                
                game = new Game(color);
                
                center.add(game);
                validate();
            });
        }
        
        AI.setOpaque(false);
        AI.setMajorTickSpacing(1);
        AI.setPaintTicks(true);
       
        menuBar.setLayout(new BorderLayout());

        menuBar.add(gameMenu, BorderLayout.WEST);
        menuBar.add(AI, BorderLayout.EAST);
        
        setJMenuBar(menuBar);
        
        center.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener(game);
                
                game.setPreferredSize(new Dimension(DIMENSION, DIMENSION));
            }            
            @Override
            public void componentRemoved(ContainerEvent e) {
                UNDO.removeActionListener(game);
            }
        });
        center.add(game);
        
        UNDO.setFocusable(false);
        
        HINT.setHorizontalTextPosition(JCheckBox.LEADING);
        HINT.setFocusable(false);
        HINT.addChangeListener(e -> game.repaint());
        
        parentUndo.add(UNDO);
        parentHint.add(HINT);
        
        south.add(parentUndo);
        south.add(GAME_OVER);
        south.add(parentHint);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) throws IOException {
        //image size
        int size = DIMENSION / SIZE;
        
        //read images (png) and scale smooth
        for (String piece : WB.split("")) {
            IMAGE[0][WB.indexOf(piece)] = ImageIO.read(new File(piece + ".png")).getScaledInstance(size, size, Image.SCALE_SMOOTH);//pawn
            IMAGE[1][WB.indexOf(piece)] = ImageIO.read(new File(piece + "k.png")).getScaledInstance(size, size, Image.SCALE_SMOOTH);//king
        }
        
        //start program
        new Draughts10x10();
    }
    
}
