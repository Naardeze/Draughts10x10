package draughts;

import static board.HintBoard.HINT;
import static board.SquareBoard.SIZE;
import static draughts.Game.GAME_OVER;
import static draughts.Game.UNDO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class Draughts extends JFrame {
    //colors
    final public static int WHITE = 0;
    final public static int BLACK = 1;
    
    //ai search depth
    final private static int MIN_DEPTH = 1;
    final private static int MAX_DEPTH = 5;
    
    final public static JSlider AI = new JSlider(MIN_DEPTH, MAX_DEPTH);
    
    private Game game = new Game(WHITE);
    
    private Draughts() {
        super("Draughts10x10");
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bk.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu gameMenu = new JMenu("Game");
        JMenu aiMenu = new JMenu("AI");
        
        JPanel center = new JPanel();
        JPanel south = new JPanel(new GridLayout(1, 3));
        
        JPanel parent1 = new JPanel();
        JPanel parent2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        for (int color : new int[] {WHITE, BLACK}) {
            gameMenu.add(new String[] {"White", "Black"}[color]).addActionListener(e -> {
                center.remove(game);
                game = new Game(color);
                center.add(game);
            });
        }
        
        AI.setMajorTickSpacing(1);
        AI.setPaintLabels(true);
        
        aiMenu.add(AI);
        
        menuBar.add(gameMenu);
        menuBar.add(aiMenu);
        
        setJMenuBar(menuBar);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        
        center.addContainerListener(new ContainerAdapter() {
            //game size
            int dimension = Math.min(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height) / 3 * 2 / SIZE * SIZE;
            
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener(game);
                
                game.setPreferredSize(new Dimension(dimension, dimension));
                center.validate();
            }
            @Override
            public void componentRemoved(ContainerEvent e) {
                UNDO.removeActionListener(game);
            }
        });
        center.add(game);
        
        UNDO.setFocusable(false);
        
        HINT.setHorizontalTextPosition(JCheckBox.LEFT);
        HINT.setFocusable(false);
        HINT.addActionListener(e-> game.repaint());
        
        parent1.add(UNDO);
        parent2.add(HINT);
        
        south.add(parent1);
        south.add(GAME_OVER);
        south.add(parent2);
        
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        new Draughts();
    }
    
}
