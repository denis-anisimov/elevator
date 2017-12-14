package su.spb.den;

public class OutputProviderImpl implements OutputProvider {

    @Override
    public void error(String error) {
        System.err.println(error);
        System.err.flush();
    }

    @Override
    public void floorPassed(int floor) {
        System.out.println(String
                .format("\nThe elevator is on the floor number %d", floor));
        showCommandInvite();
    }

    @Override
    public void doorsOpened() {
        System.out.println("\nThe elevator's doors are opened");
        showCommandInvite();
    }

    @Override
    public void doorsClosed() {
        System.out.println("\nThe elevator's doors are closed");
        showCommandInvite();
    }

    @Override
    public void sameFloorInside(int floor) {
        System.out.println(String.format(
                "\nThe elevator's doors are closed, it's on the floor number %s, "
                        + "ignoring command to go the same floor requested inside the elevator",
                floor));
        showCommandInvite();
    }

    @Override
    public void doorsClosing() {
        // ignore
    }

    private void showCommandInvite() {
        System.out.print("> ");
        System.out.flush();
    }
}
