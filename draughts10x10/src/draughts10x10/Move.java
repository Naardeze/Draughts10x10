package draughts10x10;

import java.util.ArrayList;

/**
 * Move
 * 
 * captures
 * to
 * 
 * -pieceMove (from) -> used in animation
 * 
 * @author Naardeze
 */

final class Move {
    //captured pieces
    final private ArrayList<Integer> captures;
    //destination
    final private int to;
    
    Move(int to) {
        this(new ArrayList(), to);
    }
    
    Move(ArrayList<Integer> captures, int to) {
        this.captures = captures;
        this.to = to;
    }
    
    ArrayList<Integer> getCaptures() {
        return captures;
    }
    
    int getTo() {
        return to;
    }   
    
    //used in animation
    //<from, <captures>, to>
    ArrayList<Integer> getPieceMove(int from) {
        ArrayList<Integer> pieceMove = new ArrayList(captures);
        
        pieceMove.add(0, from);
        pieceMove.add(to);
        
        return pieceMove;
    }
    
}
