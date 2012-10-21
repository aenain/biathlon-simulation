/**
 * Klasy odpowiedzialne za model symulacji.
 */
package biathlon;

import desmoj.core.simulator.Model;
import java.util.LinkedList;

/**
 * Klasa reprezentująca poszczególnych zawodników
 * 
 * @author Artur Hebda
 */
public class Biathlete extends StaggeredEntity {
    protected LinkedList<LinkedList<Boolean>> shots;
    protected int currentShootingSessionMisses = 0;
    protected int currentLap = 1;

    public Biathlete(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        shots = new LinkedList();
    }

    @Override
    public String formatMessage(String message) {
        return message + " on lap " + getCurrentLap();
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
     * 
     * @return lista true/false oznaczająca, czy zawodnik trafił w danym strzale.
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
