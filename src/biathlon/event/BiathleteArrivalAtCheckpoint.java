package biathlon.event;

import biathlon.Biathlete;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

/**
 *
 * @author Artur Hebda
 */
public class BiathleteArrivalAtCheckpoint extends EventOf2Entities<Biathlete, Checkpoint> {
    public BiathleteArrivalAtCheckpoint(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine(Biathlete biathlete, Checkpoint checkpoint) {
        checkpoint.biathleteArrived(biathlete, this);
    }
}
