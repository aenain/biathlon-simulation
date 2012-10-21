package biathlon;

import biathlon.report.TraceOutput;
import biathlon.checkpoint.Checkpoint;
import biathlon.event.BiathleteGenerator;
import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Queue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.LinkedList;
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
    protected LinkedList<TraceOutput> traces;
    protected BoolDistBernoulli shotDistStream;
    protected ContDistNormal checkpointArrivalTimeInMilliSeconds;
    protected ContDistUniform shotTimeInMilliSeconds;
    
    public Biathlon(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
        traces = new LinkedList();
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

        StopCondition stopCondition = new StopCondition(model, "Stop Condition", true, true);
        experiment.stop(stopCondition);

        experiment.start();
        experiment.report();
        experiment.finish();
        model.flushAndCloseTraces();
    }

    @Override
    public String description() {
        return "Not supported yet.";
    }

    @Override
    public void doInitialSchedules() {
        generateBiathletes();
    }

    @Override
    public void init() {
        this.shootingArea = new ShootingArea(this, "ShootingArea", true);
        this.checkpoints = new Queue(this, "Checkpoints", true, true);
        this.biathletes = new Queue(this, "Biathletes", true, true);
        this.shotDistStream = new BoolDistBernoulli(this, "shotDistStream", 0.7, true, true); // probability for hit
        this.checkpointArrivalTimeInMilliSeconds = new ContDistNormal(this, "checkpointArrivalTimeInMilliSeconds", 170000, 4000, true, true);
        this.shotTimeInMilliSeconds = new ContDistUniform(this, "shotTimeInMilliSeconds", 2000, 7000, true, true);
        
        biathlon.checkpoint.BeforeShootingArea beforeShootingArea = new biathlon.checkpoint.BeforeShootingArea(this, "Checkpoint before Shooting Area", true);
        beforeShootingArea.setShootingArea(shootingArea);

        biathlon.checkpoint.AfterShootingArea afterShootingArea = new biathlon.checkpoint.AfterShootingArea(this, "Checkpoint after Shooting Area", true);
        afterShootingArea.setShootingArea(shootingArea);

        checkpoints.insert(new biathlon.checkpoint.Checkpoint(this, "Checkpoint 1", true));
        addCheckpoint(beforeShootingArea);
        addCheckpoint(afterShootingArea);
        addCheckpoint(new biathlon.checkpoint.Checkpoint(this, "Checkpoint 4", true));
        addCheckpoint(new biathlon.checkpoint.StartFinish(this, "Start Finish", true));
        checkpoints.last().setNextCheckpoint(checkpoints.first());
    }

    public void addTrace(TraceOutput trace) {
        traces.add(trace);
    }

    public void flushAndCloseTraces() {
        for (TraceOutput trace : traces) {
            trace.flushAndClose();
        }
    }
    
    public boolean getShotResult() {
        return shotDistStream.sample();
    }

    public TimeInstant getCheckpointArrivalTime() {
        return advanceTime(checkpointArrivalTimeInMilliSeconds.sample(), TimeUnit.MILLISECONDS);
    }

    public TimeInstant getShotTime() {
        return advanceTime(shotTimeInMilliSeconds.sample(), TimeUnit.MILLISECONDS);
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
            biathleteGenerator.schedule(advanceTime(i * STAGGERING_IN_SECONDS, TimeUnit.SECONDS));
        }
    }

    public TimeInstant advanceTime(TimeSpan delay) {
        return TimeOperations.add(presentTime(), delay);
    }

    public TimeInstant advanceTime(long delay, TimeUnit unit) {
        return advanceTime(new TimeSpan(delay, unit));
    }

    public TimeInstant advanceTime(double delay, TimeUnit unit) {
        return advanceTime(new TimeSpan(delay, unit));
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
