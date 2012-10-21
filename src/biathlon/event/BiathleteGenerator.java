package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class BiathleteGenerator extends ExternalEvent {
    public static Integer number = 1;

    public BiathleteGenerator(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine() {
        Biathlon model = (Biathlon)getModel();

        // stworz zawodnika
        Biathlete biathlete = new Biathlete(model, "Biathlete: " + number, true);
        number++;

        biathlete.sendNote(" has been created and starts the race!", this);
        // dodaj do kolejki
        model.getBiathletes().insert(biathlete);
        
        // zaschedule'uj event do nastepnego punktu pomiaru czasu
        Checkpoint.scheduleArrival(model.getCheckpoints().first(), biathlete, new TimeSpan(12, TimeUnit.MINUTES));
    }
}
