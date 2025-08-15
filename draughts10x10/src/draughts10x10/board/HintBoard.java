package draughts10x10.board;

import static draughts10x10.Draughts10x10.HINT;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Set;

/**
 * mouse move
 * select, hint
 * 
 * @author Naardeze
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
        
        setForeground(ORANGE);
        setVisible(false);
    }
    
    public int getSelected() {
        return selected;
    }
    
    public void setSelected(int selected) {
        this.selected = selected;
    }
    
    public void setHint(Set<Integer> hint) {
        this.hint = hint;
        
        selected = NONE;
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, square[selected]);
        } else if (HINT.isSelected()) {
            hint.forEach(hint -> paintSquare(g, square[hint]));
        }
    }
    
}
