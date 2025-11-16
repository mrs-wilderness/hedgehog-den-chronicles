package model;

import lombok.Getter;

/**
 * Represents a hedgehog in the simulation, tracking its state and attributes.
 * Is multithreading-agnostic by design and is only handled by the manager, not directly by threads.
 */
@Getter
public class Hedgehog {
    private final int id;
    private final String name;
    private int tickCount;
    private final Sex sex;
    private int offspringCount;
    private int seasonedWarriorLevel;


    public Hedgehog(int id, String name, Sex sex) {
        this.id = id;
        this.name = name;
        this.sex = sex;
    }

    public void addATick() {
        tickCount++;
    }

    public void removeAllTicks() {
        tickCount = 0;
    }

    public void incrementOffspringCount() {
        offspringCount++;
    }

    public void incrementSeasonedWarriorLevel() {
        seasonedWarriorLevel++;
    }

    public boolean isSeasonedWarrior() {
        return seasonedWarriorLevel > 0;
    }
}
