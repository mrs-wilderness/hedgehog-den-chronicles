package simulation.tools;

import config.ConfigParameters;
import manager.DenManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * This monitor ends the simulation if the hedgehog population reaches zero.
 * It interrupts the main thread to trigger shutdown.
 */
public class ExtinctionMonitor implements Runnable {

    private final Thread simulationThread;
    private static final Logger logger = LogManager.getLogger(ExtinctionMonitor.class);

    public ExtinctionMonitor(Thread simulationThread) {
        this.simulationThread = simulationThread;
    }

    @Override
    public void run() {
        DenManager manager = DenManager.getInstance();

        while (!Thread.currentThread().isInterrupted()) {
            if (manager.getHedgehogCount() == 0) {
                logger.info("EXTINCTION EVENT! No hedgehogs left. Ending simulation.");
                simulationThread.interrupt();
                break;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(ConfigParameters.POPULATION_MONITOR_CHECK_FREQUENCY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
