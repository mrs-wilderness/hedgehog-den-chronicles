package model.threads;

import config.ConfigParameters;
import manager.DenManager;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Periodically removes ticks from hedgehogs during the simulation.
 * Runs continuously with random delay jitter until interrupted.
 */
public class EpidemiologistThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(EpidemiologistThread.class);

    @Override
    public void run() {
        logger.info("Epidemiologist thread started.");
        DenManager manager = DenManager.getInstance();
        int jitterRange = (int) (ConfigParameters.EPIDEMIOLOGIST_AVG_DELAY_MS * ConfigParameters.THREAD_DELAY_JITTER_PERCENT);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                TickUtils.performTickRemoval(manager);
                TimeUnit.MILLISECONDS.sleep(
                        ConfigParameters.EPIDEMIOLOGIST_AVG_DELAY_MS
                                + ThreadLocalRandom.current().nextInt(-jitterRange, jitterRange + 1));
            } catch (InterruptedException e) {
                logger.info("Epidemiologist thread interrupted and stopping.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
