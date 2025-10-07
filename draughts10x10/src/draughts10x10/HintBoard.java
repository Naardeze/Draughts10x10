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
 * Board for player move (mouse). 
 * Colors moveable (hint) or pressed (selected) piece(s)
 * 
 * selected
 * hint
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    //not selected (show moveable)
    final static int NOT_SELECTED = -1;

    private int selected = NOT_SELECTED;
    private Set<Integer> keySet;
    
    HintBoard(Set<Integer> keySet) {
        this.keySet = keySet;

        setForeground(Color.orange);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, SQUAREBOARD.square[selected]);
        } else if (HINT.isSelected()) {
            keySet.forEach(index -> paintSquare(g, SQUAREBOARD.square[index]));
        }
    }
    
}
