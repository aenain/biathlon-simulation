package biathlon.checkpoint;

import biathlon.Biathlete;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;

/**
 *
 * @author Artur Hebda
 */
public class AfterShootingArea extends ShootingArea {
    public AfterShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void setShootingArea(biathlon.ShootingArea shootingArea) {
        this.shootingArea = shootingArea;
        shootingArea.setAfterCheckpoint(this);
    }

    @Override
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        biathlete.sendNote(" leaves " + getShootingArea(), event);
        scheduleNextCheckpoint(biathlete);
    }
}