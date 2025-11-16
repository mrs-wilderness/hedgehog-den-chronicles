package manager;

import model.Hedgehog;
import model.Sex;
import config.HedgehogNameManager;
import config.ConfigParameters;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central manager for all hedgehogs in the simulation.
 * Handles creation, reservation, release, reproduction, death, and war events.
 * Implements thread-safe resource management using a ReentrantLock and condition variables.
 * Singleton design pattern is used to ensure a single shared manager instance.
 * NB! Hedgehog list (map) access and operations are locked,
 * individual hedgehog access is assumed to be safe after reservation,
 * which matches the story in the task requirements.
 */
public class DenManager_RW{
    private final Map<Integer, Hedgehog> hedgehogs = new HashMap<>();
    private final Set<Hedgehog> reservedHedgehogs = new HashSet<>();
    private final List<Integer> randomizableHedgehogIds = new ArrayList<>();
    private int nextHedgehogId = 1;
    private int currentMaleHedgehogCount = 0;
    private int currentFemaleHedgehogCount = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private final Condition anyHedgehogAvailable = writeLock.newCondition();
    private final Condition hedgehogExceedsTickThreshold = writeLock.newCondition();
    private final Condition femaleHedgehogAvailable = writeLock.newCondition();
    private final Condition maleHedgehogAvailable = writeLock.newCondition();
    @Getter
    private final SimulationStats stats = new SimulationStats();
    private final HedgehogNameManager nameManager = new HedgehogNameManager();
    private static final Logger logger = LogManager.getLogger(DenManager_RW.class);

    private DenManager_RW() {
    }

    private static class Holder {
        private static final DenManager_RW INSTANCE = new DenManager_RW();
    }

    public static DenManager_RW getInstance() {
        return Holder.INSTANCE;
    }

