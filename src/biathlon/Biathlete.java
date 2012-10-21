/**
 * Klasy odpowiedzialne za model symulacji.
 */
package biathlon;

import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.Model;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Klasa reprezentująca poszczególnych zawodników
 * 
 * @author Artur Hebda
 */
public class Biathlete extends biathlon.core.StaggeredEntity {
    protected LinkedList<LinkedList<Boolean>> shots;
    protected int currentShootingSessionMisses = 0;
    protected int currentLap = 1;

    public Biathlete(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        shots = new LinkedList();
    }

    public void addEvent(BiathleteEvent event) {
        eventsByLap.get(currentLap - 1).add(event);
    }

    @Override
    public void generateTrace() {
        LinkedList<BiathleteEvent> events;

        for (int i = 0; i < Biathlon.LAPS; i++) {
            events = eventsByLap.get(i);
            Collections.sort(events);
            trace.startSection("Lap " + (i + 1));
            trace.startTable("Position", "Description", "Time", "Lost");
        
            for (BiathleteEvent event : events) {
                trace.insertRow(event.getRank(), event.getMessage(), event.getBiathleteTime(), event.getLost());
            }

            trace.closeTable();
            trace.closeSection();
        }

        trace.close();
    }

    /**
     * Rozpoczyna kolejne okrążenie podczas biegu zawodnika.
     */
    public void startNextLap() {
        currentLap++;
    }

    public Integer getCurrentLap() {
        return currentLap;
    }
    
    /**
     * Rozpoczyna nową serię strzałów.
     */
    public void beginShootingSession() {
        shots.add(new LinkedList());
        currentShootingSessionMisses = 0;
    }

    /*
     * @return lista list true/false oznaczająca, czy zawodnik trafił w kolejnych strzałach w kolejnych seriach
     */
    public LinkedList<LinkedList<Boolean>> getShotResults() {
        return shots;
    }

    /**
     * 
     * @return lista true/false oznaczająca, czy zawodnik trafił w kolejnych strzałach obecnej / ostatniej serii strzałów
     */
    public LinkedList<Boolean> getCurrentShootingSession() {
        return shots.getLast();
    }

    /**
     * Zapisuje rezultat danego strzału.
     * 
     * @param hit true - trafione, false - pudło
     */
    public void saveShotResult(boolean hit) {
        getCurrentShootingSession().add(hit);
        if (!hit) {
            currentShootingSessionMisses++;
        }
    }

    /**
     * 
     * @return ilość spudłowanych strzałów w danej serii 
     */
    public int countCurrentShootingSessionMisses() {
        return currentShootingSessionMisses;
    }
}
