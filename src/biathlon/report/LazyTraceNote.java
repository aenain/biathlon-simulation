package biathlon.report;

import biathlon.Entity;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 *
 * @author Artur Hebda
 */
public class LazyTraceNote extends TraceNote implements Comparable {
    protected BiathleteEvent biathleteEvent;

    public LazyTraceNote(Model origin, String message, TimeInstant time, Entity entityInvolved, EventAbstract eventInvolved) {
        super(origin, message, time, entityInvolved, eventInvolved);
    }

    public void setBiathleteEvent(BiathleteEvent event) {
        this.biathleteEvent = event;
    }

    public BiathleteEvent getBiathleteEvent() {
        return biathleteEvent;
    }
    
    @Override
    public int compareTo(Object o) {
        LazyTraceNote other = (LazyTraceNote)o;
        return biathleteEvent.compareTo(other.getBiathleteEvent());
    }
}