    public void createHedgehog() {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            Sex sex = ThreadLocalRandom.current().nextDouble() < ConfigParameters.FEMALE_BORN_PROBABILITY
                    ? Sex.FEMALE : Sex.MALE;
            String name = nameManager.getNextName(sex);
            Hedgehog hedgehog = new Hedgehog(nextHedgehogId++, name, sex);
            hedgehogs.put(hedgehog.getId(), hedgehog);
            randomizableHedgehogIds.add(hedgehog.getId());
            stats.incrementHedgehogsBorn();

            if (sex == Sex.FEMALE) {
                currentFemaleHedgehogCount++;
                stats.incrementFemaleBornCount();
                femaleHedgehogAvailable.signal();
            } else {
                currentMaleHedgehogCount++;
                stats.incrementMaleBornCount();
                maleHedgehogAvailable.signal();
            }
            anyHedgehogAvailable.signal();
        } finally {
            writeLock.unlock();
        }
    }

    public int reserveAnyHedgehog() throws InterruptedException {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            while (hedgehogs.size() == reservedHedgehogs.size()) {
                stats.incrementConditionWait();
                anyHedgehogAvailable.await();
            }
            while (true) {
                int candidateId = randomizableHedgehogIds.get(ThreadLocalRandom.current().nextInt(randomizableHedgehogIds.size()));
                Hedgehog hedgehog = hedgehogs.get(candidateId);
                if (reservedHedgehogs.add(hedgehog)) {
                    return candidateId;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public int reserveHedgehogAboveTickThreshold() throws InterruptedException {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            while (true) {
                Collections.shuffle(randomizableHedgehogIds);

                for (int candidateId : randomizableHedgehogIds) {
                    Hedgehog hedgehog = hedgehogs.get(candidateId);
                    if (!isReserved(hedgehog)
                            && hedgehog.getTickCount() >= ConfigParameters.MIN_TICKS_FOR_EPIDEMIOLOGIST) {
                        reservedHedgehogs.add(hedgehog);
                        return candidateId;
                    }
                }
                stats.incrementConditionWait();
                hedgehogExceedsTickThreshold.await();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public int reserveFemaleHedgehogIfAny() throws InterruptedException {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            if (currentFemaleHedgehogCount == 0) {
                return -1;
            }
            while (true) {
                Collections.shuffle(randomizableHedgehogIds);
                for (int candidateId : randomizableHedgehogIds) {
                    Hedgehog hedgehog = hedgehogs.get(candidateId);
                    if (!isReserved(hedgehog) && hedgehog.getSex() == Sex.FEMALE) {
                        reservedHedgehogs.add(hedgehog);
                        return candidateId;
                    }
                }
                stats.incrementConditionWait();
                femaleHedgehogAvailable.await();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public int reserveMaleHedgehogIfAny() throws InterruptedException {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            if (currentMaleHedgehogCount == 0) {
                return -1;
            }
            while (true) {
                Collections.shuffle(randomizableHedgehogIds);
                for (int candidateId : randomizableHedgehogIds) {
                    Hedgehog hedgehog = hedgehogs.get(candidateId);
                    if (!isReserved(hedgehog) && hedgehog.getSex() == Sex.MALE) {
                        reservedHedgehogs.add(hedgehog);
                        return candidateId;
                    }
                }
                stats.incrementConditionWait();
                maleHedgehogAvailable.await();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public int reserveHedgehogForFairy() throws InterruptedException {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            while (true) {
                Collections.shuffle(randomizableHedgehogIds);

                // Try to reserve a seasoned warrior
                for (int candidateId : randomizableHedgehogIds) {
                    Hedgehog hedgehog = hedgehogs.get(candidateId);
                    if (!isReserved(hedgehog) && hedgehog.isSeasonedWarrior()) {
                        reservedHedgehogs.add(hedgehog);
                        return candidateId;
                    }
                }

                // Try to reserve a male
                for (int candidateId : randomizableHedgehogIds) {
                    Hedgehog hedgehog = hedgehogs.get(candidateId);
                    if (!isReserved(hedgehog) && hedgehog.getSex() == Sex.MALE) {
                        reservedHedgehogs.add(hedgehog);
                        return candidateId;
                    }
                }

                // Reserve any unreserved hedgehog
                for (int candidateId : randomizableHedgehogIds) {
                    Hedgehog hedgehog = hedgehogs.get(candidateId);
                    if (!isReserved(hedgehog)) {
                        reservedHedgehogs.add(hedgehog);
                        return candidateId;
                    }
                }

                stats.incrementConditionWait();
                anyHedgehogAvailable.await();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void releaseHedgehog(int ticket) {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            Hedgehog hedgehog = safelyGetHedgehog(ticket);
            validateReservedHedgehog(hedgehog, "release");
            reservedHedgehogs.remove(hedgehog);

            if (hedgehog.getSex() == Sex.FEMALE) {
                femaleHedgehogAvailable.signal();
            } else {
                maleHedgehogAvailable.signal();
            }

            if (hedgehog.getTickCount() >= ConfigParameters.MIN_TICKS_FOR_EPIDEMIOLOGIST) {
                hedgehogExceedsTickThreshold.signal();
            }

            anyHedgehogAvailable.signal();
        } finally {
            writeLock.unlock();
        }
    }

    public void killThatHog(int ticket, String reason) {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            Hedgehog hedgehog = safelyGetHedgehog(ticket);
            validateReservedHedgehog(hedgehog, "kill");

            stats.adjustTicksRemoved(hedgehog.getTickCount());
            if (hedgehog.getSex() == Sex.FEMALE) {
                currentFemaleHedgehogCount--;
            } else {
                currentMaleHedgehogCount--;
            }

            hedgehogs.remove(ticket);
            reservedHedgehogs.remove(hedgehog);
            randomizableHedgehogIds.remove((Integer) ticket);
            stats.incrementHedgehogsDied();
            logger.info(hedgehog.getName() + " has died " + reason + ", with " + hedgehog.getOffspringCount() + " offspring.");
        } finally {
            writeLock.unlock();
        }
    }

    public void reproduce(int ticket1, int ticket2) {
        if (!writeLock.tryLock()) {
            stats.incrementLockContention();
            writeLock.lock();
        }

        try {
            Hedgehog hog1 = safelyGetHedgehog(ticket1);
            validateReservedHedgehog(hog1, "mate");
            Hedgehog hog2 = safelyGetHedgehog(ticket2);
            validateReservedHedgehog(hog2, "mate");

            Hedgehog mother;
            Hedgehog father;
            if (hog1.getSex() == Sex.FEMALE && hog2.getSex() == Sex.MALE) {
                mother = hog1;
                father = hog2;
            } else if (hog2.getSex() == Sex.FEMALE && hog1.getSex() == Sex.MALE) {
                mother = hog2;
                father = hog1;
            } else {
                logger.error("Critical logic error: Same-sex mating attempt between hedgehogs with tickets: " + ticket1 + ", " + ticket2);
                throw new IllegalStateException("Invalid mating attempt: same sex hedgehogs.");
            }

            mother.incrementOffspringCount();
            father.incrementOffspringCount();
            createHedgehog();
            logger.info(mother.getName() + " had a baby! " + father.getName() + " is a proud dad!");
        } finally {
            writeLock.unlock();
        }
    }

    public WarOutcome sendReservedHedgehogToWar(int ticket) {
        Hedgehog hedgehog = safelyGetHedgehog(ticket);
        validateReservedHedgehog(hedgehog, "send to war");

        double outcome = ThreadLocalRandom.current().nextDouble();
        double adjustedDeathProb = ConfigParameters.PROBABILITY_DIE_AT_WAR
                * Math.pow((1 - ConfigParameters.SEASONED_WARRIOR_DEATH_REDUCTION_PER_LEVEL), hedgehog.getSeasonedWarriorLevel());

        stats.incrementHedgehogsWentToWar();
        if (hedgehog.isSeasonedWarrior() && outcome >= adjustedDeathProb && outcome < ConfigParameters.PROBABILITY_DIE_AT_WAR) {
            logger.info("Seasoned warrior " + hedgehog.getName() + "'s prior experience helped them escape death!");
        }
        if (outcome < adjustedDeathProb) {
            stats.incrementHedgehogsDiedInWar();
            killThatHog(ticket, "in battle");
            return WarOutcome.DIED;
        } else if (outcome < adjustedDeathProb + ConfigParameters.PROBABILITY_SEASONED_WARRIOR_LEVEL_UP) {
            hedgehog.incrementSeasonedWarriorLevel();
            stats.incrementHedgehogsReturnedSeasoned();
            logger.info(hedgehog.getName() + " has leveled up as a seasoned warrior!");
            return WarOutcome.WARRIOR_LEVEL_UP;
        } else {
            logger.info(hedgehog.getName() + " returned safely from battle.");
            return WarOutcome.RETURNED_UNREMARKABLE;
        }
    }

    public int getHedgehogCount() {
        if (!readLock.tryLock()) {
            stats.incrementLockContention();
            readLock.lock();
        }

        try {
            return hedgehogs.size();
        } finally {
            readLock.unlock();
        }
    }

    public void addTickToReservedHedgehog(int ticket) {
        Hedgehog hedgehog = safelyGetHedgehog(ticket);
        validateReservedHedgehog(hedgehog, "add a tick to");

        hedgehog.addATick();
        stats.incrementTicksAdded();
    }

    public void removeAllTicksFromReservedHedgehog(int ticket) {
        Hedgehog hedgehog = safelyGetHedgehog(ticket);
        validateReservedHedgehog(hedgehog, "remove ticks from");

        int ticksBefore = hedgehog.getTickCount();
        hedgehog.removeAllTicks();
        stats.adjustTicksRemoved(ticksBefore);
        logger.info(ticksBefore + " ticks were removed from " + hedgehog.getName());
    }

    public int getTotalTicksOnHedgehogs() {
        if (!readLock.tryLock()) {
            stats.incrementLockContention();
            readLock.lock();
        }
        try {
            int sum = 0;
            for (Hedgehog h : hedgehogs.values()) {
                sum += h.getTickCount();
            }
            return sum;
        } finally {
            readLock.unlock();
        }
    }

    private boolean isReserved(Hedgehog hedgehog) {
        if (!readLock.tryLock()) {
            stats.incrementLockContention();
            readLock.lock();
        }
        try {
            return reservedHedgehogs.contains(hedgehog);
        } finally {
            readLock.unlock();
        }
    }

    private Hedgehog safelyGetHedgehog(int ticket) {
        if (!readLock.tryLock()) {
            stats.incrementLockContention();
            readLock.lock();
        }
        try {
            return hedgehogs.get(ticket);
        } finally {
            readLock.unlock();
        }
    }

    private void validateReservedHedgehog(Hedgehog hedgehog, String context) {
        if (hedgehog == null || !isReserved(hedgehog)) {
            logger.error("Critical logic error: Attempted to " + context + " hedgehog, but it either doesn't exist or was not properly reserved.");
            throw new IllegalStateException("Hedgehog does not exist or was not reserved.");
        }
    }

}
