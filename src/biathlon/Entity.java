package biathlon;

import biathlon.event.BiathleteEvent;
import biathlon.report.LazyTraceOutput;
import biathlon.report.TraceOutput;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 *
 * @author Artur Hebda
 */
public class Entity extends desmoj.core.simulator.Entity {
    protected TraceOutput trace;
    protected String name;

    public Entity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.name = name;
        setTrace(new TraceOutput(this));
    }

    public Entity(Model owner, String name, boolean showInTrace, boolean lazyOutput) {
        super(owner, name, showInTrace);
        this.name = name;

        if (lazyOutput) {
            setTrace(new LazyTraceOutput(this));
        }
        else {
            setTrace(new TraceOutput(this));
        }
    }

    protected final void setTrace(TraceOutput trace) {
        this.trace = trace;
        Biathlon model = (Biathlon)getModel();
        model.addTrace(trace);
    }

    @Override
    public String toString() {
        return name;
    }

    public String formatMessage(String message) {
        return message;
    }

    public void sendNote(String message, EventAbstract event) {
        trace.sendNote(formatMessage(message), event, null);
    }

    public void sendNote(String message, EventAbstract event, BiathleteEvent biathleteEvent) {
        trace.sendNote(formatMessage(message), event, biathleteEvent);
    }

    public TimeInstant lifeTime() {
        return presentTime();
    }
}
