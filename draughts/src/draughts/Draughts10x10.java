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
 * Draughts10x10 
 * 
 * Main window with the board (TILEBOARD) all games are played on.
 * 
 * Game (WHITE/BLACK)
 * AI level 1-5 (1 level = 2 moves)
 * UNDO
 * HINT On/Off
 * Rotate TILEBOARD
 * 
 * @author Naardeze
 */

public class Draughts10x10 extends JFrame {
    final static int WHITE = 0;
    final static int BLACK = 1;
    
    final static JSlider AI = new JSlider(1, 5);//1 level = 2 depth (alfa beta)
    
    final static TileBoard TILEBOARD = new TileBoard();

    final static JButton UNDO = new JButton(new ImageIcon("arrow.png"));
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);
    final static JCheckBox HINT = new JCheckBox("Hint");
    
    private Game game = new Game(WHITE);
    
    private Draughts10x10(int boardSize) {
        super("Draughts10x10");

        JMenuBar menuBar = new JMenuBar();//menu, AI
        JMenu menu = new JMenu("Game");//WHITE, BLACK
        
        JButton rotation = new JButton("\ud83d\udd04");//rotate TILEBOARD

        JPanel center = new JPanel();//TILEBOARD
        JPanel south = new JPanel(new GridLayout(1, 3));//left, GAME_OVER, right

        JPanel left = new JPanel();//UNDO
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));//HINT, rotation
        
        menuBar.setLayout(new BorderLayout());
        
        menuBar.add(menu, BorderLayout.WEST);
        menuBar.add(AI, BorderLayout.EAST);
        
        for (int color : new int[] {WHITE, BLACK}) {
            menu.add(new JMenuItem(new ImageIcon(Toolkit.getDefaultToolkit().createImage(WB.toCharArray()[color] + ".png").getScaledInstance(42, 42, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                TILEBOARD.remove(game);
                
                game = new Game(color);
                
                TILEBOARD.add(game, BorderLayout.CENTER);
                TILEBOARD.validate();
            });
        }

        TILEBOARD.setPreferredSize(new Dimension(boardSize, boardSize));
        TILEBOARD.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {//game on
                UNDO.addActionListener((Game) e.getChild());
            }        
            
            @Override
            public void componentRemoved(ContainerEvent e) {//game off
                UNDO.removeActionListener((Game) e.getChild());
            }
        });
        TILEBOARD.add(game, BorderLayout.CENTER);
        
        AI.setMajorTickSpacing(1);
        AI.setPaintTicks(true);//no labels -> no extra distraction
        AI.setOpaque(false);
        AI.setToolTipText("" + AI.getValue());//level
        AI.addChangeListener(e -> AI.setToolTipText("" + AI.getValue()));
        
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
        rotation.setFont(rotation.getFont().deriveFont(Font.PLAIN, 16));
        rotation.setFocusable(false);
        rotation.addActionListener(TILEBOARD);

        center.add(TILEBOARD);

        left.add(UNDO);

        right.add(HINT);
        right.add(rotation);
        
        south.add(left);
        south.add(GAME_OVER);
        south.add(right);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bb.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(menuBar);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args)throws Exception {
         //TILEBOARD size
        int boardSize = 600;
        
        //start Draughts10x10
        new Draughts10x10(boardSize);
    }

}

