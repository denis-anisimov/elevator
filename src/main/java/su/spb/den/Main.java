package su.spb.den;

import java.util.Scanner;
import java.util.stream.IntStream;

public class Main {

    private static final String STOP_COMMAND = "quit";
    private static final String INDENT = "  ";

    public static void main(String[] args) {
        Parameters params = new Parameters(args);
        if (!params.isValid()) {
            return;
        }
        showInvitation(true);

        ElevatorManager manager = new ElevatorManager(params);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {

                System.out.print("> ");
                System.out.flush();

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (STOP_COMMAND.equals(line)) {
                    break;
                }

                Boolean isOutside = null;
                if (Character.toLowerCase(line.charAt(0)) == 'o') {
                    isOutside = true;
                } else if (Character.toLowerCase(line.charAt(0)) == 'i') {
                    isOutside = false;
                }

                Integer floor = parseFloorNumber(line.substring(1));

                if (isOutside == null || floor == null) {
                    System.err.println("Unexpected command: " + line);
                    System.err.flush();
                    showInvitation(false);
                } else if (isOutside) {
                    manager.callOutside(floor);
                } else {
                    manager.callInside(floor);
                }
            }
            scanner.close();
        } finally {
            manager.shutdown();
        }
    }

    private static Integer parseFloorNumber(String line) {
        String input = line.trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void showInvitation(boolean complete) {
        if (complete) {
            System.out.println("Enter a command.");
            indent(1);
        }
        System.out.println("Available commands are:");
        indent(2);
        System.out.println(
                "o N , where the N is a floor number. Request an elevator to the N flour outside of the elevator");
        indent(2);
        System.out.println(
                "i N , where the N is a floor number. Request an elevator to the N flour inside of the elevator");
        indent(2);
        System.out.println("quit or Ctrl^C to exit");
        System.out.flush();
    }

    private static void indent(int i) {
        IntStream.range(0, i).forEach(indx -> System.out.print(INDENT));
    }

}
