package biathlon;

import desmoj.core.simulator.ModelCondition;

/**
 * Klasa odpowiedzialna za warunek zakończenia symulacji.
 * Warunkiem jest dotarcie przez wszystkich zawodników do mety.
 * 
 * @author Artur Hebda
 * @see desmoj.core.simulator.ModelCondition
 */
public class StopCondition extends ModelCondition {
    private Biathlon race;

    public StopCondition(Biathlon race, String name, boolean showInTrace, Object... args) {
        super(race, name, showInTrace, args);
        this.race = race;
    }

    /**
     * 
     * @return true (wszyscy zawodnicy dobiegli do mety i można zakończyć symulację)
     */
    @Override
    public boolean check() {
        return race.haveAllBiathletesFinished();
    }
    
}
