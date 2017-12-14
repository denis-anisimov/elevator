package su.spb.den;

import java.util.Comparator;

abstract class Command implements Comparable<Command> {

    private final int myFloor;

    private long order = System.currentTimeMillis();

    Command(int floor) {
        myFloor = floor;
    }

    abstract void execute(int currentFloor);

    protected int getCommandFloor() {
        return myFloor;
    }

    @Override
    public int compareTo(Command o) {
        return Comparator.<Long> naturalOrder().compare(order, o.order);
    }
}
