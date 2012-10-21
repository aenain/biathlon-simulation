package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.ShootingArea;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
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
        boolean hit = model.getShotResult();

        biathlete.saveShotResult(hit);
        if (hit) { message = " shots on target "; }
        else { message = " shots and misses "; }
        biathlete.sendNote(message, this);

        // jesli nie oddal 5 strzalow w serii, powtorz strzelanie
        if (biathlete.getCurrentShootingSession().size() < 5) {
            BiathleteShot biathleteShot = new BiathleteShot(model, "BiathleteShotEvent", true);
            biathleteShot.schedule(biathlete, shootingArea, model.getShotTime());
        }
        // w przeciwnym razie dolicz ewentualne kary i odwiedz checkpoint zaraz za strzelnica
        else {
            int missCount = biathlete.countCurrentShootingSessionMisses();
            Checkpoint.scheduleArrival(shootingArea.getAfterCheckpoint(), biathlete, new TimeSpan(missCount * Biathlon.MISS_PENALTY_IN_SECONDS, TimeUnit.SECONDS));
        }
    }
}