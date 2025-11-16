package model.threads;

import config.ConfigParameters;
import manager.DenManager;
import simulation.tools.SimUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Periodically attempts to mate hedgehogs, creating offspring if successful.
 * Runs continuously with random delay jitter until interrupted.
 */
public class MatingThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(MatingThread.class);

    @Override
    public void run() {
        logger.info("Mating thread started.");
        DenManager manager = DenManager.getInstance();
        int jitterRange = (int) (ConfigParameters.MATING_AVG_DELAY_MS * ConfigParameters.THREAD_DELAY_JITTER_PERCENT);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int femaleTicket = manager.reserveFemaleHedgehogIfAny();
                if (femaleTicket == -1) {
                    sleepWithJitter(jitterRange);
                    continue;
                }

                int maleTicket = manager.reserveMaleHedgehogIfAny();
                if (maleTicket != -1) {
                    SimUtils.sleepInsideTask();
                    manager.reproduce(femaleTicket, maleTicket);
                    manager.releaseHedgehog(femaleTicket);
                    manager.releaseHedgehog(maleTicket);
                } else {
                    manager.releaseHedgehog(femaleTicket);
                }

                sleepWithJitter(jitterRange);
            } catch (InterruptedException e) {
                logger.info("Mating thread interrupted and stopping.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sleepWithJitter(int jitterRange) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(
                ConfigParameters.MATING_AVG_DELAY_MS
                        + ThreadLocalRandom.current().nextInt(-jitterRange, jitterRange + 1));
    }
}
