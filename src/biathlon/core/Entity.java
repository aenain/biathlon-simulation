package biathlon.core;

import biathlon.Biathlon;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import java.util.LinkedList;

/**
 *
 * @author Artur Hebda
 */
public class Entity extends desmoj.core.simulator.Entity {
    protected String name;
    protected LinkedList<LinkedList<BiathleteEvent>> eventsByLap;
    protected biathlon.report.HTMLFileOutput trace;

    public Entity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.name = name;

        eventsByLap = new LinkedList();
        for (int i = 0; i < Biathlon.LAPS; i++) {
            eventsByLap.add(new LinkedList());
        }

        trace = new biathlon.report.HTMLFileOutput(this, "Trace for: " + this);
    }

    public void generateTrace() {
        trace.close();
    }

    @Override
    public String toString() {
        return name;
    }

    public String formatMessage(String message) {
        return message;
    }

    public TimeInstant lifeTime() {
        return presentTime();
    }
}
