package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.Entity;
import biathlon.event.BiathleteArrivalAtCheckpoint;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

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
        Biathlon model = (Biathlon)getModel();
        Checkpoint.scheduleArrival(nextCheckpoint, biathlete, model.getCheckpointArrivalTime());
    }

    public static void scheduleArrival(Checkpoint checkpoint, Biathlete biathlete, TimeSpan delay) {
        TimeInstant now = checkpoint.presentTime();
        scheduleArrival(checkpoint, biathlete, TimeOperations.add(now, delay));
    }

    public static void scheduleArrival(Checkpoint checkpoint, Biathlete biathlete, TimeInstant at) {
        BiathleteArrivalAtCheckpoint arrivalAtCheckpoint = new BiathleteArrivalAtCheckpoint(checkpoint.getModel(), "BiathletAtCheckpointArrivalEvent", true);
        arrivalAtCheckpoint.schedule(biathlete, checkpoint, at);
    }
}
