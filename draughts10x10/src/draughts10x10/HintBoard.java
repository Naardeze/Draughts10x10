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
    final static int NONE = -1;
    
    //square color
    final private static Color ORANGE = Color.orange;

    //selected piece
    private int selected;
    //moveable pieces
    private Set<Integer> keySet;
    
    HintBoard() {
        setForeground(ORANGE);
        setVisible(false);
    }
    
    //<-selected piece
    int getSelected() {
        return selected;
    }
    
    //select piece
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    //get player move
    void setBoard(Set<Integer> keySet) {
        selected = NONE;
        this.keySet = keySet;
        
        setVisible(true);
    }

    //paint selected or moveable pieces
    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, SQUAREBOARD.square[selected]);
        } else if (HINT.isSelected()) {
            keySet.forEach(index -> paintSquare(g, SQUAREBOARD.square[index]));
        }
    }
    
}
