package biathlon;

import biathlon.checkpoint.AfterShootingArea;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 *
 * @author Artur Hebda
 */
public class ShootingArea extends Entity {
    protected AfterShootingArea afterCheckpoint;

    public ShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void setAfterCheckpoint(Checkpoint checkpoint) {
        this.afterCheckpoint = (AfterShootingArea)checkpoint;
    }

    public AfterShootingArea getAfterCheckpoint() {
        return afterCheckpoint;
    }
}
