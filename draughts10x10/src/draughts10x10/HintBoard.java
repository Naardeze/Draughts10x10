package draughts10x10;

import static draughts10x10.Draughts10x10.HINT;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.SquareBoard.paintSquare;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Set;

/**
 * HintBoard
 * 
 * selected
 * hint
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    //not selected
    final static int NOT_SELECTED = -1;
    
    private int selected;
    private Set<Integer> hint;
    
    HintBoard() {
        setForeground(Color.orange);//square color
        setVisible(false);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    void setHint(Set<Integer> hint) {
        selected = NOT_SELECTED;
        
        this.hint = hint;
        
        setVisible(true);
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, SQUAREBOARD.square[selected]);
        } else if (HINT.isSelected()) {
            hint.forEach(index -> paintSquare(g, SQUAREBOARD.square[index]));
        }
    }
    
}
