package biathlon.event;

import biathlon.Biathlete;
import biathlon.Biathlon;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * Zdarzenie 'zawodnik ukończył bieg'
 * 
 * @author Artur Hebda
 */
public class BiathleteFinishRace extends Event<Biathlete> {
    public BiathleteFinishRace(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Wywoływane przy zajściu zdarzenia. Następuje zapis trace'a zawodnika
     * oraz usunięcie zawodnika z kolejki biatlonistów.
     * 
     * @param biathlete zawodnik
     */
    @Override
    public void eventRoutine(Biathlete biathlete) {
        Biathlon race = (Biathlon)getModel();
        race.incrementFinishCount();
    }
}
