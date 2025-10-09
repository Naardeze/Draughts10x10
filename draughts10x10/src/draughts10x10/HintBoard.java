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
 * selected (mousePressed)
 * keySet (moveable)
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    //not selected (show moveable)
    final static int NOT_SELECTED = -1;
    
    //square color
    final private static Color ORANGE = Color.orange;

    private int selected;
    private Set<Integer> keySet;
    
    HintBoard() {
        setVisible(false);
        setForeground(ORANGE);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    //get player move
    void setBoard(Set<Integer> keySet) {
        selected = NOT_SELECTED;
        this.keySet = keySet;
        
        setVisible(true);
    }
    
    //paint board
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, SQUAREBOARD.square[selected]);
        } else if (HINT.isSelected()) {
            keySet.forEach(index -> paintSquare(g, SQUAREBOARD.square[index]));
        }
    }
    
}
