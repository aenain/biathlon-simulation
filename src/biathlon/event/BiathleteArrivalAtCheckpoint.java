/**
 * Klasy związane z obsługą zdarzeń.
 */
package biathlon.event;

import biathlon.Biathlete;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

/**
 * Zdarzenie 'przybycie do punktu pomiarowego'
 * 
 * @author Artur Hebda
 */
public class BiathleteArrivalAtCheckpoint extends EventOf2Entities<Biathlete, Checkpoint> {
    public BiathleteArrivalAtCheckpoint(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * To co się dzieje, kiedy wystąpi podane zdarzenie. Następuje wywołanie 
     * metody biathleteArrived danego punktu pomiaru.
     * 
     * @param biathlete zawodnik
     * @param checkpoint punkt pomiaru
     */
    @Override
    public void eventRoutine(Biathlete biathlete, Checkpoint checkpoint) {
        checkpoint.biathleteArrived(biathlete, this);
    }
}
