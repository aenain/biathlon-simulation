package biathlon.event;

import biathlon.Biathlete;
import desmoj.core.simulator.TimeInstant;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class BiathleteEvent {
    protected Biathlete biathlete;
    protected TimeInstant biathleteTime;

    public BiathleteEvent(Biathlete biathlete) {
        // workaround braku mozliwosci skopiowania obiektu TimeInstant
        biathleteTime = new TimeInstant(biathlete.lifeTime().getTimeAsDouble(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    public TimeInstant getBiathleteTime() {
        return biathleteTime;
    }

    public Biathlete getBiathlete() {
        return biathlete;
    }

    public int compareTo(Object o) {
        BiathleteEvent other = (BiathleteEvent)o;
        return biathleteTime.compareTo(other.getBiathleteTime());
    }
}
