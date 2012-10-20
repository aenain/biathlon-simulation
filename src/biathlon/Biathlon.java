package biathlon;

import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class Biathlon extends Model {
    final private static int DURATION_IN_MINUTES = 80;

    public Biathlon(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Biathlon model = new Biathlon(null, "Biathlon", true, true);
        Experiment experiment = new Experiment("Individual Race");
        model.connectToExperiment(experiment);

        experiment.setShowProgressBar(true);
        TimeInstant stopTime = new TimeInstant(DURATION_IN_MINUTES, TimeUnit.MINUTES);
        experiment.tracePeriod(new TimeInstant(0), stopTime);
        experiment.stop(stopTime);

        experiment.start();
        experiment.report();
        experiment.finish();
    }

    @Override
    public String description() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void doInitialSchedules() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
