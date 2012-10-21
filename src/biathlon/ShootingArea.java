package biathlon;

import biathlon.checkpoint.AfterShootingArea;
import biathlon.checkpoint.Checkpoint;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.Model;
import java.util.LinkedList;

/**
 * Klasa reprezentująca strzelnicę jako obiekt na trasie.
 * 
 * @author Artur Hebda
 * @see biathlon.core.Entity
 */
public class ShootingArea extends biathlon.core.Entity {
    protected AfterShootingArea afterCheckpoint;

    public ShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Zapisuje informację o oddanym strzale i dodaje event do zawodnika.
     * @param biathlete zawodnik
     * @param message opis uwzględniający, czy udało się trafić
     */
    public void storeShot(Biathlete biathlete, String message) {
        BiathleteEvent shot = new BiathleteEvent(biathlete, message);
        eventsByLap.get(biathlete.getCurrentLap() - 1).add(shot);
        biathlete.addEvent(shot);
    }

    /**
     * Generuje trace dla strzelnicy.
     */
    @Override
    public void generateTrace() {
        LinkedList<BiathleteEvent> events;
        BiathleteEvent event;

        for (int i = 0; i < Biathlon.LAPS; i++) {
            events = eventsByLap.get(i);
            trace.startSection("Lap " + (i + 1));
            trace.startTable("Name", "Result", "Biathlete Time", "Simulation Time");
        
            for (int j = 0; j < events.size(); j++) {
                event = events.get(j);
                trace.insertRow(event.getBiathlete(),
                                event.getMessage(),
                                event.getBiathleteTime(),
                                event.getSimulationTime());
            }

            trace.closeTable();
            trace.closeSection();
        }

        trace.close();
    }

    /**
     * Ustawia następny punkt kontrolny, będący zaraz za strzelnicą.
     * Na tym punkcie będą uwzględniane kary za niecelne strzały.
     * Sygnatura metody wynika z chęci zachowania kompatybilności z innymi obiektami na trasie.
     * 
     * @param checkpoint punkt kontrolny za strzelnicą (musi być klasy biathlon.checkpoint.AfterShootingArea).
     * @see biathlon.checkpoint.AfterShootingArea
     */
    public void setAfterCheckpoint(Checkpoint checkpoint) {
        this.afterCheckpoint = (AfterShootingArea)checkpoint;
    }

    public AfterShootingArea getAfterCheckpoint() {
        return afterCheckpoint;
    }
}
