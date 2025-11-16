package simulation.tools;

import manager.DenManager;
import manager.SimulationStats;

/**
 * Displays live stats. Has no impact on simulation logic.
 * Intended for periodic execution via {@link java.util.concurrent.ScheduledExecutorService}.
 * Uses a one-line carriage return ('\r') approach due to IntelliJ Run Console limitations.
 * Note: Console output may appear fragmented if other threads print to System.out simultaneously.
 */
public class LiveStatsDisplay implements Runnable {

    @Override
    public void run() {
        DenManager manager = DenManager.getInstance();
        SimulationStats stats = manager.getStats();

        System.out.print("\rHedgehogs born: " + stats.getHedgehogsBorn()
                + "  Hedgehogs died: " + stats.getHedgehogsDied()
                + "  Ticks added: " + stats.getTicksAddedCount()
                + "  Ticks removed: " + stats.getTicksRemovedCount()
                + "  Lock clashes: " + stats.getLockContentionCount()
                + "  Condition waits: " + stats.getConditionWaitCount());
    }
}
