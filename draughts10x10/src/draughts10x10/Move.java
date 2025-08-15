package draughts10x10;

import java.util.ArrayList;

/**
 * captures and destination
 * 
 * board move -> animation
 * 
 * @author Naardeze
 */

public class Move {
    final private ArrayList<Integer> captures;
    final private int destination;
    
    Move(int destination) {
        this(new ArrayList(), destination);
    }
    
    Move(ArrayList<Integer> captures, int destination) {
        this.captures = captures;
        this.destination = destination;
    }
    
    public ArrayList<Integer> getCaptures() {
        return captures;
    }
    
    public int getDestination() {
        return destination;
    }   
    
    //used in animation
    //<from, <captures>, destination>
    public ArrayList<Integer> getBoardMove(int from) {
        ArrayList<Integer> boardMove = new ArrayList(captures);
        
        boardMove.add(0, from);
        boardMove.add(destination);
        
        return boardMove;
    }
    
}
