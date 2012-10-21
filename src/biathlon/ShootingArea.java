package biathlon;

import biathlon.checkpoint.AfterShootingArea;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.Model;

/**
 * Reprezentuje strzelnicę.
 * 
 * @author Artur Hebda
 */
public class ShootingArea extends Entity {
    protected AfterShootingArea afterCheckpoint;

    public ShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace, true);
    }

    public void setAfterCheckpoint(Checkpoint checkpoint) {
        this.afterCheckpoint = (AfterShootingArea)checkpoint;
    }

    public AfterShootingArea getAfterCheckpoint() {
        return afterCheckpoint;
    }
}
