/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package biathlon.report;

import biathlon.core.Entity;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Artur Hebda
 * @usage
 * html = new HTMLFileOutput(biathlete, "Trace for a biathlete");
 * html.startSection("Lap 1");
 * html.startTable(new String[] {"Position", "Start Number", "Name", "Description", "Time", "Lost"});
 * html.insertRow(new Object[] {...});
 * ...
 * html.closeTable();
 * html.closeSection();
 * html.close();
 */
public class HTMLFileOutput extends desmoj.core.report.FileOutput {
    protected Entity entity;

    public HTMLFileOutput(Entity entity, String title) {
        this.entity = entity;
        open("traces/" + entity.getClass().getName() + "/" + entity.toString() + ".html");
        writeln(HTMLFileOutput.header(title));
    }

    public HTMLFileOutput(Entity entity, String title, String filename) {
        this.entity = entity;
        open("traces/" + entity.getClass().getName() + "/" + filename + ".html");
        writeln(HTMLFileOutput.header(title));
    }

    public void startTable(Object... columns) {
        writeln(HTMLFileOutput.tableHeader(columns));
    }

    public void startTable(List<String> columns) {
        writeln(HTMLFileOutput.tableHeader(columns.toArray()));
    }

    public void insertRow(List<Object> columns) {
        writeln(HTMLFileOutput.tableRow(columns.toArray()));
    }

    public void insertRow(Object... columns) {
        writeln(HTMLFileOutput.tableRow(columns));
    }

    public void closeTable() {
        writeln(HTMLFileOutput.tableFooter());
    }

    public void startSection(String title) {
        writeln(HTMLFileOutput.sectionHeader(title));
    }

    public void closeSection() {
        writeln(HTMLFileOutput.sectionFooter());
    }

    @Override
    public void close() {
        writeln(HTMLFileOutput.footer());
        super.close();
    }

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

    public static String footer() {
        return  "</body></html>";
    }

    public static String sectionHeader(String title) {
        return  "<section>" +
                    "<h2>" + title + "</h2>";
    }

    public static String sectionFooter() {
        return  "</section>";
    }

    public static String tableHeader(Object... columns) {
        StringBuilder html = new StringBuilder("<table><thead><tr>");
        for (Object column : columns) {
            html.append("<th>").append(column.toString()).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        return html.toString();
    }

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

    public static String tableFooter() {
        return  "</tbody></table>";
    }

    public static String formatTime(TimeInstant time) {
        long hours = time.getTimeTruncated(TimeUnit.HOURS);
        long minutes = time.getTimeTruncated(TimeUnit.MINUTES) - hours * 60;
        long seconds = time.getTimeTruncated(TimeUnit.SECONDS) - hours * 3600 - minutes * 60;
        long milliseconds = time.getTimeTruncated(TimeUnit.MILLISECONDS) - hours * 3600 * 1000 - minutes * 60 * 1000 - seconds * 1000;
        long tenthOfSecond = Math.round(milliseconds / 100);
        return String.format("%02d:%02d:%02d.%1d", hours, minutes, seconds, tenthOfSecond);
    }

    /*
     * @param time - powinien byc mniejszy co modulu niż jedna 1h.
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
