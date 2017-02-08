//Nikil Pancha (nrp49)

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class with methods to calculate various statistics about the search methods
 */
public class Experiments {
    public static void main(String[] args) {
        //display proportion of solutions found with different maxnodes for beam
        for (int i = 500; i < 500000; i *= 2)
            System.out.println(i + ":  " + pFound(100, 0.001, i));

    }

    /**
     * Counts the number of inversions (a_i>a_j when i<j) in a list
     *
     * @param list list to count inversions in
     * @return the number of inversions in list
     */
    public static int nInversions(List<Integer> list) {
        int n = 0;
        for (int i = 0; i < list.size(); i++)
            for (int j = i + 1; j < list.size(); j++)
                if (list.get(i) > list.get(j)) n++;
        return n;
    }

    /**
     * Checks if a board is in a valid position
     *
     * @param b board to check validity of
     * @return true if board is in a valid position, false otherwise
     */
    public static boolean validPosition(Board b) {
        return (nInversions(b.getState()) + b.manhattanDistance()) % 2 == 0;
    }

    /**
     * @return all possible arrangements of the board, valid or not
     */
    public static List<List<Integer>> permute() {
        List<Integer> num = Arrays.asList(1, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6, 1 << 7, 1 << 8);
        List<List<Integer>> result = new ArrayList<>();
        result.add(new ArrayList<Integer>());
        for (Integer i : num) {
            List<List<Integer>> current = new ArrayList<>();
            for (List<Integer> l : result) {
                for (int j = 0; j < l.size() + 1; j++) {
                    l.add(j, i);
                    List<Integer> temp = new ArrayList<>(l);
                    current.add(temp);
                    l.remove(j);
                }
            }
            result = new ArrayList<>(current);
        }
        return result;
    }

    /**
     * @return a list of all valid possible boards
     */
    public static List<Board> validStates() {
        List<Board> out = new ArrayList<>();
        for (List<Integer> l : permute()) {
            Board b = new Board();
            b.setState(l);
            if (validPosition(b))
                out.add(b);
        }
        return out;
    }

    /**
     * Samples about 10% of possible states and calculates average nodes visited vs length of solution using A* search
     *
     * @param heuristic heuristic to use to search (h1 or h2)
     * @return List of length 32 with the value at index i being the average number of nodes visited for a solution of that length
     */
    public static List<Integer> nodesVsLength(String heuristic) {
        List<List<Integer>> listOfSolves = new ArrayList<>();
        for (int i = 0; i < 32; i++) listOfSolves.add(new ArrayList<>(Arrays.asList(-1)));
        int j = 0;
        for (Board b : validStates()) {
            if (Math.random() < 0.1) {
                Pair p = Solver.aStarStats(heuristic, b);
                listOfSolves.get(p.s.length()).add(p.i);
                if (++j % 1000 == 0) System.out.println(j);
            }
        }
        List<Integer> out = new ArrayList<>();
        //average by length
        for (List<Integer> l : listOfSolves) {
            if (l.size() > 0) {
                int sum = 0;
                for (Integer i : l) sum += i;
                out.add((int) (sum * 1.0 / l.size()));
            } else {
                out.add(-1);
            }
        }
        return out;
    }

    public static List<Integer> beamNodesVsLength() {
        List<List<Integer>> listOfSolves = new ArrayList<>();
        for (int i = 0; i < 300; i++) listOfSolves.add(new ArrayList<>());
        int j = 0;
        for (Board b : validStates()) {
            if (Math.random() < 0.1) {
                b.setMaxNodes(10000);
                Pair p = Solver.beamStats(100, b);
                listOfSolves.get(p.s.length()).add(p.i);
                if (++j % 1000 == 0) {
                    System.out.println(j);
//                    System.out.println(p.i);
                }
            }
        }
        List<Integer> out = new ArrayList<>();
        for (List<Integer> l : listOfSolves) {
            if (l.size() > 0) {
                int sum = 0;
                for (Integer i : l) sum += i;
                out.add((int) (sum * 1.0 / l.size()));
            } else {
                out.add(-1);
            }
        }
        return out;
    }

    /**
     * Class to easily represent a solution along with the number of nodes visited
     */
    public static class Pair {
        public String s;
        public int i;

        public Pair(String s, int i) {
            this.s = s;
            this.i = i;
        }
    }

    /**
     * Sample about specified proportion of all possible board states
     *
     * @param prop approximate proportion to sample
     * @return a list of boards randomly selected from the starting state
     */
    public static List<Board> sampleBoards(double prop) {
        List<Board> out = new ArrayList<>();
        for (Board b : validStates()) {
            if (Math.random() < prop) out.add(b);
        }
        return out;
    }

    /**
     * Calculate the proportion of solutions found using beam search
     *
     * @param k        beam width
     * @param prop     proportion to sample
     * @param maxNodes maximum nodes to explore
     * @return
     */
    public static double pFound(int k, double prop, int maxNodes) {
        int found = 0;
        int total = 0;
        for (Board b : sampleBoards(prop)) {
            b.setMaxNodes(maxNodes);
            Pair p = Solver.beamStats(k, b);
            if (p.i != -1) found++;
            ++total;
        }
        return found * 1.0 / total;
    }

    /**
     * Method to verify that the beam search finds the correct solution
     *
     * @return true if all solutions are correct, false if at least one is not
     */
    public static boolean testBeam() {
        List<Board> l = sampleBoards(0.1);
        for (int i = 0; i < 100; i++) {
            l.get(i).setMaxNodes(20000);
            Pair p = Solver.beamStats(250, l.get(i));
            if (p.i != -1) {
                l.get(i).applyMoves(p.s);
                if (!l.get(i).getState().equals(Board.GOALPOSITIONS)) return false;
            }
        }
        return true;
    }
}
