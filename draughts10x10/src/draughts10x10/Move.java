package draughts10x10;

import java.util.ArrayList;

/*
    class stores move captures and destination
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
    
    public ArrayList<Integer> getCaptures() {
        return captures;
    }
    
    public int getTo() {
        return to;
    }   
    
    //used in animation
    public ArrayList<Integer> getBoardMove(int from) {
        ArrayList<Integer> boardMove = new ArrayList(captures);
        
        boardMove.add(0, from);
        boardMove.add(to);
        
        return boardMove;
    }
    
}
