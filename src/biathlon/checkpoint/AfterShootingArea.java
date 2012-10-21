/**
 * Klasy związane z obsługą punktów pomiaru czasu
 */
package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.EventAbstract;
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

    @Override
    public void setShootingArea(biathlon.ShootingArea shootingArea) {
        this.shootingArea = shootingArea;
        shootingArea.setAfterCheckpoint(this);
    }

    /**
     * Obsługuje sytuację, gdy zawodnik przybędzie do tego punktu pomiaru
     * 
     * @param biathlete zawodnik przybywający do punktu pomiaru
     * @param event bieżące zdarzenie
     */
    @Override
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        biathlete.sendNote(" leaves " + getShootingArea(), event);
        sendNote(biathlete.toString() + " arrives on lap " + biathlete.getCurrentLap(), event, new BiathleteEvent(biathlete));
        scheduleNextCheckpoint(biathlete);
    }
}
