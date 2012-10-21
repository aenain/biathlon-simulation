package biathlon;

import biathlon.checkpoint.AfterShootingArea;
import biathlon.checkpoint.Checkpoint;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.Model;
import java.util.LinkedList;

/**
 * Reprezentuje strzelnicę.
 * 
 * @author Artur Hebda
 */
public class ShootingArea extends biathlon.core.Entity {
    protected AfterShootingArea afterCheckpoint;

    public ShootingArea(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void storeShot(Biathlete biathlete, String message) {
        BiathleteEvent shot = new BiathleteEvent(biathlete, message);
        eventsByLap.get(biathlete.getCurrentLap() - 1).add(shot);
        biathlete.addEvent(shot);
    }

    @Override
    public void generateTrace() {
        LinkedList<BiathleteEvent> events;
        BiathleteEvent event;

        for (int i = 0; i < Biathlon.LAPS; i++) {
            events = eventsByLap.get(i);
            trace.startSection("Lap " + (i + 1));
            trace.startTable("Name", "Result", "Biathlete Time", "Simulation Time");
        
            for (int j = 0; j < events.size(); j++) {
                event = events.get(j);
                trace.insertRow(event.getBiathlete(),
                                event.getMessage(),
                                event.getBiathleteTime(),
                                event.getSimulationTime());
            }

            trace.closeTable();
            trace.closeSection();
        }

        trace.close();
    }

    public void setAfterCheckpoint(Checkpoint checkpoint) {
        this.afterCheckpoint = (AfterShootingArea)checkpoint;
    }

    public AfterShootingArea getAfterCheckpoint() {
        return afterCheckpoint;
    }
}
