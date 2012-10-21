package biathlon;

import desmoj.core.report.HTMLTraceOutput;
import desmoj.core.report.TraceNote;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 *
 * @author Artur Hebda
 */
public class Entity extends desmoj.core.simulator.Entity {
    protected HTMLTraceOutput trace = new HTMLTraceOutput();
    protected String name;

    public Entity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.name = name;

        trace.open("traces/" + this.getClass().getName(), name);
        trace.setTimeFloats(100);
    }

    @Override
    public String toString() {
        return name;
    }

    public String formatMessage(String message) {
        return name + " " + message;
    }

    public void sendNote(String message, EventAbstract event) {
        TraceNote note = new TraceNote(getModel(), formatMessage(message), getPresentTime(), this, event);
        trace.receive(note);
    }

    public TimeInstant getPresentTime() {
        return presentTime();
    }
}