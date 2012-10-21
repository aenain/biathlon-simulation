package biathlon.event;

import biathlon.Biathlete;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 
 * @author Artur Hebda
 */
public class BiathleteEvent implements Comparable {
    protected Biathlete biathlete;
    protected TimeInstant biathleteTime, simulationTime;
    protected Integer rank;
    protected TimeSpan lost;
    protected String message;

    public BiathleteEvent(Biathlete biathlete) {
        storeCurrentTimes(biathlete);
        this.biathlete = biathlete;
    }

    public BiathleteEvent(Biathlete biathlete, String message) {
        storeCurrentTimes(biathlete);
        this.message = message;
        this.biathlete = biathlete;
    }

    // workaround braku mozliwosci skopiowania obiektu TimeInstant
    protected final void storeCurrentTimes(Biathlete biathlete) {
        biathleteTime = new TimeInstant(biathlete.lifeTime().getTimeAsDouble(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        simulationTime = new TimeInstant(biathlete.presentTime().getTimeAsDouble(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    public void setRank(int rank) {
        this.rank = rank;
        if (this.rank == 1) {
            this.lost = TimeSpan.ZERO;
        }
    }

    public Integer getRank() {
        return rank;
    }

    public void setLost(TimeSpan lost) {
        this.lost = lost;
    }

    public TimeSpan getLost() {
        return lost;
    }

    public TimeInstant getBiathleteTime() {
        return biathleteTime;
    }

    public TimeInstant getSimulationTime() {
        return simulationTime;
    }

    public String getMessage() {
        return message;
    }

    public Biathlete getBiathlete() {
        return biathlete;
    }

    @Override
    public int compareTo(Object o) {
        BiathleteEvent other = (BiathleteEvent)o;
        return biathleteTime.compareTo(other.getBiathleteTime());
    }
}
