package simulation;

import config.ConfigParameters;
import manager.DenManager;
import manager.DenManager_RW;
import model.threads.*;
import simulation.tools.ExtinctionMonitor;
import simulation.tools.LiveStatsDisplay;
import simulation.tools.SimUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Coordinates the setup, execution, and shutdown of the hedgehog simulation.
 */
public class SimulationManager {

    private static final Logger logger = LogManager.getLogger(SimulationManager.class);

    public static void runSimulation() throws InterruptedException {
        validateConfigParameters();

        logger.info("Simulation started.");
        long startTime = System.currentTimeMillis();

        DenManager manager = DenManager.getInstance();

        ScheduledExecutorService liveStatsExecutor = startLiveStatsExecutor();

        initializePopulation(manager);
        performTickOutbreak();

        List<Thread> threads = startEventThreads();
        Thread extinctionMonitorThread = startExtinctionMonitor();

        letTheSimulationRun();

        shutdownEventThreads(threads);
        shutdownExtinctionMonitor(extinctionMonitorThread);
        shutdownLiveStatsExecutor(liveStatsExecutor);

        long endTime = System.currentTimeMillis();
        boolean tickBalanceConsistent = checkTickBalance(manager);
        boolean populationBalanceConsistent = checkPopulationBalance(manager);
        logger.info("Simulation complete.");

        promptForFinalStats();
        SimUtils.printFinalStats(startTime, endTime, tickBalanceConsistent, populationBalanceConsistent);
    }

    private static void letTheSimulationRun() {
        try {
            TimeUnit.SECONDS.sleep(ConfigParameters.MAX_SIMULATION_DURATION_SEC);
        } catch (InterruptedException e) {
            logger.info("Simulation interrupted. Proceeding to shutdown.");
        }
    }

    private static Thread startExtinctionMonitor() {
        Thread extinctionMonitorThread = new Thread(new ExtinctionMonitor(Thread.currentThread()));
        extinctionMonitorThread.start();
        return extinctionMonitorThread;
    }

    private static void performTickOutbreak() throws InterruptedException {
        if (!ConfigParameters.ENABLE_TICK_OUTBREAK) {
            return;
        }
        TickOutbreakThreadBurst outbreak = new TickOutbreakThreadBurst();
        outbreak.startOutbreak();
        outbreak.waitForOutbreakToFinish();
    }

    private static ScheduledExecutorService startLiveStatsExecutor() {
        ScheduledExecutorService liveStatsExecutor = Executors.newSingleThreadScheduledExecutor();
        liveStatsExecutor.scheduleAtFixedRate(new LiveStatsDisplay(),
                0, ConfigParameters.LIVE_STATS_REFRESH_FREQUENCY_MS, TimeUnit.MILLISECONDS);
        return liveStatsExecutor;
    }

    private static void shutdownExtinctionMonitor(Thread extinctionMonitorThread) throws InterruptedException {
        extinctionMonitorThread.interrupt();
        extinctionMonitorThread.join();
    }

    private static void initializePopulation(DenManager manager) {
        for (int i = 0; i < ConfigParameters.INITIAL_HEDGEHOG_COUNT; i++) {
            manager.createHedgehog();
        }
        logger.info("Initial hedgehog population created: " + ConfigParameters.INITIAL_HEDGEHOG_COUNT);
    }

    private static List<Thread> startEventThreads() {
        List<Thread> threads = new ArrayList<>();

        int roundedHogCount = (int) (10 * Math.round(ConfigParameters.INITIAL_HEDGEHOG_COUNT / 10.0));
        int threadsPerType = (roundedHogCount / 10) * ConfigParameters.NUMBER_OF_EVENT_THREADS_PER_TYPE_PER_10_HEDGEHOGS;
        logger.info("Creating " + threadsPerType + " threads per event type based on " + ConfigParameters.INITIAL_HEDGEHOG_COUNT + " initial hedgehogs.");

        for (int i = 0; i < threadsPerType; i++) {
            if (ConfigParameters.ENABLE_CASUAL_TICK_THREADS) {
                threads.add(new Thread(new TickCasualThread()));
            }
            if (ConfigParameters.ENABLE_EPIDEMIOLOGIST_THREADS) {
                threads.add(new Thread(new EpidemiologistThread()));
            }
            if (ConfigParameters.ENABLE_HUNGRY_BUM_THREADS) {
                threads.add(new Thread(new HungryBumThread()));
            }
            if (ConfigParameters.ENABLE_MATING_THREADS) {
                threads.add(new Thread(new MatingThread()));
            }
            if (ConfigParameters.ENABLE_FAIRY_THREADS) {
                threads.add(new Thread(new FairyThread()));
            }
        }
        threads.forEach(Thread::start);
        return threads;
    }

