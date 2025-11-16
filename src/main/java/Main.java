import simulation.SimulationManager;

public class Main {
    public static void main(String[] args) {
        try {
            SimulationManager.runSimulation();
        } catch (InterruptedException e) {
            System.out.println("Simulation interrupted externally. Exiting...");
            Thread.currentThread().interrupt();
        }
    }
}
