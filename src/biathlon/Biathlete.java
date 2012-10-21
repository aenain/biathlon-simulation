package biathlon;

import desmoj.core.simulator.Model;
import java.util.LinkedList;

/**
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
    
    public void startNextLap() {
        currentLap++;
    }

    public Integer getCurrentLap() {
        return currentLap;
    }
    
    public void beginShootingSession() {
        shots.add(new LinkedList());
        currentShootingSessionMisses = 0;
    }

    public LinkedList<Boolean> getCurrentShootingSession() {
        return shots.getLast();
    }

    public void saveShotResult(boolean hit) {
        getCurrentShootingSession().add(hit);
        if (!hit) {
            currentShootingSessionMisses++;
        }
    }

    public int countCurrentShootingSessionMisses() {
        return currentShootingSessionMisses;
    }
}
