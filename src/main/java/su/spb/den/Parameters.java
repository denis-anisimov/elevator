package su.spb.den;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

class Parameters {

    /**
     * The number of floors.
     */
    private int myFloorNumbers = 10;
    /**
     * The floor height in meters.
     */
    private double myFloorHeight = 3;
    /**
     * The speed of the elevator in meters per second.
     */
    private double mySpeed = 1.5;
    /**
     * The open doors time period in seconds.
     */
    private int myEntranceTime = 3;

    private final Map<String, List<String>> params;

    private static String FLOOR_NUMBERS_PARAM = "floors";
    private static String FLOOR_HEIGHT_PARAM = "height";
    private static String SPEED_PARAM = "speed";
    private static String ENTRANCE_TIME_PARAM = "ent";

    private static final double OPEN_DOORS_TIME = 0.7;
    private static final double CLOSE_DOORS_TIME = OPEN_DOORS_TIME;

    private boolean isValid = true;

    Parameters(String[] args) {
        List<String> options = null;
        params = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                String param = args[i].substring(1);
                options = new ArrayList<>(1);
                params.put(param, options);
            } else if (options != null) {
                options.add(args[i]);
            } else {
                System.err.println(
                        "Unexpected parameter value without a parameter name: "
                                + args[i]);
                isValid = false;
                break;
            }
        }

        if (isValid) {
            isValid = readParameters();
        }

        if (isValid) {
            printCurrentParameters();
        } else {
            printUsage();
        }
    }

    boolean isValid() {
        return isValid;
    }

    int getFloorNumbers() {
        return myFloorNumbers;
    }

    double getFloorHeight() {
        return myFloorHeight;
    }

    double getSpeed() {
        return mySpeed;
    }

    double getOpenDoorsTime() {
        return OPEN_DOORS_TIME;
    }

    double getCloseDoorsTime() {
        return CLOSE_DOORS_TIME;
    }

    int getEntranceTime() {
        return myEntranceTime;
    }

    private void printUsage() {
        indent(1);
        System.out.println("Use the following paramters and values:");
        indent(2);
        System.out.println(
                "-floors n, where the n is the number of floors, greater or equals 5 and not greater 20");
        indent(2);
        System.out.println(
                "-height h, where the h is the floor height in meters");
        indent(2);
        System.out.println(
                "-speed s, where the s is the speed in meters pers second");
        indent(2);
        System.out.println(
                "-ent time, where the time is the time of open doors time period in seconds");
    }

    private void printCurrentParameters() {
        indent(1);
        System.out.println(
                "Starting the application with the following parameters:");
        indent(2);
        System.out.println("The number of floors: " + getFloorNumbers());
        indent(2);
        System.out
                .println("The floor height : " + getFloorHeight() + " meters");
        indent(2);
        System.out.println(
                "The elevator speed: " + getSpeed() + " meters per second");
        indent(2);
        System.out.println("The open doors time period: " + getEntranceTime()
                + " seconds");
        System.out.println("");
        System.out.flush();
    }

    private boolean readParameters() {
        if (params.containsKey("")) {
            System.err.println(
                    "Unexpected dash without parameter type in command line arguments");
            return false;
        }

        boolean valid = readFloorNumbers(params) && readFloorHeight()
                && readSpeed() && readEntranceTime();
        if (!valid) {
            return false;
        }

        if (params.size() > 0) {
            params.keySet().forEach(
                    key -> System.err.println("Unknown parameter " + key));
            return false;
        }
        return true;
    }

    private boolean readSpeed() {
        if (!validateOption(params, SPEED_PARAM, "elevator speed")) {
            return false;
        }
        if (params.containsKey(SPEED_PARAM)) {
            String speed = params.remove(SPEED_PARAM).get(0);
            try {
                mySpeed = Double.parseDouble(speed);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Unexpected elevator speed value specified via the command line: "
                                + speed);
                return false;
            }
        }

        if (mySpeed == 0) {
            System.err.println("The elevator speed cannot be 0");
        }
        return true;
    }

    private boolean readFloorHeight() {
        if (!validateOption(params, FLOOR_HEIGHT_PARAM, "floor height")) {
            return false;
        }
        if (params.containsKey(FLOOR_HEIGHT_PARAM)) {
            String floorHeight = params.remove(FLOOR_HEIGHT_PARAM).get(0);
            try {
                myFloorHeight = Double.parseDouble(floorHeight);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Unexpected floor height value specified via the command line: "
                                + floorHeight);
                return false;
            }
        }
        if (myFloorHeight == 0) {
            System.err.println("The floor height cannot be 0");
        }
        return true;
    }

    private boolean readFloorNumbers(Map<String, List<String>> params) {
        if (!validateOption(params, FLOOR_NUMBERS_PARAM, "floor numbers")) {
            return false;
        }
        if (params.containsKey(FLOOR_NUMBERS_PARAM)) {
            String floorNumbers = params.remove(FLOOR_NUMBERS_PARAM).get(0);
            try {
                myFloorNumbers = Integer.parseInt(floorNumbers);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Unexpected floor numbers value specified via the command line: "
                                + floorNumbers);
                return false;
            }
        }
        boolean valid = myFloorNumbers >= 5 && myFloorNumbers <= 20;
        if (!valid) {
            System.err.println(
                    "The floor numbers value should be greater or equals 5 and not less or equals 20");
        }
        return valid;
    }

    private boolean readEntranceTime() {
        if (!validateOption(params, ENTRANCE_TIME_PARAM,
                "open doors time period")) {
            return false;
        }
        if (params.containsKey(ENTRANCE_TIME_PARAM)) {
            String time = params.remove(ENTRANCE_TIME_PARAM).get(0);
            try {
                myEntranceTime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Unexpected open doors time period value specified via the command line: "
                                + time);
                return false;
            }
        }
        return true;
    }

    private boolean validateOption(Map<String, List<String>> params, String key,
            String parameter) {
        List<String> list = params.get(key);
        if (list != null && list.size() == 0) {
            System.err.println("No value is specified for the " + parameter);
            return false;
        } else if (list != null && list.size() > 1) {
            System.err.println(
                    "Too many values are specified for the " + parameter);
            return false;
        }
        return true;
    }

    private void indent(int i) {
        IntStream.range(0, i).forEach(indx -> System.out.print("  "));
    }
}
