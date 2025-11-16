package model.threads;

import config.ConfigParameters;
import manager.DenManager;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Launches a short-lived burst of threads to simulate a tick outbreak.
 * Intended to stress the simulation at startup and add an initial tick population.
 * Treated as one whole event and relies on the caller invoking waitForOutbreakToFinish().
 */
public class TickOutbreakThreadBurst {
    private final int threadCount;
    private final List<Thread> threads = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(TickOutbreakThreadBurst.class);

    public TickOutbreakThreadBurst() {
        int hedgehogCount = DenManager.getInstance().getHedgehogCount();
        this.threadCount = (int) Math.round(hedgehogCount * ConfigParameters.OUTBREAK_THREADS_PER_HEDGEHOG_FACTOR);
    }

    public void startOutbreak() {
        logger.info("Starting a tick outbreak with " + threadCount + " threads.");
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new TickRunnable());
            threads.add(thread);
            thread.start();
        }
    }

    public void waitForOutbreakToFinish() throws InterruptedException {
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            logger.warn("Simulation was interrupted while waiting for a tick outbreak to finish.");
            throw e;
        }
        logger.info("Tick outbreak finished.");
    }

    private static class TickRunnable implements Runnable {
        @Override
        public void run() {
            DenManager manager = DenManager.getInstance();

            try {
                TickUtils.performTickAddition(manager);
            } catch (InterruptedException e) {
                logger.warn("Tick outbreak thread was interrupted before completion.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
