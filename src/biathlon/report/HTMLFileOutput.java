/**
 * Klasy rozszerzające funkcjonalności z desmoj.core.report.
 */
package biathlon.report;

import biathlon.core.Entity;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Generowanie trace'u dla danej encji w formacie HTML.
 * Przy generowaniu html-a opiera się o metodę toString() przekazanych obiektów.
 * - null zostaje zamieniony na pusty string.
 * - TimeInstant zostaje zamieniony na string postaci "HH:MM:SS.d", gdzie d oznacza dziesiąte części sekundy
 * - TimeSpan zostaje zamieniony na string postaci "+MM:SS.d", gdzie + jest plusem, gdy różnica jest dodatnia,
 *   w przeciwnym razie pojawia się minus. d - dziesiąta część sekundy.
 * 
 * @author Artur Hebda
 * @example
 *      html = new HTMLFileOutput(biathlete, "Trace for: FINELLO Jeremy");
 *      html.startSection("Lap 1");
 *      html.startTable("Position", "Description", "Time", "Lost");
 *      html.insertRow(1, "shots and misses", currentTime, TimeSpan.ZERO);
 *      ...
 *      html.closeTable();
 *      html.closeSection();
 *      html.close();
 * 
 * @see desmoj.core.report.FileOutput
 */
public class HTMLFileOutput extends desmoj.core.report.FileOutput {
    /**
     * Encja, dla której generowany jest trace.
     */
    protected Entity entity;

    /**
     * Tworzenie trace'a dla danej encji.
     * Utworzy plik w katalogu traces/#{nazwa_klasy}/ o nazwie uzyskanej dzięki metodzie
     * toString() na obiekcie encji.
     * @param entity encja, dla której generujemy trace
     * @param title tytuł trace'a
     */
    public HTMLFileOutput(Entity entity, String title) {
        this.entity = entity;
        open("traces/" + entity.getClass().getName() + "/" + entity.toString() + ".html");
        writeln(HTMLFileOutput.header(title));
    }

    /**
     * Tworzenie trace'a dla danej encji.
     * Można sprecyzować nazwę pliku trace.
     * @param entity encja, dla której generujemy trace
     * @param title tytuł trace'a
     * @param filename nazwa pliku trace
     */
    public HTMLFileOutput(Entity entity, String title, String filename) {
        this.entity = entity;
        open("traces/" + entity.getClass().getName() + "/" + filename + ".html");
        writeln(HTMLFileOutput.header(title));
    }

    /**
     * Dodaje do trace'a nagłówek tabeli
     * @param columns kolumny tabeli
     */
    public void startTable(Object... columns) {
        writeln(HTMLFileOutput.tableHeader(columns));
    }

    /**
     * Dodaje do trace'a nagłówek tabeli
     * @param columns kolumny tabeli
     */
    public void startTable(List<String> columns) {
        writeln(HTMLFileOutput.tableHeader(columns.toArray()));
    }

    /**
     * Dodaje do trace'a wers tabeli
     * @param columns zawartość wiersza
     */
    public void insertRow(List<Object> columns) {
        writeln(HTMLFileOutput.tableRow(columns.toArray()));
    }

    /**
     * Dodaje do trace'a wers tabeli
     * @param columns zawartość wiersza
     */
    public void insertRow(Object... columns) {
        writeln(HTMLFileOutput.tableRow(columns));
    }

    /**
     * Dodaje do trace'a sekcję zamykającą tabelę.
     */
    public void closeTable() {
        writeln(HTMLFileOutput.tableFooter());
    }

    /**
     * Dodaje do trace'a początek nowej sekcji wraz z nagłówkiem
     * @param title treść nagłówka (opis sekcji)
     */
    public void startSection(String title) {
        writeln(HTMLFileOutput.sectionHeader(title));
    }

    /**
     * Dodaje do trace'a zamknięcie sekcji.
     */
    public void closeSection() {
        writeln(HTMLFileOutput.sectionFooter());
    }

    /**
     * Dodaje do trace'a niezbędną sekcję kończącą dokument HTML i zamyka trace.
     */
    @Override
    public void close() {
        writeln(HTMLFileOutput.footer());
        super.close();
    }

