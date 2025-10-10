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
    final public static int NOT_SELECTED = -1;
    
    //hint color
    final private static Color ORANGE = Color.orange;
    
    private static boolean hintOn;

    private int selected;
    private Set<Integer> keySet;
    
    public HintBoard(Rectangle[] square, boolean hintOn) {
        super(square);
        
        HintBoard.hintOn = hintOn;
        
        setForeground(ORANGE);
        setVisible(false);
    }
    
    public int getSelected() {
        return selected;
    }
    
    public void setSelected(int selected) {
        this.selected = selected;
    }
    
    //get player move
    public void setBoard(Set<Integer> keySet) {
        selected = NOT_SELECTED;
        this.keySet = keySet;
        
        setVisible(true);
    }
    
    public static void setHintOn(boolean hintOn) {
        HintBoard.hintOn = hintOn;
    }
    
    //paint board
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, square[selected]);
        } else if (hintOn) {
            keySet.forEach(index -> paintSquare(g, square[index]));
        }
    }
    
}
