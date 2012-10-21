package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.event.BiathleteEvent;
import biathlon.event.BiathleteFinishRace;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import java.util.Collections;
import java.util.LinkedList;

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
        if (biathlete.getCurrentLap() >= Biathlon.LAPS) {
            BiathleteEvent finish = new BiathleteEvent(biathlete, "finishes the race");
            biathlete.addEvent(finish);
            eventsByLap.getLast().add(finish);

            BiathleteFinishRace biathleteFinishRace = new BiathleteFinishRace(getModel(), "BiathleteFinishRaceEvent", true);
            biathleteFinishRace.schedule(biathlete, presentTime());
            if (eventsByLap.getLast().size() == Biathlon.BIATHLETE_COUNT) {
                generateFinalTrace();
            }
        }
        else {
            super.biathleteArrived(biathlete, event);
            biathlete.startNextLap();
        }
    }

    public void generateFinalTrace() {
        biathlon.report.HTMLFileOutput html = new biathlon.report.HTMLFileOutput(this, "Final Results", "final_results");
        html.startSection("Results");
        html.startTable("Position", "Name", "Missed Shots", "Time", "Lost");
        LinkedList<BiathleteEvent> events = eventsByLap.getLast();

        Collections.sort(events);
        TimeInstant firstEventAt = events.getFirst().getBiathleteTime();
        BiathleteEvent event;
        for (int i = 0; i < events.size(); i++) {
            event = events.get(i);
            StringBuilder missedShots = new StringBuilder(" (");
            int missedShotCount = 0;
            int missedShotOnLapCount = 0;
            for (LinkedList<Boolean> shotsOnLap : event.getBiathlete().getShotResults()) {
                missedShotOnLapCount = 0;
                for (Boolean shot : shotsOnLap) {
                    if (! shot) {
                        missedShotOnLapCount++;
                    }
                }
                missedShots.append(missedShotOnLapCount).append("+");
                missedShotCount += missedShotOnLapCount;
            }
            missedShots.insert(0, missedShotCount);
            missedShots.setCharAt(missedShots.length() - 1, ')'); // zamien zbedny '+' na ')'
            html.insertRow(new Integer(i + 1),
                           event.getBiathlete(),
                           missedShots,
                           event.getBiathleteTime(),
                           TimeOperations.diff(firstEventAt, event.getBiathleteTime()));
        }

        html.closeTable();
        html.closeSection();
        html.close();
    }
}
