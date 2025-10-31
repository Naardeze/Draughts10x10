package draughts;

import static draughts.Board10x10.paintTile;
import static draughts.Draughts.BOARD10X10;
import static draughts.Draughts.HINT;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Set;


/**
 * HintBoard
 *
 * Color moveable or from.
 * 
 * from (mousePressed)
 * keySet (moveable)
 * 
 * @author Naardeze
*/

final class HintBoard extends Component {
    final static int NONE = -1;//no piece from
    
    final private static Color ORANGE = Color.orange;

    private int from;
    private Set<Integer> keySet;
    
    HintBoard() {
        setForeground(ORANGE);
        setVisible(false);
    }
    
    int getFrom() {
        return from;
    }
    
    void setFrom(int from) {
        this.from = from;
    }
    
    void setKeySet(Set<Integer> keySet) {
        from = NONE;
        this.keySet = keySet;
        
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        if (from != NONE) {
            paintTile(g, BOARD10X10.tile[from]);
        } else if (HINT.isSelected()) {
            keySet.forEach(hint -> paintTile(g, BOARD10X10.tile[hint]));
        }
    }
    
}
