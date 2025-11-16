package manager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks global simulation statistics in a thread-safe manner.
 */
public class SimulationStats {
    // Counts how many times threads encountered explicit lock contention.
    private final AtomicInteger lockContentionCount = new AtomicInteger(0);
    // Counts how many times threads had to wait on a condition.
    private final AtomicInteger conditionWaitCount = new AtomicInteger(0);
    private final AtomicInteger ticksAddedCount = new AtomicInteger(0);
    private final AtomicInteger ticksRemovedCount = new AtomicInteger(0);
    private final AtomicInteger hedgehogsBorn = new AtomicInteger(0);
    private final AtomicInteger hedgehogsDied = new AtomicInteger(0);
    private final AtomicInteger maleBornCount = new AtomicInteger(0);
    private final AtomicInteger femaleBornCount = new AtomicInteger(0);
    private final AtomicInteger hedgehogsWentToWar = new AtomicInteger(0);
    private final AtomicInteger hedgehogsDiedInWar = new AtomicInteger(0);
    private final AtomicInteger hedgehogsWarriorLevelUps = new AtomicInteger(0);


    public void incrementMaleBornCount() {
        maleBornCount.incrementAndGet();
    }

    public void incrementFemaleBornCount() {
        femaleBornCount.incrementAndGet();
    }

    public void incrementHedgehogsWentToWar() {
        hedgehogsWentToWar.incrementAndGet();
    }

    public void incrementHedgehogsDiedInWar() {
        hedgehogsDiedInWar.incrementAndGet();
    }

    public void incrementHedgehogsReturnedSeasoned() {
        hedgehogsWarriorLevelUps.incrementAndGet();
    }

    public void incrementLockContention() {
        lockContentionCount.incrementAndGet();
    }

    public void incrementConditionWait() {
        conditionWaitCount.incrementAndGet();
    }

    public void incrementTicksAdded() {
        ticksAddedCount.incrementAndGet();
    }

    public void adjustTicksRemoved(int removedCount) {
        ticksRemovedCount.addAndGet(removedCount);
    }

    public void incrementHedgehogsBorn() {
        hedgehogsBorn.incrementAndGet();
    }

    public void incrementHedgehogsDied() {
        hedgehogsDied.incrementAndGet();
    }
    public int getFemaleBornCount() {
        return femaleBornCount.get();
    }

    public int getMaleBornCount() {
        return maleBornCount.get();
    }

    public int getLockContentionCount() {
        return lockContentionCount.get();
    }

    public int getConditionWaitCount() {
        return conditionWaitCount.get();
    }

    public int getTicksAddedCount() {
        return ticksAddedCount.get();
    }

    public int getTicksRemovedCount() {
        return ticksRemovedCount.get();
    }
    public int getHedgehogsBorn() {
        return hedgehogsBorn.get();
    }

    public int getHedgehogsDied() {
        return hedgehogsDied.get();
    }

    public int getHedgehogsWentToWar() {
        return hedgehogsWentToWar.get();
    }

    public int getHedgehogsDiedInWar() {
        return hedgehogsDiedInWar.get();
    }

    public int getHedgehogsWarriorLevelUps() {
        return hedgehogsWarriorLevelUps.get();
    }

}
