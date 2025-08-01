package draughts10x10.board;

import static draughts10x10.Draughts10x10.HINT;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Set;

/*
    board for mouse move and select or hint piece(s)
*/

public class HintBoard extends AbstractBoard {
    //not selected
    final public static int NONE = -1;
    
    //selected & hint color
    final private static Color ORANGE = Color.orange;

    private int selected = NONE;
    private Set<Integer> hint;
    
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
    
    public void setHint(Set<Integer> hint) {
        this.hint = hint;
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, square[selected]);
        } else if (HINT.isSelected()) {//not the nicest, but the simplest way
            hint.forEach(hint -> paintSquare(g, square[hint]));
        }
    }
    
}
