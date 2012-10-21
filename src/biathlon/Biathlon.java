package biathlon;

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
import java.util.concurrent.TimeUnit;

/**
 * Klasa odpowiedzialna za model symulacji
 * 
 * @author Artur Hebda
 * 
 */
public class Biathlon extends Model {
    /**
     * dla jak długiego przedziału czasu generować trace.
     */
    public static int DURATION_IN_MINUTES = 80;

    public static int BIATHLETE_COUNT = 30;
    public static int LAPS = 4;

    /**
     * długość kary za "pudło" na strzelnicy.
     */
    public static int MISS_PENALTY_IN_SECONDS = 60;

    /**
     * odstęp czasowy między startem kolejnych zawodników.
     */
    public static int STAGGERING_IN_SECONDS = 30;

    protected Queue<Biathlete> biathletes;
    protected ShootingArea shootingArea;
    protected Queue<Checkpoint> checkpoints;

    /**
     * stream generujący rezultaty kolejnych strzałów na strzelnicy.
     */
    protected BoolDistBernoulli shotDistStream;

    /**
     * stream generujący czas trwania biegu danego zawodnika do kolejnego punktu pomiaru
     * czasu.
     * Zakładamy, że punkty pomiaru czasu są rozmieszczone równomiernie (nie dotyczy
     * dystansu między biathlon.checkpoint.BeforeShootingArea i biathlon.checkpoint.AfterShootingArea,
     * gdyż całą strzelnicę traktujemy jak jedno miejsce. 
     */
    protected ContDistNormal checkpointArrivalTimeInMilliSeconds;

    /**
     * stream generujący czas oddania strzału przez zawodnika.
     * brak rozróżnienia na pozycje leżącą i stojacą.
     */
    protected ContDistUniform shotTimeInMilliSeconds;

    /**
     * liczba zawodników, którzy ukończyli zawody.
     * Używana pośrednio w warunku zatrzymania symulacji.
     */
    protected int finishCount = 0;

