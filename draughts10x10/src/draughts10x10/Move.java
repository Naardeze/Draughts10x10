package draughts10x10;

import java.util.ArrayList;

/**
 * Move
 * 
 * captures (captured pieces)
 * to (destination)
 * 
 * @author Naardeze
 */

public class Move {
    final private ArrayList<Integer> captures;
    final private int to;
    
    Move(int to) {
        this(new ArrayList(), to);
    }
    
    Move(ArrayList<Integer> captures, int to) {
        this.captures = captures;
        this.to = to;
    }
    
    //<-captured pieces
    public ArrayList<Integer> getCaptures() {
        return captures;
    }
    
    //<-destination
    public int getTo() {
        return to;
    }
    
    //<-<<captures>, to>
    public ArrayList<Integer> getBoardMove() {
        ArrayList<Integer> boardMove = new ArrayList(captures);
        
        boardMove.add(to);
        
        return boardMove;
    }
    public ArrayList<Integer> getBoardMove(int from) {
        ArrayList<Integer> boardMove = new ArrayList(captures);
        
        boardMove.add(0, from);
        boardMove.add(to);
        
        return boardMove;
    }
    
}
