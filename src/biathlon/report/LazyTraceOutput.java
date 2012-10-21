package biathlon.report;

import biathlon.Entity;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.EventAbstract;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author Artur Hebda
 */
public class LazyTraceOutput extends TraceOutput {
    protected LinkedList<LazyTraceNote> notes;

    public LazyTraceOutput(Entity entity) {
        super(entity);
        notes = new LinkedList();
    }

    @Override
    public void sendNote(String message, EventAbstract event, BiathleteEvent biathleteEvent) {
        LazyTraceNote note = new LazyTraceNote(entity.getModel(), entity.formatMessage(message), biathleteEvent.getBiathleteTime(), entity, event);
        note.setBiathleteEvent(biathleteEvent);
        notes.add(note);
    }

    @Override
    public void flushAndClose() {
        Collections.sort(notes);
        for (LazyTraceNote note : notes) {
            receive(note);
        }
        close();
    }
}
