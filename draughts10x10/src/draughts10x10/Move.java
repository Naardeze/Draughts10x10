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
    final private ArrayList<Integer> captures;
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
        ArrayList<Integer> boardMove = new ArrayList(captures);
        
        boardMove.add(0, from);
        boardMove.add(to);
        
        return boardMove;
    }
    
}
