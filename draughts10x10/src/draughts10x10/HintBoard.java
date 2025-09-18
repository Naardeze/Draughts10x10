package draughts10x10;

import static draughts10x10.Draughts10x10.HINT;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;
import static draughts10x10.Draughts10x10.SQUAREBOARD;

/* HintBoard
 *
 * select (pressed)
 * hint (moveable)
*/

final class HintBoard extends Board {
    //highlight hint
    final static int NOT_SELECTED = -1;
    
    //selected, hint color
    final private static Color ORANGE = Color.orange;
    
    private int selected;
    private Set<Integer> hint;
    
    HintBoard() {
        super(SQUAREBOARD.square);

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
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, square[selected]);
        } else if (HINT.isSelected()) {
            hint.forEach(index -> paintSquare(g, square[index]));
        }
    }
    
}
