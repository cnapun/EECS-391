//Nikil Pancha (nrp49)
import java.util.*;

/**
 * Class to represent an 8-puzzle's state
 */
public class Board {
    //List that holds the positions
    private List<Integer> pieces;
    //stores all previous moves made
    private String prevMoves = "";
    //stores the total pathcost up ot this point
    private int pathCost = 0;
    //stores the maximum number of nodes to be visited by the search
    private int maxNodes = 10000;
    //random number generator
    private final Random RANDOM = new Random(12341234L);

    //names of pieces
    private static final List<String> NAMES = Arrays.asList("b", "1", "2", "3", "4", "5", "6", "7", "8");
    private static final List<Integer> GOALROWS = Arrays.asList(0, 0, 0, 1, 1, 1, 2, 2, 2);
    private static final List<Integer> GOALCOLS = Arrays.asList(0, 1, 2, 0, 1, 2, 0, 1, 2);
    //desired final positions
    public static final List<Integer> GOALPOSITIONS = Arrays.asList(0b1, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000, 0b10000000, 0b100000000);

    /**
     * Constructor to initialize a board in the solved state
     */
    public Board() {
        int blank = 0b1;
        int one = 0b10;
        int two = 0b100;
        int three = 0b1000;
        int four = 0b10000;
        int five = 0b100000;
        int six = 0b1000000;
        int seven = 0b10000000;
        int eight = 0b100000000;
        pieces = Arrays.asList(blank, one, two, three, four, five, six, seven, eight);
    }

    /**
     * Constructor to initialize a board with a specified state
     *
     * @param pieces    state to initialize tile positions to
     * @param pathCost  current path cost of new board
     * @param prevMoves previous moves to be stored
     */
    private Board(List<Integer> pieces, int pathCost, String prevMoves) {
        this.pieces = pieces;
        this.pathCost = pathCost;
        this.prevMoves = prevMoves;
    }

    /**
     * Move the blank tile up (no error checking)
     */
    public void up() {
        reassign(pieces.get(0) >> 3);
    }

    /**
     * Move the blank tile down (no error checking)
     */
    public void down() {
        reassign(pieces.get(0) << 3);
    }

    /**
     * Move the blank tile left (no error checking)
     */
    public void left() {
        reassign(pieces.get(0) >> 1);
    }

    /**
     * Move the blank tile right (no error checking)
     */
    public void right() {
        reassign(pieces.get(0) << 1);
    }

    /**
     * Get the current column of a piece
     *
     * @param piece bitboard to find column of
     * @return 0 if in left column, 1 if in middle, 2 if in right
     */
    private static int column(int piece) {
        if ((piece & 0b100100100) != 0) return 2;
        else if ((piece & 0b10010010) != 0) return 1;
        else return 0;
    }

    /**
     * get the current row of a piece
     *
     * @param piece bitboard to find row of
     * @return 0 if in first row, 1 if in 2nd, 2 if in 3rd
     */
    private static int row(int piece) {
        if (piece > 0b100000) return 2;
        else if (piece > 0b100) return 1;
        else return 0;
    }

    /**
     * Compute the sum of the manhattan distance of all tiles on the board (h2)
     *
     * @return value of h2 for the current board
     */
    public int manhattanDistance() {
        int total = 0;
        for (int i = 1; i < pieces.size(); i++) {
            int piece = pieces.get(i);
            total += Math.abs(row(piece) - GOALROWS.get(i));
            total += Math.abs(column(piece) - GOALCOLS.get(i));
        }
        return total;
    }

    /**
     * Compute the number of misplaced tiles (h1)
     *
     * @return value of h1 for the current board
     */
    public int nMisplaced() {
        int total = 0;
        for (int i = 1; i < pieces.size(); i++) {
            if (!GOALPOSITIONS.get(i).equals(pieces.get(i))) {
                total++;
            }
        }
        return total;
    }

