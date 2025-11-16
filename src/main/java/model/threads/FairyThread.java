package model.threads;

import config.ConfigParameters;
import manager.DenManager;
import manager.WarOutcome;
import simulation.tools.SimUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Periodically sends hedgehogs to war on behalf of the fairies.
 * Runs continuously with random delay jitter until interrupted.
 */
public class FairyThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(FairyThread.class);

    @Override
    public void run() {
        logger.info("Fairy thread started.");
        DenManager manager = DenManager.getInstance();
        int jitterRange = (int) (ConfigParameters.FAIRY_AVG_DELAY_MS * ConfigParameters.THREAD_DELAY_JITTER_PERCENT);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int ticket = manager.reserveHedgehogForFairy();
                SimUtils.sleepInsideTask();
                WarOutcome outcome = manager.sendReservedHedgehogToWar(ticket);
                if (outcome != WarOutcome.DIED) {
                    manager.releaseHedgehog(ticket);
                }

                TimeUnit.MILLISECONDS.sleep(
                        ConfigParameters.FAIRY_AVG_DELAY_MS
                                + ThreadLocalRandom.current().nextInt(-jitterRange, jitterRange + 1));
            } catch (InterruptedException e) {
                logger.info("Fairy thread interrupted and stopping.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
