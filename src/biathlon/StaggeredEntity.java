package biathlon;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 *
 * @author Artur Hebda
 */
public class StaggeredEntity extends Entity {
    protected TimeInstant createdAt;

    public StaggeredEntity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.createdAt = presentTime();
    }

    @Override
    public TimeInstant getPresentTime() {
        return presentTime(); // minus createdAt
    }
}
