package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.event.BiathleteFinishRace;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;

/**
 *
 * @author Artur Hebda
 */
public class StartFinish extends Checkpoint {
    public StartFinish(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        biathlete.startNextLap();

        if (biathlete.getCurrentLap() > Biathlon.LAPS) {
            BiathleteFinishRace biathleteFinishRace = new BiathleteFinishRace(getModel(), "BiathleteFinishRaceEvent", true);
            biathleteFinishRace.schedule(biathlete, presentTime());
        }
        else {
            biathlete.sendNote(" arrives to " + this, event);
            scheduleNextCheckpoint(biathlete);
        }
    }
}
