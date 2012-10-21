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
     * Wywoływane przy zajściu zdarzenia.
     * Jest zwiększana liczba zawodników, którzy skończyli wyścig.
     * 
     * @param biathlete zawodnik
     */
    @Override
    public void eventRoutine(Biathlete biathlete) {
        Biathlon race = (Biathlon)getModel();
        race.incrementFinishCount();
    }
}
