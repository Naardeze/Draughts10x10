package draughts;

import static draughts.Draughts10x10.HINT;
import static draughts.Draughts10x10.TILEBOARD;
import static draughts.TileBoard.paintTile;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Set;

/**
 * HintBoard
 * 
 * Board coloring moveable or selected (by mouse) player piece(s).
 * 
 * selected (mousePressed)
 * keySet (moveable)
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    final static int NONE = -1;//no piece selected
    
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
    
    void setKeySet(Set<Integer> keySet) {
        selected = NONE;
        this.keySet = keySet;
        
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintTile(g, TILEBOARD.tile[selected]);
        } else if (HINT.isSelected()) {
            keySet.forEach(index -> paintTile(g, TILEBOARD.tile[index]));
        }
    }
    
}
