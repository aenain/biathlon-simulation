/**
 * Klasy rozszerzające funkcjonalność klas desmoj.core.simulator.
 */
package biathlon.core;

import biathlon.Biathlon;
import biathlon.event.BiathleteEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import java.util.LinkedList;

/**
 * Podstawowa klasa, reprezentująca encję.
 * Przechowuje zdarzenia pogrupowane według okrążeń, na których wystąpiły.
 * Posiada osobny trace.
 * 
 * @author Artur Hebda
 * @see desmoj.core.simulator.Entity
 */
public class Entity extends desmoj.core.simulator.Entity {
    protected String name;
    protected LinkedList<LinkedList<BiathleteEvent>> eventsByLap;
    protected biathlon.report.HTMLFileOutput trace;

    public Entity(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.name = name;

        eventsByLap = new LinkedList();
        for (int i = 0; i < Biathlon.LAPS; i++) {
            eventsByLap.add(new LinkedList());
        }
        trace = new biathlon.report.HTMLFileOutput(this, "Trace for " + this);
    }

    /**
     * Metoda, która w tej klasie tylko zamyka otwarty w konstruktorze trace.
     * W klasach dziedziczących ta metoda powinna służyć do generowania całego trace'a i jego zamykania!
     */
    public void generateTrace() {
        trace.close();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Czas życia encji w symulacji.
     * Metoda dodana dla kompatybilności z klasą StaggeredEntity
     * @return czas symulacji (encja jest tworzona w chwili t = 0)
     */
    public TimeInstant lifeTime() {
        return presentTime();
    }
}
