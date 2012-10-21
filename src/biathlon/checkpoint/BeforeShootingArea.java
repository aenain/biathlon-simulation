package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.event.BiathleteEvent;
import biathlon.event.BiathleteShot;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;

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
        sendNote(biathlete + " arrives on lap " + biathlete.getCurrentLap(), event, new BiathleteEvent(biathlete));
        biathlete.beginShootingSession();

        Biathlon model = (Biathlon)getModel();
        BiathleteShot biathleteShot = new BiathleteShot(getModel(), "BiathleteShotEvent", true);
        biathleteShot.schedule(biathlete, getShootingArea(), model.getShotTime());
    }
}
