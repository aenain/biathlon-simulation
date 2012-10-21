package biathlon;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class StaggeredEntity extends Entity {
    protected TimeSpan creationDelay;

    public StaggeredEntity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        creationDelay = new TimeSpan(owner.presentTime().getTimeAsDouble(TimeUnit.SECONDS), TimeUnit.SECONDS);
    }

    @Override
    public TimeInstant getPresentTime() {
        return TimeOperations.subtract(presentTime(), creationDelay);
    }
}
