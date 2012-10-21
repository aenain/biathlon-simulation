package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Entity;
import biathlon.event.BiathleteArrivalAtCheckpoint;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class Checkpoint extends Entity {
    protected Checkpoint nextCheckpoint;

    public Checkpoint(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void setNextCheckpoint(Checkpoint checkpoint) {
        this.nextCheckpoint = checkpoint;
    }

    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        biathlete.sendNote(" arrives to " + this, event);
        scheduleNextCheckpoint(biathlete);
    }

    public void scheduleNextCheckpoint(Biathlete biathlete) {
        Checkpoint.scheduleArrival(nextCheckpoint, biathlete, new TimeInstant(12, TimeUnit.MINUTES));
    }

    public static void scheduleArrival(Checkpoint checkpoint, Biathlete biathlete, TimeInstant at) {
        BiathleteArrivalAtCheckpoint arrivalAtCheckpoint = new BiathleteArrivalAtCheckpoint(checkpoint.getModel(), "BiathletAtCheckpointArrivalEvent", true);
        arrivalAtCheckpoint.schedule(biathlete, checkpoint, at);
    }
}