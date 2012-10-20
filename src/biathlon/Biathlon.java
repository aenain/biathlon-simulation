package biathlon;

import biathlon.checkpoint.Checkpoint;
import biathlon.event.BiathleteGenerator;
import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 */
public class Biathlon extends Model {
    public static int DURATION_IN_MINUTES = 80;
    public static int BIATHLETE_COUNT = 30;
    public static int LAPS = 4;
    public static int MISS_PENALTY_IN_SECONDS = 60;
    public static int STAGGERING_IN_SECONDS = 30;

    protected Queue<Biathlete> biathletes;
    protected ShootingArea shootingArea;
    protected Queue<Checkpoint> checkpoints;
    
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

        StopCondition stopCondition = new StopCondition(model, "Stop Condition", false, false);
        experiment.stop(stopCondition);

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
        this.shootingArea = new ShootingArea(this, "ShootingArea", true);
        this.checkpoints = new Queue(this, "Checkpoints", false, false);

        biathlon.checkpoint.BeforeShootingArea beforeShootingArea = new biathlon.checkpoint.BeforeShootingArea(this, "Checkpoint before Shooting Area", true);
        beforeShootingArea.setShootingArea(shootingArea);

        biathlon.checkpoint.AfterShootingArea afterShootingArea = new biathlon.checkpoint.AfterShootingArea(this, "Checkpoint after Shooting Area", true);
        afterShootingArea.setShootingArea(shootingArea);

        addCheckpoint(new biathlon.checkpoint.Checkpoint(this, "Checkpoint 1", true));
        addCheckpoint(beforeShootingArea);
        addCheckpoint(afterShootingArea);
        addCheckpoint(new biathlon.checkpoint.Checkpoint(this, "Checkpoint 4", true));
        addCheckpoint(new biathlon.checkpoint.StartFinish(this, "Start/Finish", true));
        checkpoints.last().setNextCheckpoint(checkpoints.first());
        
        generateBiathletes();
    }

    @Override
    public void init() {
    }

    public boolean haveAllBiathletesFinished() {
        return biathletes.isEmpty();
    }

    protected void generateBiathletes() {
        BiathleteGenerator biathleteGenerator;
        for (int i = 0; i < BIATHLETE_COUNT; i++) {
            biathleteGenerator = new BiathleteGenerator(this, "BiathleteGenerator", true);
            // tworzenie biatlonistow jest rownoznaczne z ich startem do wyscigu
            // nalezy wiec uwzglednic opoznienia na starcie
            biathleteGenerator.schedule(new TimeInstant(i * STAGGERING_IN_SECONDS, TimeUnit.SECONDS));
        }
    }
 
    public Queue<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public Queue<Biathlete> getBiathletes() {
        return biathletes;
    }

    protected void addCheckpoint(Checkpoint checkpoint) {
        Checkpoint lastCheckpoint = checkpoints.last();
        lastCheckpoint.setNextCheckpoint(checkpoint);
        checkpoints.insert(checkpoint);
    }

    public ShootingArea getShootingArea() {
        return shootingArea;
    }
}
