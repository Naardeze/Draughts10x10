package board;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Set;
import javax.swing.JCheckBox;

/*
    board used for manual player move
    shows selected or selectable pieces
*/

public class HintBoard extends AbstractBoard {
    //hint on/off
    final public static JCheckBox HINT = new JCheckBox("Hint");
    
//not selected
    final public static int NONE = -1;

    //color
    final private static Color ORANGE = Color.orange;
    
    private int selected = NONE;
    private Set<Integer> keySet;
    
    public HintBoard(Rectangle[] square) {
        super(square);
        
        setOpaque(false);
        setForeground(ORANGE);
        setVisible(false);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                selected = NONE;
            }
        });
    }
    
    public int getSelected() {
        return selected;
    }
    
    public void setSelected(int selected) {
        this.selected = selected;
    }
    
    public void setKeySet(Set<Integer> keySet) {
        this.keySet = keySet;
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, square[selected]);
        } else if (HINT.isSelected()) {
            keySet.forEach(index -> paintSquare(g, square[index]));
        }
    }
    
}
