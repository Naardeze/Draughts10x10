package draughts10x10;

import static draughts10x10.Draughts10x10.HINT;
import static draughts10x10.Board10x10.paintSquare;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Set;
import static draughts10x10.Draughts10x10.BOARD10X10;

/**
 * HintBoard
 * 
 * selected
 * hint
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    //not selected (show moveable)
    final static int NOT_SELECTED = -1;

    //pressed piece
    private int selected;
    //moveable pieces
    private Set<Integer> hint;
    
    HintBoard() {
        //not visible by default
        setVisible(false);
        //square color
        setForeground(Color.orange);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    //prepare player input
    void setHint(Set<Integer> hint) {
        selected = NOT_SELECTED;
        
        this.hint = hint;
        
        setVisible(true);
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, BOARD10X10.square[selected]);
        } else if (HINT.isSelected()) {
            hint.forEach(hint -> paintSquare(g, BOARD10X10.square[hint]));
        }
    }
    
}
