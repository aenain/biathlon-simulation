package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.event.BiathleteShot;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class BeforeShootingArea extends ShootingArea {
    public BeforeShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        biathlete.sendNote(" arrives to " + getShootingArea(), event);
        BiathleteShot biathleteShot = new BiathleteShot(getModel(), "BiathleteShotEvent", true);
        biathleteShot.schedule(biathlete, getShootingArea(), new TimeInstant(12, TimeUnit.SECONDS));
    }
}