package draughts10x10;

import static draughts10x10.PieceBoard.IMAGE;
import static draughts10x10.PieceBoard.WB;
import static draughts10x10.SquareBoard.SIZE;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
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

/**
 * main
 * 
 * new game -> white, black
 * AI -> 1-7
 * 
 * undo player move
 * hint on/off
 * rotate board
 * 
 * @author Naardeze
 */

final class Draughts10x10 extends JFrame {
    //colors
    final static int WHITE = 0;
    final static int BLACK = 1;

    //ai search depth (moves + 1-7)
    final static JSlider AI = new JSlider(1, 7);
    
    //squareboard
    final static SquareBoard SQUAREBOARD = new SquareBoard();
    
    //static components
    final static JButton UNDO = new JButton(new ImageIcon("arrow.png"));//undo move
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);//game_over text
    final static JCheckBox HINT = new JCheckBox("Hint");//hint on/off
    
    //squareboard size
    final private static int DIMENSION = Math.min(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height) / 2 / SIZE * SIZE;
    
    //current game
    private Game game = new Game(WHITE);
    
    private Draughts10x10() {
        super("Draughts10x10");
        
        //top of frame
        setIconImage(Toolkit.getDefaultToolkit().createImage("bk.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JMenuBar menuBar = new JMenuBar();//gameMenu, AI
        JMenu menu = new JMenu("Game");//WHITE, BLACK
        
        JButton rotation = new JButton("\ud83d\udd04");//rotate squareboard

        JPanel center = new JPanel();//SQUAREBOARD
        JPanel south = new JPanel(new GridLayout(1, 3));//parent1, GAME_OVER, parent2

        JPanel parent1 = new JPanel();//UNDO
        JPanel parent2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));//HINT, rotation

        //menuBar
        menuBar.setLayout(new BorderLayout());

        menuBar.add(menu, BorderLayout.WEST);
        menuBar.add(AI, BorderLayout.EAST);

        //gameMenu
        for (int color : new int[] {WHITE, BLACK}) {
            menu.add(new JMenuItem(new String[] {"White", "Black"}[color], new ImageIcon(Toolkit.getDefaultToolkit().createImage(WB.toCharArray()[color] + ".png").getScaledInstance(24, 24, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                SQUAREBOARD.remove(game);
                
                game = new Game(color);
                
                SQUAREBOARD.add(game, BorderLayout.CENTER);
                SQUAREBOARD.validate();
            });
        }
        
        AI.setOpaque(false);
        AI.setMajorTickSpacing(1);
        AI.setPaintTicks(true);
       
        //center
        SQUAREBOARD.setPreferredSize(new Dimension(DIMENSION, DIMENSION));
        SQUAREBOARD.addContainerListener(new ContainerAdapter() {
            //game on/off SQUAREBOARD
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener((Game) e.getChild());
            }        
            @Override
            public void componentRemoved(ContainerEvent e) {
                UNDO.removeActionListener((Game) e.getChild());
            }
        });
        SQUAREBOARD.add(game, BorderLayout.CENTER);
        
        center.add(SQUAREBOARD);
        
        //south
        UNDO.setContentAreaFilled(false);
        UNDO.setBorder(null);
        
        HINT.setHorizontalTextPosition(JCheckBox.LEFT);
        HINT.setFocusable(false);
        HINT.addChangeListener(e -> game.repaint());
        
        rotation.setContentAreaFilled(false);
        rotation.setBorder(null);
        rotation.setFocusable(false);
        rotation.setFont(rotation.getFont().deriveFont(Font.PLAIN, 14));
        rotation.addActionListener(SQUAREBOARD);
        
        parent1.add(UNDO);

        parent2.add(HINT);
        parent2.add(rotation);
        
        south.add(parent1);
        south.add(GAME_OVER);
        south.add(parent2);
        
        //frame
        setJMenuBar(menuBar);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        //image size (square)
        int size = DIMENSION / SIZE;
        
        //read piece images (png)
        for (char piece : WB.toCharArray()) {
            IMAGE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + ".png").getScaledInstance(size, size, Image.SCALE_SMOOTH);//pawn (w, b)
            IMAGE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + "k.png").getScaledInstance(size, size, Image.SCALE_SMOOTH);//pawn (w, b)
        }
        
        //start program
        new Draughts10x10();
    }
    
}
