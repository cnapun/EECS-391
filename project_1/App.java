//Nikil Pancha (nrp49)

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Class containing the command line interface to manipulate an 8-puzzle
 */
public class App {

    /**
     * For some reason I was unable pipe commands from a text file into this, but if the file is given as an argument, it will be read properly
     *
     * @param args can be empty, or filename of file containing one command per line
     */
    public static void main(String[] args) {
        Board b = new Board();
        if (args.length == 0) { //no file is given
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String s;
                try {
                    s = br.readLine(); //read line from System.in
                    executeCommand(s, b); //execute the command on the board
                } catch (Exception e) {
                }
            }
        } else { //read commands from a file
            Path path = Paths.get(args[0]);
            try {
                Files.lines(path).forEach(s -> {
                    System.out.println(s);
                    executeCommand(s, b);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Executes a given command on a given board, and either succeeds or prints an appropriate error message
     *
     * @param s command to execute
     * @param b board to execute command on
     */
    public static void executeCommand(String s, Board b) {
        try {
            String[] commands = s.split(" ");
            String command = "";
            String extra = "";
            //simplifies things because a few commands have a single argument
            if (commands.length == 2) {
                command = commands[0];
                extra = commands[1];
            }
            if (commands[0].equals("printState")) {
                System.out.println(b);
            } else if (commands[0].equals("setState")) {
                if (commands.length != 4) {
                    System.out.println("Please enter a valid board ");
                } else {
                    try {
                        b.setPieces(commands[1], commands[2], commands[3]);
                    } catch (NumberFormatException e) {
                        System.out.println("Not a valid board");
                    }
                }
            } else if (command.equals("randomizeState")) {
                try {
                    int n = Integer.valueOf(extra);
                    if (n >= 0) {
                        b.scramble(n);
                    } else {
                        System.out.println("Please choose a positive number");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number");
                }
            } else if (command.equals("move")) {
                if (Arrays.asList("up", "down", "left", "right").contains(extra)) {
                    if (b.possibleMoves().contains(extra.charAt(0))) {
                        b.getClass().getMethod(extra).invoke(b);
                    } else {
                        System.out.println("Please enter a valid move");
                    }

                } else {
                    System.out.println("Please enter a valid move");
                }
            } else if (commands[0].equals("solve")) {
                if (commands.length != 3) {
                    System.out.println("Please enter required arguments");
                } else if (commands[1].equals("A-star")) {
                    if (commands[2].equals("h1") || commands[2].equals("h2")) {
                        String soln = Solver.aStar(commands[2], b);
                        System.out.println(soln);
                    } else {
                        System.out.println("Please enter a valid heuristic");
                    }
                } else if (commands[1].equals("beam")) {
                    try {
                        int k = Integer.parseInt(commands[2]);
                        if (k <= 0) {
                            throw new NumberFormatException("Beam width must be positive");
                        } else {
                            System.out.println(Solver.beam(k, b));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a positive integer");
                    }
                } else {
                    System.out.println("Please enter a valid search method");
                }
            } else if (commands[0].equals("maxNodes")) {
                if (commands.length == 2) {
                    try {
                        int maxnodes = Integer.parseInt(commands[1]);
                        if (maxnodes < 0) System.out.println("Please enter a valid number");
                        else b.setMaxNodes(maxnodes);
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number");
                    }
                } else {
                    System.out.println("Wrong number of arguments");
                }
            } else {
                System.out.println("You did not enter a valid command.  Please try again");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
