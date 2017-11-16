package study.microcoffee.creditrating.behavior;

/**
 * Class that implements fixed delay service behavior.
 */
public class FixedDelayServiceBehavior extends AbstractServiceBehavior {

    private int delaySecs;

    public FixedDelayServiceBehavior(int delaySecs) {
        this.delaySecs = delaySecs;
    }

    @Override
    public void execute() {
        sleep(delaySecs * 1000);
    }
}
