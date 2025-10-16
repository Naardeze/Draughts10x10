package draughts;

import static draughts.Draughts10x10.HINT;
import static draughts.Draughts10x10.SQUAREBOARD;
import static draughts.SquareBoard.paintSquare;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Set;

/**
 * HintBoard
 * 
 * selected (mousePressed)
 * keySet (moveable)
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    //not selected (show moveable)
    final static int NONE = -1;
    
    //square color
    final private static Color ORANGE = Color.orange;

    private int selected;
    private Set<Integer> keySet;
    
    HintBoard() {
        setForeground(ORANGE);
        setVisible(false);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    //get player move
    void setBoard(Set<Integer> keySet) {
        selected = NONE;
        this.keySet = keySet;
        
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, SQUAREBOARD.square[selected]);
        } else if (HINT.isSelected()) {
            keySet.forEach(index -> paintSquare(g, SQUAREBOARD.square[index]));
        }
    }
    
}