    /**
     * Nagłówek dokumentu HTML
     * @param title tytuł dokumentu (pojawi się również w pierwszym nagłówku)
     * @return fragment HTML
     */
    public static String header(String title) {
        return  "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                        "<head>" +
                            "<meta charset='utf-8' />" +
                            "<title>" + title + "</title>" +
                            "<style type='text/css'>" +
                                "table { width: 100%; }" +
                                "table th { text-align: left; }" +
                            "</style>" +
                        "</head>" +
                        "<body>" +
                            "<h1>" + title + "</h1>";
    }

    /**
     * Sekwencja kończąca dokument HTML
     * @return fragment HTML
     */
    public static String footer() {
        return  "</body></html>";
    }

    /**
     * Nagłówek sekcji
     * @param title tytuł sekcji (opis)
     * @return fragment HTML
     */
    public static String sectionHeader(String title) {
        return  "<section>" +
                    "<h2>" + title + "</h2>";
    }

    /**
     * Sekwencja kończąca sekcję
     * @return fragment HTML
     */
    public static String sectionFooter() {
        return  "</section>";
    }

    /**
     * Nagłówek tabeli z uwzględnieniem kolumn
     * @param columns kolumny
     * @return fragment HTML
     */
    public static String tableHeader(Object... columns) {
        StringBuilder html = new StringBuilder("<table><thead><tr>");
        for (Object column : columns) {
            html.append("<th>").append(column.toString()).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        return html.toString();
    }

    /**
     * Wers tabeli
     * @param columns zawartość wiersza
     * @return fragment HTML
     */
    public static String tableRow(Object... columns) {
        StringBuilder html = new StringBuilder("<tr>");
        for (Object column : columns) {
            if (column == null) {
                column = "";
            }
            else if (column instanceof TimeInstant) {
                column = HTMLFileOutput.formatTime((TimeInstant)column);
            }
            else if (column instanceof TimeSpan) {
                column = HTMLFileOutput.formatTime((TimeSpan)column);
            }

            html.append("<td>").append(column.toString()).append("</td>");
        }
        html.append("</tr>");
        return html.toString();
    }

    /**
     * Sekwencja kończąca tabelę
     * @return fragmentHTML
     */
    public static String tableFooter() {
        return  "</tbody></table>";
    }

    /**
     * Formatowanie czasu.
     * Format: "HH:MM:SS.d", gdzie HH - liczba godzin, MM - liczba minut, SS - liczba sekund,
     * d - zaokrąglona liczba dziesiątych części sekundy
     * @param time
     * @return sformatowany czas
     */
    public static String formatTime(TimeInstant time) {
        long hours = time.getTimeTruncated(TimeUnit.HOURS);
        long minutes = time.getTimeTruncated(TimeUnit.MINUTES) - hours * 60;
        long seconds = time.getTimeTruncated(TimeUnit.SECONDS) - hours * 3600 - minutes * 60;
        long milliseconds = time.getTimeTruncated(TimeUnit.MILLISECONDS) - hours * 3600 * 1000 - minutes * 60 * 1000 - seconds * 1000;
        long tenthOfSecond = Math.round(milliseconds / 100);
        return String.format("%02d:%02d:%02d.%1d", hours, minutes, seconds, tenthOfSecond);
    }

    /**
     * Formatowanie czasu.
     * Format: "+MM:SS.d", gdzie + oznacza, że przedział (różnica) czasu jest większy od 0 (gdy będzie
     * mniejszy, pojawi sie '-'), MM - liczba minut, SS - liczba sekund,
     * d - zaokrąglona liczba dziesiątych części sekundy
     * @param time - powinien byc mniejszy co modulu niż jedna 1h.
     * @return sformatowany czas
     */
    public static String formatTime(TimeSpan time) {
        String format;
        long minutes = time.getTimeTruncated(TimeUnit.MINUTES);
        long seconds = time.getTimeTruncated(TimeUnit.SECONDS) - minutes * 60;
        long milliseconds = time.getTimeTruncated(TimeUnit.MILLISECONDS) - minutes * 60 * 1000 - seconds * 1000;
        long tenthOfSecond = Math.round(milliseconds / 100);

        if (time.compareTo(TimeSpan.ZERO) == 0) {
            format = "";
        }
        else if (time.compareTo(TimeSpan.ZERO) < 0) {
            format = "-%02d:%02d.%1d";
        }
        else {
            format = "+%02d:%02d.%1d";
        }
        
        return String.format(format, minutes, seconds, tenthOfSecond);
    }
}