    /**
     * @return string representation of board
     */
    public String toString() {
        String[][] state = new String[3][3];
        for (int i = 0; i < pieces.size(); i++) {
            int pos = 31 - Integer.numberOfLeadingZeros(pieces.get(i));
            state[pos / 3][pos % 3] = NAMES.get(i);
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String[] row : state) {
            sb.append(row[0]).append(row[1]).append(row[2]);
            if (++i < 3) sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Swap the blank tile with another piece
     *
     * @param newpos new position of the blank tile
     */
    private void reassign(int newpos) {
        //indexOf would be prettier, but this works
        if (newpos == pieces.get(1)) pieces.set(1, pieces.get(0));
        else if (newpos == pieces.get(2)) pieces.set(2, pieces.get(0));
        else if (newpos == pieces.get(3)) pieces.set(3, pieces.get(0));
        else if (newpos == pieces.get(4)) pieces.set(4, pieces.get(0));
        else if (newpos == pieces.get(5)) pieces.set(5, pieces.get(0));
        else if (newpos == pieces.get(6)) pieces.set(6, pieces.get(0));
        else if (newpos == pieces.get(7)) pieces.set(7, pieces.get(0));
        else if (newpos == pieces.get(8)) pieces.set(8, pieces.get(0));
        pieces.set(0, newpos);
    }

    /**
     * @return the list of bitboards representing the current state of the board
     */
    public List<Integer> getState() {
        return pieces;
    }

    /**
     * @return a list of all possible moves that can be made, as chars
     */
    public List<Character> possibleMoves() {
        List<Character> out = new ArrayList<>(4);
        if ((pieces.get(0) & 0b1001001) == 0) out.add('l');
        if ((pieces.get(0) & 0b100100100) == 0) out.add('r');
        if ((pieces.get(0) & 0b111) == 0) out.add('u');
        if ((pieces.get(0) & 0b111000000) == 0) out.add('d');
        return out;
    }

    /**
     * Apply a series of moves
     *
     * @param scramble String of moves (u,d,l,r) to apply (example: drdrulld)
     */
    public void applyMoves(String scramble) {
        int i = 0;
        for (char s : scramble.toCharArray()) {
            List<Character> permittedMoves = possibleMoves();
            if (!permittedMoves.contains(s)) {
                throw new IllegalArgumentException(s + " is not a valid move at position " + i);
            }
            if (s == 'u') {
                up();
            } else if (s == 'd') {
                down();
            } else if (s == 'l') {
                left();
            } else if (s == 'r') {
                right();
            }
            i++;
        }
    }

    /**
     * Apply n random moves with the only constraint that a move cannot undo the previous move (du will never happen, nor will rl)
     *
     * @param nMoves number of moves to apply
     * @return the sequence of moves that were applied
     */
    public String scramble(int nMoves) {
        StringBuilder sb = new StringBuilder();
        char last = ' ';
        for (int i = 0; i < nMoves; i++) {
            List<Character> choosableMoves = possibleMoves();
            for (int j = 0; j < choosableMoves.size(); j++) {
                if (choosableMoves.get(j) == last)
                    choosableMoves.remove(j);
            }
            int choice = (int) (RANDOM.nextDouble() * choosableMoves.size());
            char move = choosableMoves.get(choice);
            applyMoves(String.valueOf(move));
//            applyMoves(String.valueOf(invertMove(move)));
            sb.append(move);
            last = invertMove(move);
        }
        return sb.toString();
    }

    /**
     * Set the pieces of the board to a specific state
     *
     * @param pieces new list of bitboards
     */
    public void setState(List<Integer> pieces) {
        this.pieces = pieces;
    }

    /**
     * Find the move that would undo the previous move
     *
     * @param move move to undo
     * @return opposite of move.  If move='d', 'u', etc
     */
    public char invertMove(char move) {
        switch (move) {
            case 'u':
                return 'd';
            case 'd':
                return 'u';
            case 'l':
                return 'r';
            case 'r':
                return 'l';
            default:
                return ' ';
        }
    }

    /**
     * @return all possible board arrangements reachable from the current position
     */
    public List<Board> getChildren() {
        List<Character> possibleMoves = possibleMoves();
        List<Board> out = new ArrayList<>(4);
        for (char move : possibleMoves) {
            applyMoves(String.valueOf(move));
            out.add(new Board(new ArrayList<Integer>(getState()), getPathCost() + 1, prevMoves + String.valueOf(move)));
            applyMoves(String.valueOf(invertMove(move)));
        }
        return out;
    }

    /**
     * @return current path cost
     */
    public int getPathCost() {
        return pathCost;
    }

    /**
     * Provides the heuristic used for the beam search, in this case, only the sum of Manhattan distances
     *
     * @return Sum of Manhattan distances for all tiles
     */
    public int beamHeuritic() {
        return manhattanDistance();
    }

    /**
     * @return the maximum number of nodes that may be visited by a search
     */
    public int getMaxNodes() {
        return maxNodes;
    }

    /**
     * Sets maxNodes
     * @param maxNodes the maximum number of nodes that may be visited by a search
     */
    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    /**
     * Takes three strings, one for each row, and sets the position accordingly
     * @param row1 first row
     * @param row2 second row
     * @param row3 third row
     * @throws IllegalArgumentException if an invalid input is give (does not check for the solvability of the state)
     */
    public void setPieces(String row1, String row2, String row3) throws IllegalArgumentException {
        List<Integer> newPieces = new ArrayList<>(9);
        newPieces.addAll(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0));
        int bitboardPosition = 0b1;
        for (String row : Arrays.asList(row1, row2, row3)) {
            if (row.length() != 3) {
                throw new IllegalArgumentException("All rows must have 3 pieces");
            }
            for (char c : row.toCharArray()) {
                int pos;
                if (c == 'b') pos = 0;
                else pos = c - 48;
                if (pos < 0 || pos > 10) {
                    throw new IllegalArgumentException(c + " is not a valid piece");
                } else {
                    if (newPieces.get(pos) != 0) {
                        throw new IllegalArgumentException(c + " is repeated");
                    } else {
                        newPieces.set(pos, bitboardPosition);
                    }
                }
                bitboardPosition <<= 1;
            }
        }

        pieces = newPieces;
    }

    /**
     * @return all of the moves made to get to the current state from the starting state
     */
    public String getPrevMoves() {
        return prevMoves;
    }

    public void setPrevMoves(String prevMoves) {
        this.prevMoves = prevMoves;
    }

    /**
     * Not a proper hashcode function, but it is only used to make sure states are not repeated in beam search, so it does not need to work as it should, and should not in order for the code to function properly
     */
    @Override
    public int hashCode() {
        return getState().hashCode();
    }

    /**
     * Checks for equality of the state with another object
     * @param obj object to check equality with
     * @return true if states are equal, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Board) && ((Board) obj).getState().equals(getState());
    }
}
