package biathlon;

import desmoj.core.simulator.ModelCondition;

/**
 * Klasa odpowiedzialna za warunek zakończenia symulacji - kiedy wszyscy 
 * zawodnicy dobiegną do mety.
 * 
 * @author Artur Hebda
 */
public class StopCondition extends ModelCondition {
    private Biathlon race;

    public StopCondition(Biathlon race, String name, boolean showInTrace, Object... args) {
        super(race, name, showInTrace, args);
        this.race = race;
    }

    /**
     * 
     * @return true - jeśli wszyscy zawodnicy dobiegli do mety 
     */
    @Override
    public boolean check() {
        return race.haveAllBiathletesFinished();
    }
    
}
