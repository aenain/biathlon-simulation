package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.event.BiathleteShot;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;

/**
 * Klasa reprezentująca punkt pomiaru znajdujący się tuż przed strzelnicą.
 * 
 * @author Artur Hebda
 */
public class BeforeShootingArea extends ShootingArea {
    public BeforeShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Obsługuje sytuację, gdy zawodnik przybędzie do tego punktu pomiaru.
     * 
     * @param biathlete zawodnik przybywający do punktu pomiaru
     * @param event bieżące zdarzenie
     */
    @Override
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        storeBiathleteArrival(biathlete);
        biathlete.beginShootingSession();

        Biathlon model = (Biathlon)getModel();
        BiathleteShot biathleteShot = new BiathleteShot(getModel(), "BiathleteShotEvent", true);
        biathleteShot.schedule(biathlete, getShootingArea(), model.getShotTime());
    }
}
