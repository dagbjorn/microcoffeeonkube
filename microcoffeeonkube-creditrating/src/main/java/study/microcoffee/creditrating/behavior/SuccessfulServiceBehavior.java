package study.microcoffee.creditrating.behavior;

/**
 * Class that implements successful service behavior.
 */
public class SuccessfulServiceBehavior extends AbstractServiceBehavior {

    @Override
    public void execute() {
        sleep(DEFAULT_EXECUTION_TIME_MS);
    }
}
