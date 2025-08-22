package draughts10x10;

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

final class HintBoard extends AbstractBoard {
    //not selected
    final static int NONE = -1;
    
    //selected & hint color
    final private static Color ORANGE = Color.orange;
    
    private int selected;
    private Set<Integer> hint;
    
    HintBoard(Rectangle[] square) {
        super(square);

        setForeground(ORANGE);
        setVisible(false);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    void setHint(Set<Integer> hint) {
        this.hint = hint;
        
        selected = NONE;
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, square[selected]);
        } else if (HINT.isSelected()) {//not the nicest but the simplest way
            hint.forEach(hint -> paintSquare(g, square[hint]));
        }
    }
    
}
