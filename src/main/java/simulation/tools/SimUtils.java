package simulation.tools;

import config.ConfigParameters;
import manager.DenManager;
import manager.SimulationStats;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Assorted utility methods for simulation output and consistency checks.
 */
public class SimUtils {

    public static void printFinalStats(long startTime, long endTime, boolean tickBalanceConsistent, boolean populationBalanceConsistent) {
        DenManager manager = DenManager.getInstance();
        SimulationStats stats = manager.getStats();

        int ticksAdded = stats.getTicksAddedCount();
        int ticksRemoved = stats.getTicksRemovedCount();
        int totalTicksPresent = manager.getTotalTicksOnHedgehogs();
        int hedgehogsBorn = stats.getHedgehogsBorn();
        int maleBornCount = stats.getMaleBornCount();
        int femaleBornCount = stats.getFemaleBornCount();
        int hedgehogsDied = stats.getHedgehogsDied();
        int currentHedgehogCount = manager.getHedgehogCount();
        int hedgehogsWentToWar = stats.getHedgehogsWentToWar();
        int hedgehogsDiedInWar = stats.getHedgehogsDiedInWar();
        int hedgehogsReturnedSeasoned = stats.getHedgehogsWarriorLevelUps();

        System.out.println("\n=== Final Simulation Stats ===");
        System.out.println("Hedgehogs born: " + hedgehogsBorn + " (" + maleBornCount + " ♂ / " + femaleBornCount + " ♀)");
        System.out.println("Hedgehogs died: " + hedgehogsDied);
        System.out.println("Final hedgehog population: " + currentHedgehogCount);
        if (populationBalanceConsistent) {
            System.out.println("Population balance is consistent ✅");
        } else {
            System.out.println("Population balance inconsistency detected ⚠️");
        }
        System.out.println("Total ticks added: " + ticksAdded);
        System.out.println("Total ticks removed: " + ticksRemoved);
        System.out.println("Total ticks present on hedgehogs: " + totalTicksPresent);
        if (tickBalanceConsistent) {
            System.out.println("Tick balance is consistent ✅");
        } else {
            System.out.println("Tick balance inconsistency detected ⚠️");
        }
        System.out.println("Hedgehogs sent to war: " + hedgehogsWentToWar + " (" + hedgehogsDiedInWar + " ☠️ / " + hedgehogsReturnedSeasoned + " 🏅)");
        System.out.println("Total lock contention events: " + stats.getLockContentionCount());
        System.out.println("Total condition waits: " + stats.getConditionWaitCount());
        System.out.println("Total simulation duration: " + (endTime - startTime) + " ms");
    }

    public static void sleepInsideTask() throws InterruptedException {
        int sleepTime = ThreadLocalRandom.current().nextInt(
                ConfigParameters.MIN_SLEEP_INSIDE_TASK_MS,
                ConfigParameters.MAX_SLEEP_INSIDE_TASK_MS + 1
        );
        TimeUnit.MILLISECONDS.sleep(sleepTime);
    }
}