    /**
     * @see desmoj.core.simulator.Model
     */
    public Biathlon(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    /**
     * Główna metoda, gdzie wszystko bierze swój początek.
     * Tworzy obiekty, przeprowadza symulacje, generuje trace'y, taka alfa i omega :)
     * 
     * @param args ignorowane.
     */
    public static void main(String[] args) {
        Biathlon model = new Biathlon(null, "Biathlon", true, true);
        Experiment experiment = new Experiment("Individual Race");
        model.connectToExperiment(experiment);

        experiment.setShowProgressBar(true);
        TimeInstant stopTime = new TimeInstant(DURATION_IN_MINUTES, TimeUnit.MINUTES);
        experiment.tracePeriod(new TimeInstant(0), stopTime);

        /* warunek zatrzymania symulacji */
        StopCondition stopCondition = new StopCondition(model, "Stop Condition", true, true);
        experiment.stop(stopCondition);

        experiment.start();
        experiment.report();
        experiment.finish();
        model.generateTraces();
    }

    @Override
    public String description() {
        return  "Model biegu indywidualnego mężczyzn na 20km w biathlonie. <br />" +
                "Założenia: <br />" +
                "Zwycięzca potrzebuje ok. 50 min przy 0-1 pudłach na pokonanie całej trasy <br />" +
                "Startuje 30 zawodników <br />" +
                "Każdy zawodnik oddaje po 5 strzałów w 4 seriach <br />" +
                "Jest jedna strzelnica, na której każdy zawodnik ma swoje stanowisko i może " +
                    "oddawać strzały zarówno w pozycji stojącej, jak i leżącej (nierozróżnialne) <br />" +
                "Dotarcie na strzelnicę (punkt pomiaru czasu) jest równoznaczne z rozpoczęciem strzelania <br />" +
                "Przed opuszczeniem strzelnicy (punkt pomiaru czasu) jest doliczana kara czasowa za chybienia <br />" +
                "Kara za chybienie to 60 sekund <br />" +
                "Punkty pomiaru czasu są rozmieszczone w równych odstępach (dla uproszczenia). " +
                    "Nie dotyczy punktu pomiaru czasu przed i za strzelnicą. Hipotetyczny dystans między nimi jest zerowy <br />" +
                "Trasa jest podzielona na 4 okrążenia, na każdym zawodnicy w połowie dystansu odwiedzają strzelnicę <br />" +
                "Czas biegu zawodników między kolejnymi punktami pomiaru czasu można opisać przy użyciu rozkładu normalnego <br />" +
                "Czas przygotowania i oddania strzału przez zawodników można opisać przy użyciu rozkładu jednostajnego <br />" +
                "Parametry rozkładów zostały dobrane empirycznie na podstawie wyników zawodów z Canmore (15 lutego 2012).";
    }

    @Override
    public void doInitialSchedules() {
        generateBiathletes();
    }

     /**
     * Metoda inicjująca symulację.
     * 
     * Tworzone w niej są wszystkie niezbędne obiekty, takie jak strzelnicę, rozkłady prawdopodobieństw,
     * kolejki zawodników i punktów pomiarowych.
     */
    @Override
    public void init() {
        this.shootingArea = new ShootingArea(this, "ShootingArea", true);
        this.checkpoints = new Queue(this, "Checkpoints", true, true);
        this.biathletes = new Queue(this, "Biathletes", true, true);
        this.shotDistStream = new BoolDistBernoulli(this, "shotDistStream", 0.8, true, true); // prawdopodobieństwo trafienia
        this.checkpointArrivalTimeInMilliSeconds = new ContDistNormal(this, "checkpointArrivalTimeInMilliSeconds", 185000, 12000, true, true); // wartość średnia i odchylenie standardowe rozkładu
        this.shotTimeInMilliSeconds = new ContDistUniform(this, "shotTimeInMilliSeconds", 2000, 7000, true, true); // wartości graniczne rozkładu
        
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

    /**
     * Wygenerowanie trace'ów dla punktów pomiaru czasu, strzelnicy i zawodników.
     * Ważne jest, by na końcu generować dla zawodników, gdyż generowanie trace'ów
     * dla punktów pomiaru czasu pozwala ustalić, na której pozycji był zawodnik
     * na danym punkcie pomiaru czasu.
     */
    public void generateTraces() {
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.generateTrace();
        }
        shootingArea.generateTrace();
        for (Biathlete biathlete : biathletes) {
            biathlete.generateTrace();
        }
    }
    
    /**
     * Losowanie rezultatu danego strzału.
     * 
     * @return true (trafiono), false (pudło)
     */
    public boolean getShotResult() {
        return shotDistStream.sample();
    }

    /**
     * Losowanie czasu przybycia do następnego punktu pomiaru czasu.
     * 
     * @return bezwględny czas przybycia do następnego punktu pomiaru czasu obliczany względem czasu symulacji.
     */
    public TimeInstant getCheckpointArrivalTime() {
        return advanceTime(checkpointArrivalTimeInMilliSeconds.sample(), TimeUnit.MILLISECONDS);
    }

    /**
     * Losowanie długości trwania przygotowania i oddawania strzału przez zawodnika.
     * 
     * @return bezwględny czas oddania strzału obliczany względem czasu symulacji.
     */
    public TimeInstant getShotTime() {
        return advanceTime(shotTimeInMilliSeconds.sample(), TimeUnit.MILLISECONDS);
    }

    /**
     * Sprawdzenie, czy wszyscy zawodnicy dotarli do mety
     * 
     * @return true (wszyscy ukończyli) 
     */
    public boolean haveAllBiathletesFinished() {
        return finishCount == BIATHLETE_COUNT;
    }

    /**
     * Poinformowanie modelu o dotarciu do mety kolejnego zawodnika.
     */
    public void incrementFinishCount() {
        finishCount++;
    }

    /**
     * Tworzy i zapisuje w harmonogramie zdarzenie odpowiedzialne za wygenerowanie
     * i start zawodnika.
     * Uwzględnia opóźnienie wynikające ze specyfiki wyścigu - każdy zawodnik startuje z pewnym stałym
     * upóźnieniem względem poprzedniego.
     */
    protected void generateBiathletes() {
        BiathleteGenerator biathleteGenerator;
        for (int i = 0; i < BIATHLETE_COUNT; i++) {
            biathleteGenerator = new BiathleteGenerator(this, "BiathleteGenerator", true);
            // tworzenie biatlonistow jest rownoznaczne z ich startem do wyscigu
            // nalezy wiec uwzglednic opoznienia na starcie
            biathleteGenerator.schedule(advanceTime(i * STAGGERING_IN_SECONDS, TimeUnit.SECONDS));
        }
    }

    /**
     * Zwraca bezwględny czas symulacji, który będzie po upływie podanego opóźnienia od teraz.
     * @param delay opóźnienie
     * @return bezwględny czas symulacji
     */
    public TimeInstant advanceTime(TimeSpan delay) {
        return TimeOperations.add(presentTime(), delay);
    }

    /**
     * Zwraca bezwględny czas symulacji, który będzie po upływie podanego opóźnienia od teraz.
     * @param delay opóźnienie w jednostkach bezwględnych
     * @param unit jednostka referencyjna czasu
     * @return bezwględny czas symulacji
     */
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
    
    public ShootingArea getShootingArea() {
        return shootingArea;
    }

    /**
     * Dodaje punkt pomiarowy do kolejki.
     * 
     * @param checkpoint punkt pomiaru czasu, który chcemy dodać
     */
    protected void addCheckpoint(Checkpoint checkpoint) {
        Checkpoint lastCheckpoint = checkpoints.last();
        lastCheckpoint.setNextCheckpoint(checkpoint);
        checkpoints.insert(checkpoint);
    }
}
