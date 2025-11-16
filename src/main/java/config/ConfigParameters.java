package config;

/**
 * Central configuration for simulation settings, probabilities, and timing.
 * All parameters are static and should be adjusted before simulation start.
 */
public class ConfigParameters {
    // ===== Simulation Scope =====
    public static final int MAX_SIMULATION_DURATION_SEC = 30;
    // There are 100 unique male and 100 unique female hedgehog names available with the current lists.
    // The actual maximum total hedgehog count is not strictly 200 due to randomized sex assignment.
    public static final int INITIAL_HEDGEHOG_COUNT = 10;
    public static final int NUMBER_OF_EVENT_THREADS_PER_TYPE_PER_10_HEDGEHOGS = 1;

    // ===== Feature Toggles =====
    public static final boolean ENABLE_TICK_OUTBREAK = true;
    public static final boolean ENABLE_CASUAL_TICK_THREADS = true;
    public static final boolean ENABLE_EPIDEMIOLOGIST_THREADS = true;
    public static final boolean ENABLE_HUNGRY_BUM_THREADS = true;
    public static final boolean ENABLE_MATING_THREADS = true;
    public static final boolean ENABLE_FAIRY_THREADS = true;

    // ===== Behavioral Probability Settings =====
    public static final double FEMALE_BORN_PROBABILITY = 0.4;
    public static final double PROBABILITY_DIE_AT_WAR = 0.3;
    public static final double PROBABILITY_SEASONED_WARRIOR_LEVEL_UP = 0.3;
    public static final double SEASONED_WARRIOR_DEATH_REDUCTION_PER_LEVEL = 0.15;

    // ===== Thread Scaling & Triggers =====
    public static final double OUTBREAK_THREADS_PER_HEDGEHOG_FACTOR = 4.0;
    public static final int MIN_TICKS_FOR_EPIDEMIOLOGIST = 5;

    // ===== Thread Timing (Delays & Jitter) =====
    public static final double THREAD_DELAY_JITTER_PERCENT = 0.5;

    public static final int CASUAL_TICK_AVG_DELAY_MS = 200;
    public static final int EPIDEMIOLOGIST_AVG_DELAY_MS = 600;
    public static final int HUNGRY_BUM_AVG_DELAY_MS = 2000;
    public static final int MATING_AVG_DELAY_MS = 1500;
    public static final int FAIRY_AVG_DELAY_MS = 1400;

    public static final int MIN_SLEEP_INSIDE_TASK_MS = 10;
    public static final int MAX_SLEEP_INSIDE_TASK_MS = 50;

    // ===== Monitoring & Display =====
    public static final int POPULATION_MONITOR_CHECK_FREQUENCY_MS = 500;
    public static final int LIVE_STATS_REFRESH_FREQUENCY_MS = 500;
}