    private static void shutdownEventThreads(List<Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static void shutdownLiveStatsExecutor(ScheduledExecutorService executor) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(ConfigParameters.LIVE_STATS_REFRESH_FREQUENCY_MS + 1);
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            logger.warn("Live Stats Executor did not terminate in time. Forcing shutdown.");
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    private static void promptForFinalStats() {
        System.out.println("\nSimulation complete. Press Enter to view final stats...");
        new Scanner(System.in).nextLine();
    }

    private static boolean checkTickBalance(DenManager manager) {
        boolean tickBalanceConsistent = manager.getStats().getTicksAddedCount()
                == manager.getTotalTicksOnHedgehogs() + manager.getStats().getTicksRemovedCount();
        if (!tickBalanceConsistent) {
            logger.error("Actual tick count is not as expected. Critical error in simulation logic.");
        }
        return tickBalanceConsistent;
    }

    private static boolean checkPopulationBalance(DenManager manager) {
        boolean populationBalanceConsistent = manager.getStats().getHedgehogsBorn() - manager.getStats().getHedgehogsDied()
                == manager.getHedgehogCount();
        if (!populationBalanceConsistent) {
            logger.error("Actual hedgehog count is not as expected. Critical error in simulation logic.");
        }
        return populationBalanceConsistent;
    }

    private static void validateConfigParameters() {
        if (ConfigParameters.MAX_SIMULATION_DURATION_SEC < 0) {
            throw new IllegalArgumentException("MAX_SIMULATION_DURATION cannot be negative.");
        }
        if (ConfigParameters.INITIAL_HEDGEHOG_COUNT < 0) {
            throw new IllegalArgumentException("INITIAL_HEDGEHOG_COUNT cannot be negative.");
        }
        if (ConfigParameters.NUMBER_OF_EVENT_THREADS_PER_TYPE_PER_10_HEDGEHOGS < 0) {
            throw new IllegalArgumentException("NUMBER_OF_EVENT_THREADS_PER_TYPE_PER_10_HEDGEHOGS cannot be negative.");
        }
        if (ConfigParameters.FEMALE_BORN_PROBABILITY < 0 || ConfigParameters.FEMALE_BORN_PROBABILITY > 1) {
            throw new IllegalArgumentException("FEMALE_BORN_PROBABILITY must be between 0 and 1.");
        }
        if (ConfigParameters.PROBABILITY_DIE_AT_WAR < 0 || ConfigParameters.PROBABILITY_SEASONED_WARRIOR_LEVEL_UP < 0) {
            throw new IllegalArgumentException("PROBABILITY_DIE_AT_WAR and PROBABILITY_SEASONED_WARRIOR_LEVEL_UP cannot be negative.");
        }
        if (ConfigParameters.PROBABILITY_DIE_AT_WAR + ConfigParameters.PROBABILITY_SEASONED_WARRIOR_LEVEL_UP > 1) {
            throw new IllegalArgumentException("PROBABILITY_DIE_AT_WAR + PROBABILITY_SEASONED_WARRIOR_LEVEL_UP must not exceed 1.");
        }
        if (ConfigParameters.SEASONED_WARRIOR_DEATH_REDUCTION_PER_LEVEL < 0 || ConfigParameters.SEASONED_WARRIOR_DEATH_REDUCTION_PER_LEVEL >= 1) {
            throw new IllegalArgumentException("SEASONED_WARRIOR_DEATH_REDUCTION_PER_LEVEL must be in [0, 1).");
        }
        if (ConfigParameters.OUTBREAK_THREADS_PER_HEDGEHOG_FACTOR < 0) {
            throw new IllegalArgumentException("OUTBREAK_THREADS_PER_HEDGEHOG_FACTOR cannot be negative.");
        }
        if (ConfigParameters.MIN_TICKS_FOR_EPIDEMIOLOGIST < 0) {
            throw new IllegalArgumentException("MIN_TICKS_FOR_EPIDEMIOLOGIST cannot be negative.");
        }
        if (ConfigParameters.THREAD_DELAY_JITTER_PERCENT < 0 || ConfigParameters.THREAD_DELAY_JITTER_PERCENT > 1) {
            throw new IllegalArgumentException("THREAD_DELAY_JITTER_PERCENT must be between 0 and 1.");
        }
        if (ConfigParameters.MIN_SLEEP_INSIDE_TASK_MS < 0 || ConfigParameters.MAX_SLEEP_INSIDE_TASK_MS < 0) {
            throw new IllegalArgumentException("Sleep durations cannot be negative.");
        }
        if (ConfigParameters.MIN_SLEEP_INSIDE_TASK_MS > ConfigParameters.MAX_SLEEP_INSIDE_TASK_MS) {
            throw new IllegalArgumentException("MIN_SLEEP_INSIDE_TASK_MS cannot exceed MAX_SLEEP_INSIDE_TASK_MS.");
        }
        if (ConfigParameters.CASUAL_TICK_AVG_DELAY_MS < 0) {
            throw new IllegalArgumentException("CASUAL_TICK_AVG_DELAY_MS cannot be negative.");
        }
        if (ConfigParameters.EPIDEMIOLOGIST_AVG_DELAY_MS < 0) {
            throw new IllegalArgumentException("EPIDEMIOLOGIST_AVG_DELAY_MS cannot be negative.");
        }
        if (ConfigParameters.HUNGRY_BUM_AVG_DELAY_MS < 0) {
            throw new IllegalArgumentException("HUNGRY_BUM_AVG_DELAY_MS cannot be negative.");
        }
        if (ConfigParameters.MATING_AVG_DELAY_MS < 0) {
            throw new IllegalArgumentException("MATING_AVG_DELAY_MS cannot be negative.");
        }
        if (ConfigParameters.FAIRY_AVG_DELAY_MS < 0) {
            throw new IllegalArgumentException("FAIRY_AVG_DELAY_MS cannot be negative.");
        }
        if (ConfigParameters.POPULATION_MONITOR_CHECK_FREQUENCY_MS < 0) {
            throw new IllegalArgumentException("POPULATION_MONITOR_CHECK_FREQUENCY_MS cannot be negative.");
        }
        if (ConfigParameters.LIVE_STATS_REFRESH_FREQUENCY_MS < 0) {
            throw new IllegalArgumentException("LIVE_STATS_REFRESH_FREQUENCY_MS cannot be negative.");
        }
    }
}
