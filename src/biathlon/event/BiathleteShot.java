package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.ShootingArea;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;

/**
 * Zdarzenie 'pojedynczy strzał'
 * 
 * @author Artur Hebda
 */
public class BiathleteShot extends EventOf2Entities<Biathlete, ShootingArea> {
    public BiathleteShot(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Wywoływane podczas zajścia zdarzenia. Losuje i zapisuje rezultat danego strzału.
     * Następnie, jeśli ilość wykonanych strzałów jest mniejsza niż 5, to dodaje
     * do harmonogramu zdarzenie kolejnego strzału. W przeciwnym wypadku oblicza
     * karny czas.
     * 
     * @param biathlete
     * @param shootingArea 
     */
    @Override
    public void eventRoutine(Biathlete biathlete, ShootingArea shootingArea) {
        Biathlon model = (Biathlon)getModel();

        String message;
        boolean hit = model.getShotResult();

        biathlete.saveShotResult(hit);
        if (hit) { message = " shots on target "; }
        else { message = " shots and misses "; }
        biathlete.sendNote(message, this);
        shootingArea.sendNote(biathlete.toString() + message + "on lap " + biathlete.getCurrentLap(), this, new BiathleteEvent(biathlete));

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