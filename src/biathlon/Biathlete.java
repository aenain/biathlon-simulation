/**
 * Podstawowy pakiet symulacji.
 */
package biathlon;

import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.Model;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Klasa reprezentująca poszczególnych zawodników.
 * 
 * @author Artur Hebda
 * @see biathlon.core.StaggeredEntity
 */
public class Biathlete extends biathlon.core.StaggeredEntity {
    
    protected LinkedList<LinkedList<Boolean>> shots;
    protected int currentShootingSessionMisses = 0;
    protected int currentLap = 1;

    /**
     * @see biathlon.core.StaggeredEntity 
     */
    public Biathlete(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        shots = new LinkedList();
    }

    /**
     * Dodaje event na obecnym okrążeniu.
     * @param event reprezentuje event, którego czas musiał zostać zamrożony dla chwili wystąpienia, np. oddanie strzału 
     */
    public void addEvent(BiathleteEvent event) {
        eventsByLap.get(currentLap - 1).add(event);
    }

    /**
     * Generuje trace dla zawodnika z przebiegu całego wyścigu.
     * Przed wywołaniem tej metody należy wywołać analogiczną metodę
     * dla punktów pomiaru czasu i strzelnicy (ustalenie pozycji i straty do lidera
     * na danym punkcie).
     */
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

    /**
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
     * @param hit true (trafienie), false (pudło)
     */
    public void saveShotResult(boolean hit) {
        getCurrentShootingSession().add(hit);
        if (!hit) {
            currentShootingSessionMisses++;
        }
    }

    /**
     * 
     * @return ilość niecelnych strzałów w obecnej / ostatniej serii 
     */
    public int countCurrentShootingSessionMisses() {
        return currentShootingSessionMisses;
    }
}
