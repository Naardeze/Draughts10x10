package draughts10x10;

import draughts10x10.board.PositionBoard;
import static draughts10x10.board.SquareBoard.SIZE;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

public class Draughts10x10 extends JFrame {
    //colors
    final public static int WHITE = 0;
    final public static int BLACK = 1;
    
    //ai search depth
    final private static int MIN_DEPTH = 1;
    final private static int MAX_DEPTH = 5;//min & max node -> search depth = depth x 2
    
    final public static JSlider AI = new JSlider(MIN_DEPTH, MAX_DEPTH);
    
    final public static JButton UNDO = new JButton("\u25c0");
    final public static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);
    final public static JCheckBox HINT = new JCheckBox("Hint");
    
    private Game game = new Game(WHITE);
    
    private Draughts10x10() {
        super("Draughts10x10");
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bb.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu gameMenu = new JMenu("Game");
        JMenu aiMenu = new JMenu("AI");
        
        JPanel center = new JPanel();
        JPanel south = new JPanel(new GridLayout(1, 3));

        JPanel undoPanel = new JPanel();
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        for (int color : new int[] {WHITE, BLACK}) {
            gameMenu.add(new JMenuItem(new ImageIcon(PositionBoard.IMAGE[0][color].getScaledInstance(40, 40, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                center.remove(game);
                
                game = new Game(color);
                
                center.add(game);
                center.validate();
            });
        }
        
        AI.setMajorTickSpacing(1);
        AI.setPaintLabels(true);
        
        aiMenu.add(AI);
        
        menuBar.add(gameMenu);
        menuBar.add(aiMenu);
        
        setJMenuBar(menuBar);
        
        center.addContainerListener(new ContainerAdapter() {
            //game size
            int size = Math.min(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height) / 2 / SIZE * SIZE;
            
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener(game);
                
                game.setPreferredSize(new Dimension(size, size));
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
        
        undoPanel.add(UNDO);
        hintPanel.add(HINT);
        
        south.add(undoPanel);
        south.add(GAME_OVER);
        south.add(hintPanel);
        
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
