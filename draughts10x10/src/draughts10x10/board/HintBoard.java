package draughts10x10.board;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Set;

/**
 * HintBoard
 * 
 * selected (mousePressed)
 * keySet (moveable)
 * 
 * @author Naardeze
*/

final public class HintBoard extends Board {
    //not selected (show moveable)
    final public static int NONE = -1;
    
    //square color
    final private static Color ORANGE = Color.orange;
    
    //hint on/off
    private static boolean hintOn;

    //selected piece
    private int selected;
    //moveable pieces
    private Set<Integer> keySet;
    
    public HintBoard(Rectangle[] square, boolean hintOn) {
        super(square);
        
        HintBoard.hintOn = hintOn;
        
        setForeground(ORANGE);
        setVisible(false);
    }
    
    //<-selected piece
    public int getSelected() {
        return selected;
    }
    
    //select piece
    public void setSelected(int selected) {
        this.selected = selected;
    }
    
    //get player move
    public void setBoard(Set<Integer> keySet) {
        selected = NONE;
        this.keySet = keySet;
        
        setVisible(true);
    }

    //hint on/off
    public static void setHintOn(boolean hintOn) {
        HintBoard.hintOn = hintOn;
    }
    
    //paint selected piece or moveable pieces
    @Override
    public void paint(Graphics g) {
        if (selected != NONE) {
            paintSquare(g, square[selected]);
        } else if (hintOn) {
            keySet.forEach(index -> paintSquare(g, square[index]));
        }
    }
    
}
