package ro.fortsoft.baloo.strategy;

/**
 * @author Serban Balamaci
 */
public class DelayOnUnregisteredHostHandlingStrategy implements UnregisteredHostHandlingStrategy {

    private double delay;

    public DelayOnUnregisteredHostHandlingStrategy(double delay) {
        this.delay = delay;
    }

    public double getDelay() {
        return delay;
    }

}
