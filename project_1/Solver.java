//Nikil Pancha (nrp49)

import java.lang.reflect.Method;
import java.util.*;

/**
 * Class to solve the 8-puzzle with various methods
 */
public class Solver {
    /**
     * Solves a board using A* with a specified heuristic
     *
     * @param heuristicType either h1 or h2.  h1 is number of misplaced tiles, h2 is sum of Manhattan distance of all tiles from goal state
     * @param start         Board to start search from
     * @return The string of moves to reach the solved state from start, or "No solution was found" if no solution is
     * @throws IllegalArgumentException if heuristicType is not h1 or h2
     */
    public static String aStar(String heuristicType, Board start) throws IllegalArgumentException {
        Experiments.Pair p = aStarStats(heuristicType, start);
        if (p.i == -1) return "No Solution was Found";
        else return p.s;
    }


    /**
     * Solves a board using A* with a specified heuristic
     *
     * @param heuristicType either h1 or h2.  h1 is number of misplaced tiles, h2 is sum of Manhattan distance of all tiles from goal state
     * @param start         Board to start search from
     * @return The result of the search and the number of nodes visited, or and empty string and -1 if no solution was found
     * @throws IllegalArgumentException if heuristicType is not h1 or h2
     */
    public static Experiments.Pair aStarStats(String heuristicType, Board start) throws IllegalArgumentException {
        Method heuristic;

        //get the method used for the heuristic function
        try {
            heuristic = heuristicType.equals("h1") ? Board.class.getMethod("nMisplaced") : Board.class.getMethod("manhattanDistance");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(heuristicType + " is not a valid heuristic");
        }

        //Queue to store nodes that have yet to be visited
        PriorityQueue<Board> queue = new PriorityQueue<Board>((b1, b2) -> {
            try {
                return ((int) heuristic.invoke(b1) + b1.getPathCost()) - ((int) heuristic.invoke(b2) + b2.getPathCost());
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });

        queue.add(start); //queue of open nodes
        Set<List<Integer>> visited = new HashSet<>(); //set of closed nodes
        Board goalBoard = new Board();

        boolean found = false;
        int nVisited = 0;
        int maxNodes = start.getMaxNodes();
        //search until queue is empty, solution is found, or maxNodes is exceeded
        while (!found && nVisited < maxNodes && queue.size() > 0) {
            Board current = queue.poll();
            //goal test
            if (current.getState().equals(Board.GOALPOSITIONS)) {
                found = true;
                goalBoard = current;
            }

            visited.add(current.getState());
            nVisited++;

            //Add nodes to queue if they have not already been explored
            for (Board child : current.getChildren()) {
                if (!visited.contains(child.getState())) {
                    queue.add(child);
                }
            }
        }

        if (!found) {
            return new Experiments.Pair("", -1);
        } else {
            return new Experiments.Pair(goalBoard.getPrevMoves(), nVisited);
        }
    }

    /**
     * Solves a board using a beam search
     *
     * @param k     beam width
     * @param start board to start search from
     * @return The string of moves to reach the solved state from start, or "No solution was found" if no solution is found
     */
    public static String beam(int k, Board start) {
        Experiments.Pair p = beamStats(k, start);
        if (p.i == -1) return "No solution was found";
        else return p.s;
    }

    /**
     * Solves a board using a beam search
     *
     * @param k     beam width
     * @param start board to start search from
     * @return The result of the search and the number of nodes visited, or an empty string and -1 if no solution was found
     */
    public static Experiments.Pair beamStats(int k, Board start) {
        boolean found = false;
        //the goal is the solved state
        Board goalBoard = new Board();
        List<Board> kStates = new ArrayList<>();
        //initialize with k random scrambles from the start state
        for (int i = 0; i < k - 1; i++) {
            Board f = new Board();
            f.setState(new ArrayList<>(start.getState()));
            String s = f.scramble(15);
            f.setPrevMoves(s);
            kStates.add(f);
        }

        //add the starting state in case it is very close to the solution
        kStates.add(start);

        int maxNodes = start.getMaxNodes(); //maximum number of nodes to visit
        int nVisited = 0; //number of nodes visited


        while (!found && nVisited < maxNodes) {
            Set<Board> children = new HashSet<>(); //set to guarantee uniqueness of of new k States chosen
            //add all children to set
            for (Board parent : kStates) {
                if (parent.getState().equals(Board.GOALPOSITIONS)) {
                    found = true;
                    goalBoard = parent;
                    break;
                }
                children.addAll(parent.getChildren());
                nVisited++;
            }

            //sort the list and truncate if length is more than k
            List<Board> childrenList = new ArrayList<>(children);
            childrenList.sort((b1, b2) -> b1.beamHeuritic() - b2.beamHeuritic());
            if (childrenList.size() > k) {
                childrenList = childrenList.subList(0, k);
            }
            kStates = childrenList;
        }

        if (!found) {
            return new Experiments.Pair("", -1);
        } else {
            return new Experiments.Pair(goalBoard.getPrevMoves(), nVisited);
        }
    }
}
