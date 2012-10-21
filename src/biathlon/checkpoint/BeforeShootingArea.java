package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.event.BiathleteShot;
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
     * Gdy zawodnik dotrze do punktu tuż przed strzelnicą, zacznie strzelać.
     * W związku z tym jest rozpoczynana nowa seria strzałów i schedule'owany event pierwszego strzału.
     * 
     * @param biathlete zawodnik, który dotarł do tego punktu pomiaru
     * @see biathlon.event.BiathleteShot
     */
    @Override
    public void biathleteArrived(Biathlete biathlete) {
        storeBiathleteArrival(biathlete);
        biathlete.beginShootingSession();

        Biathlon model = (Biathlon)getModel();
        BiathleteShot biathleteShot = new BiathleteShot(getModel(), "BiathleteShotEvent", true);
        biathleteShot.schedule(biathlete, getShootingArea(), model.getShotTime());
    }
}
