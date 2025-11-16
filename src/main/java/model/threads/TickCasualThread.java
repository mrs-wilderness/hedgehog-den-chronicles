package model.threads;

import config.ConfigParameters;
import manager.DenManager;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Periodically adds ticks to hedgehogs during the simulation.
 * Runs continuously with random delay jitter until interrupted.
 */
public class TickCasualThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(TickCasualThread.class);

    @Override
    public void run() {
        logger.info("Casual tick thread started.");
        DenManager manager = DenManager.getInstance();
        int jitterRange = (int) (ConfigParameters.CASUAL_TICK_AVG_DELAY_MS * ConfigParameters.THREAD_DELAY_JITTER_PERCENT);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                TickUtils.performTickAddition(manager);
                TimeUnit.MILLISECONDS.sleep(
                        ConfigParameters.CASUAL_TICK_AVG_DELAY_MS
                                + ThreadLocalRandom.current().nextInt(-jitterRange, jitterRange + 1));
            } catch (InterruptedException e) {
                logger.info("Casual tick thread interrupted and stopping.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
