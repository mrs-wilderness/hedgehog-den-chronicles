package model.threads;

import config.ConfigParameters;
import manager.DenManager;
import simulation.tools.SimUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Periodically kills random hedgehogs to simulate predation by bums.
 * Runs continuously with random delay jitter until interrupted.
 */
public class HungryBumThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(HungryBumThread.class);

    @Override
    public void run() {
        logger.info("Hungry bum thread started.");
        DenManager manager = DenManager.getInstance();
        int jitterRange = (int) (ConfigParameters.HUNGRY_BUM_AVG_DELAY_MS * ConfigParameters.THREAD_DELAY_JITTER_PERCENT);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int ticket = manager.reserveAnyHedgehog();
                SimUtils.sleepInsideTask();
                manager.killThatHog(ticket, "devoured by a bum");

                TimeUnit.MILLISECONDS.sleep(
                        ConfigParameters.HUNGRY_BUM_AVG_DELAY_MS
                                + ThreadLocalRandom.current().nextInt(-jitterRange, jitterRange + 1));
            } catch (InterruptedException e) {
                logger.info("Hungry bum thread interrupted and stopping.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
