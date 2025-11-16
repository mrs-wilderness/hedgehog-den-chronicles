package model.threads;

import manager.DenManager;
import simulation.tools.SimUtils;

/**
 * Utility methods for tick-related operations.
 */
public class TickUtils {

    public static void performTickAddition(DenManager manager) throws InterruptedException {
        int ticket = manager.reserveAnyHedgehog();

        try {
            manager.addTickToReservedHedgehog(ticket);

            SimUtils.sleepInsideTask();

        } finally {
            manager.releaseHedgehog(ticket);
        }
    }

    public static void performTickRemoval(DenManager manager) throws InterruptedException {
        int ticket = manager.reserveHedgehogAboveTickThreshold();

        try {
            manager.removeAllTicksFromReservedHedgehog(ticket);

            SimUtils.sleepInsideTask();

        } finally {
            manager.releaseHedgehog(ticket);
        }
    }

}
