package biathlon;

import desmoj.core.simulator.ModelCondition;

/**
 *
 * @author Artur Hebda
 */
public class StopCondition extends ModelCondition {
    private Biathlon race;

    public StopCondition(Biathlon race, String name, boolean showInTrace, Object... args) {
        super(race, name, showInTrace, args);
        this.race = race;
    }

    @Override
    public boolean check() {
        return race.haveAllBiathletesFinished();
    }
    
}
