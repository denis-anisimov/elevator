package su.spb.den;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

class ElevatorManager {

    private final BlockingQueue<Command> myCommandQueue = new LinkedBlockingQueue<>();

    private final Parameters myParams;

    private final DispatcherThread myDispatcherThread = new DispatcherThread();

    private final Timer myTimer = new Timer();

    private final OutputProvider myOutputProvider;

    ElevatorManager(Parameters params) {
        this(params, new OutputProviderImpl());
    }

    ElevatorManager(Parameters params, OutputProvider provider) {
        myParams = params;
        myOutputProvider = provider;
        myDispatcherThread.setDaemon(true);
        myDispatcherThread.start();
    }

    protected OutputProvider getOutputProvider() {
        return myOutputProvider;
    }

    void callOutside(int floorNumber) {
        if (validateFloor(floorNumber)) {
            if (!myCommandQueue.offer(new OutsideCall(floorNumber))) {
                commandQueueOverload();
            }
        }
    }

    void callInside(int floorNumber) {
        if (validateFloor(floorNumber)) {
            if (!myCommandQueue.offer(new InsideCall(floorNumber))) {
                commandQueueOverload();
            }
        }
    }

    void shutdown() {
        myDispatcherThread.stop.set(true);
        myDispatcherThread.interrupt();
        myTimer.cancel();
    }

    private boolean validateFloor(int floorNumber) {
        if (floorNumber > myParams.getFloorNumbers()) {
            error(String.format("The input floor value '%d' is too big",
                    floorNumber));
            return false;
        } else if (floorNumber < 1) {
            error(String.format(
                    "The input floor value '%d' cannot be less than 1",
                    floorNumber));
            return false;
        }
        return true;
    }

    private void commandQueueOverload() {
        error("Too many commands to handle in queue. "
                + "The command is not accepted");
    }

    private void error(String error) {
        getOutputProvider().error(error);
    }

    /**
     * Internal command for the manager which has to be dispatched immediately
     * from the command queue.
     * <p>
     * Regular (user) commands are collected into inner queue to dispatch them
     * one by one. Meta commands are executed by the dispatcher thread
     * immediately.
     */
    private interface MetaCommand {

    }

    private class DispatcherThread extends Thread {

        private final AtomicReference<Boolean> stop = new AtomicReference<Boolean>(
                false);

        private int myCurrentFloor = 1;

        private boolean isElevatorAwaiting = true;

        private Queue<Command> myInternalQueue = new PriorityQueue<>();

        @Override
        public void run() {
            while (!stop.get()) {
                try {
                    Command command = myCommandQueue.take();
                    boolean executeNow = command instanceof MetaCommand;
                    executeNow = executeNow || (isElevatorAwaiting
                            && myInternalQueue.isEmpty());
                    if (executeNow) {
                        command.execute(myCurrentFloor);
                    } else {
                        myInternalQueue.add(command);
                    }
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }

    /**
     * Internal meta command that runs the next command from the internal queue.
     * <p>
     * This command is sent after each the elevator task (use command) is
     * completed.
     */
    private class CommandCompleted extends Command implements MetaCommand {

        CommandCompleted(int floor) {
            super(floor);
        }

        @Override
        void execute(int currentFloor) {
            assert myDispatcherThread == Thread.currentThread();

            Queue<Command> queue = myDispatcherThread.myInternalQueue;
            myDispatcherThread.myCurrentFloor = getCommandFloor();
            myDispatcherThread.isElevatorAwaiting = true;
            if (!queue.isEmpty()) {
                queue.poll().execute(getCommandFloor());
            }
        }

    }

    private abstract class AbstractCommand extends Command {

        AbstractCommand(int floor) {
            super(floor);
        }

        long scheduleMove(int currentFloor) {
            assert myDispatcherThread == Thread.currentThread();

            myDispatcherThread.isElevatorAwaiting = false;
            long floorTime = (long) ((myParams.getFloorHeight() * 1000)
                    / myParams.getSpeed());
            int delta = getCommandFloor() - currentFloor;
            int sign = delta > 0 ? 1 : -1;

            long latestScheduled = 0;
            for (int i = 1; i <= Math.abs(delta); i++) {
                latestScheduled = i * floorTime;
                int newFloor = currentFloor + sign * i;
                myTimer.schedule(new ElevatorTask(
                        () -> getOutputProvider().floorPassed(newFloor)),
                        latestScheduled);
            }
            return latestScheduled;
        }

        protected void scheduleDoors(long time) {
            assert myDispatcherThread == Thread.currentThread();

            long scheduledTime = time
                    + (long) (myParams.getOpenDoorsTime() * 1000);
            myTimer.schedule(new ElevatorTask(() -> {
                getOutputProvider().doorsOpened();
                myCommandQueue.add(new DoorsOpened(getCommandFloor()));
            }), scheduledTime);

            scheduledTime += myParams.getEntranceTime() * 1000;
            myTimer.schedule(
                    new ElevatorTask(() -> getOutputProvider().doorsClosing()),
                    scheduledTime);

            scheduledTime += myParams.getCloseDoorsTime() * 1000;
            myTimer.schedule(new ElevatorTask(() -> {
                getOutputProvider().doorsClosed();
                myCommandQueue.add(new CommandCompleted(getCommandFloor()));
            }), scheduledTime);
        }

    }

    private class OutsideCall extends AbstractCommand {

        OutsideCall(int floor) {
            super(floor);
        }

        @Override
        public int compareTo(Command o) {
            if (o instanceof OutsideCall) {
                return super.compareTo(o);
            }
            return 1;
        }

        @Override
        void execute(int currentFloor) {
            long time = scheduleMove(currentFloor);
            scheduleDoors(time);
        }

    }

    private class InsideCall extends AbstractCommand {

        InsideCall(int floor) {
            super(floor);
        }

        @Override
        public int compareTo(Command o) {
            if (o instanceof OutsideCall) {
                return -1;
            } else if (o instanceof InsideCall) {
                return super.compareTo(o);
            }
            return 1;
        }

        @Override
        void execute(int currentFloor) {
            assert myDispatcherThread == Thread.currentThread();

            if (getCommandFloor() == currentFloor) {
                getOutputProvider().sameFloorInside(getCommandFloor());
                myCommandQueue.add(new CommandCompleted(getCommandFloor()));
            } else {
                long time = scheduleMove(currentFloor);
                scheduleDoors(time);
            }
        }

    }

    private class DoorsOpened extends Command implements MetaCommand {

        DoorsOpened(int floor) {
            super(floor);
        }

        @Override
        void execute(int currentFloor) {
            assert myDispatcherThread == Thread.currentThread();

            Iterator<Command> iterator = myDispatcherThread.myInternalQueue
                    .iterator();
            while (iterator.hasNext()) {
                Command next = iterator.next();
                if (next.getCommandFloor() == getCommandFloor()) {
                    iterator.remove();
                }
            }
        }
    }

    private static class ElevatorTask extends TimerTask {

        private final Runnable myRunnable;

        ElevatorTask(Runnable runnable) {
            myRunnable = runnable;
        }

        @Override
        public void run() {
            myRunnable.run();
        }

    }

}
