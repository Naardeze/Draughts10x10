package draughts;

import static draughts.PieceBoard.WB;
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
 * Draughts (main)
 *
 * BOARD10X10 -> the board all games are played on
 * game -> current game on BOARD10X10
 * menu -> new Game (WHITE/BLACK)
 * LEVEL (1-5) -> AI (1 level = 2 moves)
 * ARROW -> undo move
 * HINT -> color moveable
 * Rotate -> BOARD10X10
 * 
 * @author Naardeze
 */

final class Draughts extends JFrame {
    final static int WHITE = 0;
    final static int BLACK = 1;

    final static JSlider LEVEL = new JSlider(1, 5);//3
    
    final static Board10x10 BOARD10X10 = new Board10x10();

    final static JButton ARROW = new JButton(new ImageIcon("arrow.png"));
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);
    final static JCheckBox HINT = new JCheckBox("Hint");
    
    private Game game = new Game(WHITE);//WHITE by default
    
    private Draughts(int boardSize) {
        super("Draughts10x10");

        JMenuBar menuBar = new JMenuBar();//menu, LEVEL
        JMenu menu = new JMenu("Game");//WHITE, BLACK
        
        JButton rotation = new JButton("\ud83d\udd04");//rotate BOARD10X10

        JPanel center = new JPanel();//BOARD10X10
        JPanel south = new JPanel(new GridLayout(1, 3));//left, GAME_OVER, right

        JPanel left = new JPanel();//ARROW
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));//HINT, rotation
        
        for (int color : new int[] {WHITE, BLACK}) {
            menu.add(new JMenuItem(new ImageIcon(Toolkit.getDefaultToolkit().createImage(WB.toCharArray()[color] + ".png").getScaledInstance(44, 44, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                BOARD10X10.remove(game);
                
                game = new Game(color);

                BOARD10X10.add(game, BorderLayout.CENTER);
                BOARD10X10.validate();
            });
        }
        
        LEVEL.setMajorTickSpacing(1);
        LEVEL.setPaintTicks(true);//no labels (ToolTip) -> no distraction
        LEVEL.setOpaque(false);
        LEVEL.setToolTipText("" + LEVEL.getValue());//label
        LEVEL.addChangeListener(e -> LEVEL.setToolTipText("" + LEVEL.getValue()));
        
        BOARD10X10.setPreferredSize(new Dimension(boardSize, boardSize));
        BOARD10X10.addContainerListener(new ContainerAdapter() {//game on/off BOARD10X10
            @Override
            public void componentAdded(ContainerEvent e) {//on
                ARROW.addActionListener((Game) e.getChild());
            }        
            
            @Override
            public void componentRemoved(ContainerEvent e) {//off
                ARROW.removeActionListener((Game) e.getChild());
            }
        });
        BOARD10X10.add(game, BorderLayout.CENTER);
        
        ARROW.setContentAreaFilled(false);
        ARROW.setBorder(null);
        ARROW.setFocusable(false);

        GAME_OVER.setFont(GAME_OVER.getFont().deriveFont(16f));
        
        HINT.setHorizontalTextPosition(JCheckBox.LEFT);
        HINT.setFont(HINT.getFont().deriveFont(16f));
        HINT.setFocusable(false);
        HINT.addActionListener(e -> game.repaint());
        
        rotation.setContentAreaFilled(false);
        rotation.setBorder(null);
        rotation.setFont(rotation.getFont().deriveFont(Font.PLAIN, 16));
        rotation.setFocusable(false);
        rotation.addActionListener(BOARD10X10);

        menuBar.setLayout(new BorderLayout());
        
        menuBar.add(menu, BorderLayout.WEST);
        menuBar.add(LEVEL, BorderLayout.EAST);
        
        center.add(BOARD10X10);

        left.add(ARROW);

        right.add(HINT);
        right.add(rotation);
        
        south.add(left);
        south.add(GAME_OVER);
        south.add(right);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bb.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(menuBar);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        int boardSize = 560;

        //start Draughts
        new Draughts(boardSize);
    }

}
