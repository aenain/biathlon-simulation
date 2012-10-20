package biathlon.checkpoint;

import desmoj.core.simulator.Model;

/**
 *
 * @author Artur Hebda
 */
public class ShootingArea extends Checkpoint {
    protected biathlon.ShootingArea shootingArea;

    public ShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void setShootingArea(biathlon.ShootingArea shootingArea) {
        this.shootingArea = shootingArea;
    }

    public biathlon.ShootingArea getShootingArea() {
        return this.shootingArea;
    }
}
