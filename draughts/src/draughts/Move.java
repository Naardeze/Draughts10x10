package draughts;

import java.util.ArrayList;

/*
    class stores move captures and destination
*/

public class Move {
    final private ArrayList<Integer> captures;
    final private int destination;
    
    public Move(int to) {
        this(new ArrayList(), to);
    }
    
    public Move(ArrayList<Integer> captures, int to) {
        this.captures = captures;
        this.destination = to;
    }
    
    public ArrayList<Integer> getCaptures() {
        return captures;
    }
    
    public int getDestination() {
        return destination;
    }   
    
    //used in animation
    public ArrayList<Integer> getPieceMove(int from) {
        ArrayList<Integer> move = new ArrayList(captures);
        
        move.add(0, from);
        move.add(destination);
        
        return move;
    }
    
}
