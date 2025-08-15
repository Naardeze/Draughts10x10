package draughts10x10;

import static draughts10x10.board.PieceBoard.WB;
import draughts10x10.board.SquareBoard;
import static draughts10x10.board.SquareBoard.SIZE;
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
 * main (JFrame)
 * 
 * game -> white/black
 * AI -> 1-7
 * 
 * undo player move
 * hint on/off
 * 
 * @author Naardeze
 */

public class Draughts10x10 extends JFrame {
    //colors
    final public static int WHITE = 0;
    final public static int BLACK = 1;

    //search depth
    final private static int MIN_DEPTH = 1;
    final private static int MAX_DEPTH = 5;//can take some time
   
    //ai -> 1 'value' = 2 moves (min & max)
    final static JSlider AI = new JSlider(MIN_DEPTH, MAX_DEPTH);
    
    //empty board
    final static SquareBoard SQUAREBOARD = new SquareBoard();
    
    //squareboard size
    final public static int DIMENSION = Math.min(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height) / 2 / SIZE * SIZE;

    final static JButton UNDO = new JButton("\u25c0");//undo move buttton
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);//game over text
    final public static JCheckBox HINT = new JCheckBox("Hint");//hint on/off
    
    //game (white by default)
    private Game game = new Game(WHITE);
    
    private Draughts10x10() {
        super("Draughts10x10");
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bk.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        
        //rotate squareboard
        JButton rotation = new JButton("\ud83d\udd04");

        JPanel center = new JPanel();
        JPanel south = new JPanel(new GridLayout(1, 3));

        JPanel parent1 = new JPanel();
        JPanel parent2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        //nes game
        for (int color : new int[] {WHITE, BLACK}) {
            gameMenu.add(new JMenuItem(new String[] {"White", "Black"}[color], new ImageIcon(Toolkit.getDefaultToolkit().createImage(WB.toCharArray()[color] + ".png").getScaledInstance(24, 24, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                SQUAREBOARD.remove(game);
                
                game = new Game(color);
                
                SQUAREBOARD.add(game, BorderLayout.CENTER);
                SQUAREBOARD.validate();
            });
        }
        
        AI.setOpaque(false);
        AI.setMajorTickSpacing(1);
        AI.setPaintTicks(true);
       
        menuBar.setLayout(new BorderLayout());

        menuBar.add(gameMenu, BorderLayout.WEST);
        menuBar.add(AI, BorderLayout.EAST);
        
        setJMenuBar(menuBar);

        SQUAREBOARD.setPreferredSize(new Dimension(DIMENSION, DIMENSION));
        SQUAREBOARD.addContainerListener(new ContainerAdapter() {
            //game on
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener(game);
            }        
            //game off
            @Override
            public void componentRemoved(ContainerEvent e) {
                UNDO.removeActionListener(game);
            }
        });
        SQUAREBOARD.add(game, BorderLayout.CENTER);
        
        center.add(SQUAREBOARD);
        
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
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        new Draughts10x10();
    }
    
}
