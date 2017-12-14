package su.spb.den;

public interface OutputProvider {

    void error(String error);

    void floorPassed(int floor);

    void doorsOpened();

    void doorsClosed();

    void doorsClosing();

    void sameFloorInside(int floor);
}
