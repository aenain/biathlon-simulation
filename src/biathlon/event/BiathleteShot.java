package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.ShootingArea;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class BiathleteShot extends EventOf2Entities<Biathlete, ShootingArea> {
    public BiathleteShot(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine(Biathlete biathlete, ShootingArea shootingArea) {
        Biathlon model = (Biathlon)getModel();

        String message;
        boolean hit = true;

        biathlete.saveShotResult(hit);
        if (hit) { message = " shots on target "; }
        else { message = " shots and misses "; }
        biathlete.sendNote(message, this);

        // jesli nie oddal 5 strzalow w serii, powtorz strzelanie
        if (biathlete.getCurrentShootingSession().size() < 5) {
            BiathleteShot biathleteShot = new BiathleteShot(model, "BiathleteShotEvent", true);
            biathleteShot.schedule(biathlete, shootingArea, new TimeInstant(12, TimeUnit.SECONDS));
        }
        // w przeciwnym razie dolicz ewentualne kary i odwiedz checkpoint zaraz za strzelnica
        else {
            int missCount = biathlete.countCurrentShootingSessionMisses();
            BiathleteArrivalAtCheckpoint biathleteAtCheckpointArrival = new BiathleteArrivalAtCheckpoint(getModel(), "BiathletAtCheckpointArrivalEvent", true);
            biathleteAtCheckpointArrival.schedule(biathlete, shootingArea.getAfterCheckpoint(), new TimeInstant(missCount * Biathlon.MISS_PENALTY_IN_SECONDS, TimeUnit.SECONDS));
        }
    }
    
}