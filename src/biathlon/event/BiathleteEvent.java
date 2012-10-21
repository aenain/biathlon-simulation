package biathlon.event;

import biathlon.Biathlete;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;

/**
 * Klasa reprezentująca "zamrożone" zdarzenie związane z biatlonistą.
 * Gdy na trasie wydarzy się coś ciekawego (dotarcie do punktu pomiaru czasu,
 * oddanie strzału na strzelnicy), warto to zapamiętać, by np. potem uwzględnić w trace.
 * Jest to prosta klasa, która przechowuje "zamrożony" czas eventu opisanego przez <tt>message</tt>
 * w układzie związanym z symulacją (czas symulacji), jak i w układzie związanym z zawodnikiem
 * (czas, który upłynął od startu tego zawodnika do wyścigu).
 * Zawiera również "zamrożoną" pozycję zawodnika w stawce i jego ewentualną stratę do lidera.
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

    /**
     * "Zamraża" aktualny czas w układzie symulacji i biatlonisty.
     * workaround braku możliwości skopiowania obiektu TimeInstant.
     */
    protected final void storeCurrentTimes(Biathlete biathlete) {
        biathleteTime = new TimeInstant(biathlete.lifeTime().getTimeAsDouble(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        simulationTime = new TimeInstant(biathlete.presentTime().getTimeAsDouble(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    /**
     * "Zamraża" aktualną pozycję zawodnika.
     * Jeśli zawodnik jest liderem, można od razu ustawić zerową stratę do lidera.
     * @param rank pozycja zawodnika (np. na punkcie pomiaru czasu)
     */
    public void setRank(int rank) {
        this.rank = rank;
        if (this.rank == 1) {
            this.lost = TimeSpan.ZERO;
        }
    }

    /**
     * Zwraca pozycję zawodnika w czasie tego zdarzenia.
     * @return pozycja zawodnika
     */
    public Integer getRank() {
        return rank;
    }

    /**
     * "Zamraża" aktualną stratę zawodnika do lidera.
     * @param lost strata do lidera
     */
    public void setLost(TimeSpan lost) {
        this.lost = lost;
    }

    /**
     * Zwraca stratę lidera w czasie tego zdarzenia.
     * @return strata do lidera
     */
    public TimeSpan getLost() {
        return lost;
    }

    /**
     * Czas zdarzenia w układzie biatlonisty
     * @return czas zdarzenia
     */
    public TimeInstant getBiathleteTime() {
        return biathleteTime;
    }

    /**
     * Czas zdarzenia w układzie symulacji
     * @return czas zdarzenia
     */
    public TimeInstant getSimulationTime() {
        return simulationTime;
    }

    /**
     * Opis zdarzenia / Wiadomość
     * @return opis
     */
    public String getMessage() {
        return message;
    }

    public Biathlete getBiathlete() {
        return biathlete;
    }

    /**
     * Event jest mniejszy, gdy jego czas w układzie biatlonisty jest mniejszy.
     * @param o inny event klasy BiathleteEvent
     * @return wynik porównania
     * @see Comparable
     */
    @Override
    public int compareTo(Object o) {
        BiathleteEvent other = (BiathleteEvent)o;
        return biathleteTime.compareTo(other.getBiathleteTime());
    }
}
