package biathlon.core;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;

/**
 * Klasa reprezentująca "opóźnioną" encję.
 * "Opóźniona" encja to taka, która powstała w pewnym niezerowym punkcie czasowym symulacji
 * i ważne dla niej jest śledzenie czasu we własnym układzie odniesienia.
 * 
 * @author Artur Hebda
 */
public class StaggeredEntity extends Entity {
    protected TimeSpan creationDelay;

    public StaggeredEntity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        creationDelay = new TimeSpan(owner.presentTime().getTimeAsDouble(TimeUnit.SECONDS), TimeUnit.SECONDS);
    }


    /**
     * Czas życia encji - czas w jej własnym układzie odniesienia
     * @return czas życia encji (od momentu powstania do obecnej chwili w symulacji) 
     */
    @Override
    public TimeInstant lifeTime() {
        return TimeOperations.subtract(presentTime(), creationDelay);
    }
}
