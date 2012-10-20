package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 *
 * @author Artur Hebda
 */
public class BiathleteFinishRace extends Event<Biathlete> {
    public BiathleteFinishRace(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine(Biathlete biathlete) {
        biathlete.sendNote(" finishes the race ", this);
        Biathlon race = (Biathlon)getModel();
        race.getBiathletes().remove(biathlete);
    }
    
}
