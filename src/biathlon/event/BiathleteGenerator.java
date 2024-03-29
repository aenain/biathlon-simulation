package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.checkpoint.Checkpoint;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * Zdarzenie 'wygeneruj i wystartuj zawodnika'.
 * 
 * @author Artur Hebda
 */
public class BiathleteGenerator extends ExternalEvent {
    public static Integer number = 1;

    public BiathleteGenerator(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Wywoływane podczas zajścia zdarzenia. Tworzy nowego zawodnika oraz dodaje go
     * do kolejki biatlonistów. Następnie dopisuje do harmonogramu zdarzenie 
     * 'przybycie do punktu pomiarowego', co w praktyce oznacza start zawodnika
     * w biegu.
     */
    @Override
    public void eventRoutine() {
        Biathlon model = (Biathlon)getModel();

        // stworz zawodnika
        Biathlete biathlete = new Biathlete(model, "Biathlete: " + number, true);
        number++;

        biathlete.addEvent(new BiathleteEvent(biathlete, "starts the race"));

        // dodaj do kolejki
        model.getBiathletes().insert(biathlete);
        
        // zaschedule'uj event do nastepnego punktu pomiaru czasu
        Checkpoint.scheduleArrival(model.getCheckpoints().first(), biathlete, model.getCheckpointArrivalTime());
    }
}
