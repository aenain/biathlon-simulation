package biathlon.checkpoint;

import biathlon.Biathlete;
import biathlon.Biathlon;
import biathlon.core.Entity;
import biathlon.event.BiathleteArrivalAtCheckpoint;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Klasa odpowiedzialna za punkt pomiaru czasu. Dziedziczą z niej klasy
 * odpowiadające za różne typy punktów.
 * 
 * @author Artur Hebda
 */
public class Checkpoint extends Entity {
    protected Checkpoint nextCheckpoint;

    public Checkpoint(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void generateTrace() {
        LinkedList<BiathleteEvent> events;
        BiathleteEvent event;
        TimeInstant firstEventAt;

        for (int i = 0; i < Biathlon.LAPS; i++) {
            events = eventsByLap.get(i);
            Collections.sort(events);
            trace.startSection("Lap " + (i + 1));
            trace.startTable("Position", "Name", "Time", "Lost");
        
            if (events.size() > 0) {
                firstEventAt = events.getFirst().getBiathleteTime();
                for (int j = 0; j < events.size(); j++) {
                    event = events.get(j);
                    event.setRank(new Integer(j + 1));
                    event.setLost(TimeOperations.diff(firstEventAt, event.getBiathleteTime()));
                    trace.insertRow(event.getRank(),
                                    event.getBiathlete(),
                                    event.getBiathleteTime(),
                                    event.getLost());
                }
            }
            trace.closeTable();
            trace.closeSection();
        }

        trace.close();
    }

    public void setNextCheckpoint(Checkpoint checkpoint) {
        this.nextCheckpoint = checkpoint;
    }

    /**
     * Obsługuje sytuację, gdy zawodnik przybędzie do tego punktu pomiaru.
     * 
     * @param biathlete zawodnik przybywający do punktu pomiaru
     * @param event bieżące zdarzenie
     */
    public void biathleteArrived(Biathlete biathlete, EventAbstract event) {
        storeBiathleteArrival(biathlete);
        scheduleNextCheckpoint(biathlete);
    }

    /*
     * Tworzy BiathleteEvent i dodaje go do listy zdarzeń zawodnika i punktu pomiaru czasu.
     */
    protected void storeBiathleteArrival(Biathlete biathlete) {
        BiathleteEvent arrival = new BiathleteEvent(biathlete, "arrives at " + this);
        biathlete.addEvent(arrival);
        eventsByLap.get(biathlete.getCurrentLap() - 1).add(arrival);
    }

    /**
     * Dopisuje do harmonogramu zdarzenie pojawienia się przy kolejnym punkcie pomiarowym.
     * 
     * @param biathlete zawodnik
     */
    public void scheduleNextCheckpoint(Biathlete biathlete) {
        Biathlon model = (Biathlon)getModel();
        Checkpoint.scheduleArrival(nextCheckpoint, biathlete, model.getCheckpointArrivalTime());
    }

    /**
     * Dopisuje do harmonogramu zdarzenie pojawienia się przy podanym punkcie pomiarowym, 
     * z uwzględnieniem opóźnienia czasu.
     * 
     * @param checkpoint zadany punkt pomiaru
     * @param biathlete zawodnik
     * @param delay opóźnienie 
     */
    public static void scheduleArrival(Checkpoint checkpoint, Biathlete biathlete, TimeSpan delay) {
        TimeInstant now = checkpoint.presentTime();
        scheduleArrival(checkpoint, biathlete, TimeOperations.add(now, delay));
    }

    /**
     * Dopisuje do harmonogramu zdarzenie pojawienia się przy podanym punkcie pomiarowym, 
     * z podaniem konkretnego czasu przybycia.
     * 
     * @param checkpoint zadany punkt pomiaru
     * @param biathlete zawodnik
     * @param at czas przybycia
     */
    public static void scheduleArrival(Checkpoint checkpoint, Biathlete biathlete, TimeInstant at) {
        BiathleteArrivalAtCheckpoint arrivalAtCheckpoint = new BiathleteArrivalAtCheckpoint(checkpoint.getModel(), "BiathletAtCheckpointArrivalEvent", true);
        arrivalAtCheckpoint.schedule(biathlete, checkpoint, at);
    }
}
