package biathlon.report;

import biathlon.Entity;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 *
 * @author Artur Hebda
 */
public class TraceNote extends desmoj.core.report.TraceNote {
    public TraceNote(Model origin, String message, TimeInstant time, Entity entityInvolved, EventAbstract eventInvolved) {
        super(origin, message, time, entityInvolved, eventInvolved);
    }
}
