package biathlon.report;

import biathlon.Entity;
import biathlon.event.BiathleteEvent;
import desmoj.core.report.TraceNote;
import desmoj.core.simulator.EventAbstract;

/**
 *
 * @author Artur Hebda
 */
public class TraceOutput extends desmoj.core.report.HTMLTraceOutput {
    protected Entity entity;

    public TraceOutput(Entity entity) {
        super();
        this.entity = entity;
        open("traces/" + entity.getClass().getName(), entity.toString());
        setTimeFloats(100);
    }

    public void sendNote(String message, EventAbstract event, BiathleteEvent _) {
        TraceNote note = new TraceNote(entity.getModel(), entity.formatMessage(message), entity.lifeTime(), entity, event);
        receive(note);
    }

    public void flushAndClose() {
        close();
    }
}
