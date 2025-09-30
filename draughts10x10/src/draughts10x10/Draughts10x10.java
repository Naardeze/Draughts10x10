package draughts10x10;

import static draughts10x10.PositionBoard.WB;
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
 * Draughts10x10 (main)
 * 
 * Game (WHITE, BLACK) -> new game
 * AI (1-7) -> search depth (moves + 1-7 + depth=0 while contains captures)
 * 
 * undo move (color=player or game over)
 * hint on/off
 * rotate board (1-50 -> 50-1 -> etc)
 * 
 * @author Naardeze
*/

final class Draughts10x10 extends JFrame {
    //colors
    final static int WHITE = 0;
    final static int BLACK = 1;

    //AI search depth
    final static JSlider AI = new JSlider(1, 7); 
    
    //board 10x10
    final static SquareBoard SQUAREBOARD = new SquareBoard();
    
    //static components
    final static JButton UNDO = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().createImage("arrow.png")));
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);
    final static JCheckBox HINT = new JCheckBox("Hint");
    
    //game on SQUAREBOARD
    private Game game = new Game(WHITE);
    
    private Draughts10x10(int boardSize) {//500
        super("Draughts10x10");

        //prepare window
        JMenuBar menuBar = new JMenuBar();//menu, AI
        JMenu menu = new JMenu("Game");//WHITE, BLACK
        
        JButton rotation = new JButton("\ud83d\udd04");//rotate squareboard

        JPanel center = new JPanel();//SQUAREBOARD
        JPanel south = new JPanel(new GridLayout(1, 3));//left, GAME_OVER, right

        JPanel left = new JPanel();//UNDO
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));//HINT, rotation

        //new game
        for (int color : new int[] {WHITE, BLACK}) {
            menu.add(new JMenuItem(new String[] {"White", "Black"}[color], new ImageIcon(Toolkit.getDefaultToolkit().getImage(WB.toCharArray()[color] + ".png").getScaledInstance(24, 24, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                SQUAREBOARD.remove(game);
                
                game = new Game(color);
                
                SQUAREBOARD.add(game, BorderLayout.CENTER);
                SQUAREBOARD.validate();
            });
        }

        AI.setMajorTickSpacing(1);
        AI.setPaintTicks(true);
        AI.setOpaque(false);

        //game on/off SQUAREBOARD 
        SQUAREBOARD.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener((Game) e.getChild());//game on
            }        
            @Override
            public void componentRemoved(ContainerEvent e) {
                UNDO.removeActionListener((Game) e.getChild());//game off
            }
        });
        SQUAREBOARD.setPreferredSize(new Dimension(boardSize, boardSize));
        SQUAREBOARD.add(game, BorderLayout.CENTER);

        UNDO.setContentAreaFilled(false);
        UNDO.setBorder(null);
        UNDO.setFocusable(false);
        
        GAME_OVER.setFont(GAME_OVER.getFont().deriveFont(16f));
        
        HINT.setHorizontalTextPosition(JCheckBox.LEFT);
        HINT.setFont(HINT.getFont().deriveFont(16f));
        HINT.setFocusable(false);
        HINT.addActionListener(e -> game.repaint());
        
        rotation.setContentAreaFilled(false);
        rotation.setBorder(null);
        rotation.setFocusable(false);
        rotation.setFont(rotation.getFont().deriveFont(Font.PLAIN, 16));
        rotation.addActionListener(SQUAREBOARD);
        
        menuBar.setLayout(new BorderLayout());
        
        menuBar.add(menu, BorderLayout.WEST);
        menuBar.add(AI, BorderLayout.EAST);

        center.add(SQUAREBOARD);
        
        left.add(UNDO);

        right.add(HINT);
        right.add(rotation);
        
        south.add(left);
        south.add(GAME_OVER);
        south.add(right);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        
        setIconImage(Toolkit.getDefaultToolkit().getImage("bb.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(menuBar);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        int boardSize = 500;
        
        //start program (SQUAREBOARD size)
        new Draughts10x10(boardSize);
    }
    
}

