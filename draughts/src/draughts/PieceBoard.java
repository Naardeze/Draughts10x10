package draughts;

import static draughts.Draughts10x10.TILEBOARD;
import static draughts.TileBoard.GRID;
import static draughts.TileBoard.paintTile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;

/**
 * PieceBoard
 * 
 * Board with pieces (begin position by default) and highlighted move tiles.
 *
 * board (begin)
 * move (yellow, green)
 * 
 * @author Naardeze
 */

final class PieceBoard extends JComponent {
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    final static char EMPTY = '_';

    final static String WB = W + "" + B;//"wb"

    final static Image[][] PIECE = new Image[2][WB.length()];//[wb][WB]

    final private static Color[] MOVE = {Color.yellow, Color.green};//{capture, piece}

    private char[] board = new char[TILEBOARD.tile.length];//50
    private ArrayList<Integer> move = new ArrayList();
    
    PieceBoard() {
        //begin position
        Arrays.fill(board, 0, board.length / 2 - GRID / 2, B);//0-19 'b'
        Arrays.fill(board, board.length / 2 - GRID / 2, board.length / 2 + GRID / 2, EMPTY);//20-29 '_'
        Arrays.fill(board, board.length / 2 + GRID / 2, board.length, W);//30-49 'w'
    }

    char getIndex(int index) {
        return board[index];
    }
    
    void setIndex(int index, char piece) {
        board[index] = piece;
    }
    
    String getBoard() {
        return String.valueOf(board);
    }
    
    void setBoard(String board) {
        this.board = board.toCharArray();
    }
    
    ArrayList<Integer> getMove() {
        return move;
    }
            
    void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        for (int index : move) {
            g.setColor(MOVE[(move.indexOf(index) + 1) / move.size()]);//[0..0, 1]
            
            paintTile(g, TILEBOARD.tile[index]);
        }

        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                g.drawImage(PIECE[(Character.toLowerCase(board[i]) + "" + Character.toUpperCase(board[i])).indexOf(board[i])][WB.indexOf(Character.toLowerCase(board[i]))], TILEBOARD.tile[i].x, TILEBOARD.tile[i].y, this);
            }
        }
    }
    
}

