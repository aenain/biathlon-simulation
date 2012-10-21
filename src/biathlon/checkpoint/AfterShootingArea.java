/**
 * Klasy reprezentujące różne punkty pomiaru czasu.
 */
package biathlon.checkpoint;

import biathlon.Biathlete;
import desmoj.core.simulator.Model;

/**
 * Klasa reprezentująca punkt pomiaru czasu znajdujący się za strzelnicą.
 * 
 * @author Artur Hebda
 */
public class AfterShootingArea extends ShootingArea {
    public AfterShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Łączy ten obiekt ze strzelnicą.
     * @param shootingArea 
     */
    @Override
    public void setShootingArea(biathlon.ShootingArea shootingArea) {
        this.shootingArea = shootingArea;
        shootingArea.setAfterCheckpoint(this);
    }

    /**
     * Ten event jest schedule'owany zgodnie z opóźnieniami wynikającymi z kar za niecelne strzały.
     * 
     * @param biathlete zawodnik, który opuszcza strzelnicę
     */
    @Override
    public void biathleteArrived(Biathlete biathlete) {
        storeBiathleteArrival(biathlete);
        scheduleNextCheckpoint(biathlete);
    }
}
