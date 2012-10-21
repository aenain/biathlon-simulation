package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.event.BiathleteEvent;
import biathlon.event.BiathleteFinishRace;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;

/**
 * Klasa opisująca punkt pomiaru znajdujący się na linii początku okrążenia.
 * 
 * @author Artur Hebda
 */
public class StartFinish extends Checkpoint {
    public StartFinish(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Obsługuje sytuację, gdy zawodnik przybędzie do tego punktu pomiaru.
     * 
     * @param biathlete zawodnik przybywający do punktu pomiaru
     * @param event bieżące zdarzenie
     */
    @Override
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        biathlete.startNextLap();

        if (biathlete.getCurrentLap() > Biathlon.LAPS) {
            BiathleteFinishRace biathleteFinishRace = new BiathleteFinishRace(getModel(), "BiathleteFinishRaceEvent", true);
            trace.sendNote(biathlete.toString() + " finishes the race", event, new BiathleteEvent(biathlete));
            biathleteFinishRace.schedule(biathlete, presentTime());
        }
        else {
            biathlete.sendNote(" arrives to " + this, event);
            trace.sendNote(biathlete.toString() + " starts lap " + biathlete.getCurrentLap(), event, new BiathleteEvent(biathlete));
            scheduleNextCheckpoint(biathlete);
        }
    }
}
