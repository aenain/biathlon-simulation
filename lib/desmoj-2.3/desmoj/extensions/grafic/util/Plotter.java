package desmoj.extensions.grafic.util;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import desmoj.core.statistic.Histogram;
import desmoj.core.statistic.TimeSeries;



/**
 * Class to plot DesmoJ histogram and time-series data in jFreeChart Plotter.
 * The DesmoJ datasets are converted in jFreeChart Format.
 * Onscreen and offscreen plots are supported.
 * 
 * See also PaintPanel and TimeSeriesDataSetAdapter
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author christian.mueller@th-wildau.de and goebel@informatik.uni-hamburg.de
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public class Plotter {

    private PaintPanel paintPanel;
    
    /**
	 * Constructor to set the path of output directory and the size of created image. 
	 * @param path
	 * @param size
     */
    public Plotter(String path, Dimension size){
    	this.paintPanel = new PaintPanel(path, size);
    }
    
    /**
     * make an on- or off-screen histogram plot with a desmoJ histogram dataset.
     * @param histogram
     * @param onscreen
     */
    public void makeHistogramPlot(Histogram histogram, boolean onscreen){
    	if(onscreen){
    		paintPanel.show(Plotter.getHistogramPlot(histogram), histogram.getName());
    	}else{
    		paintPanel.save(Plotter.getHistogramPlot(histogram), histogram.getName());
    	}
    }

    /**
     * make an on- or off-screen time-series plot with a desmoJ time-series dataset.
     * @param ts
     * @param onscreen
     */
    public void makeTimeSeriesPlot(TimeSeries ts, boolean onscreen){
    	if(onscreen){
    		paintPanel.show(Plotter.getTimeSeriesPlot(ts), ts.getName());
    	}else{
    		paintPanel.save(Plotter.getTimeSeriesPlot(ts), ts.getName());
    	}
    }

    /**
     * Build a JPanel with a histogram plot of a desmoJ histogram dataset
     * @param histogram		desmoJ histogram dataset
     * @return
     */
    private static JPanel getHistogramPlot(Histogram histogram){
		JFreeChart chart;
		chart = ChartFactory.createBarChart(histogram.getName(), "X", "Y", 
				convertHistogram(histogram), PlotOrientation.VERTICAL, false, true, false);
		chart.setBackgroundPaint(Color.white);
        CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
        categoryplot.setBackgroundPaint(Color.lightGray);
        categoryplot.setRangeGridlinePaint(Color.white);
        categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        BarRenderer barrenderer = (BarRenderer)categoryplot.getRenderer();
        barrenderer.setBaseItemLabelsVisible(true);
        barrenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
        categoryaxis.setCategoryMargin(0.0D);
        categoryaxis.setUpperMargin(0.02D);
        categoryaxis.setLowerMargin(0.02D);
        NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setUpperMargin(0.1D);   

		return new ChartPanel(chart);
	}
	
    /**
     * Convert DesmoJ histogram dataset in to a jFreeChart dataset
     * @param histogram
     * @return
     */
    private static CategoryDataset convertHistogram(Histogram histogram)
    {
        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
        String s = "Prison Population Rates";
        for (int i = 1; i < histogram.getCells(); i++) {
            defaultcategorydataset.addValue(histogram.getObservationsInCell(i), s, "[" + histogram.getLowerLimit(i) + ", " + (histogram.getLowerLimit(i)+histogram.getCellWidth()) + "[");
            //System.out.println("added: " + histogram.getLowerLimit(i) + " " + histogram.getObservationsInCell(i));
        }
        return defaultcategorydataset;
    }
    
    /**
     * Build a JPanel with a time-series plot of a desmoJ time-series dataset.
     * @param ts		desmoJ time-series dataset
     * @return
     */
	private static JPanel getTimeSeriesPlot(TimeSeries ts){
		JFreeChart chart;
		chart = ChartFactory.createScatterPlot(ts.getName(), "X", "Y", new TimeSeriesDataSetAdapter(ts), PlotOrientation.VERTICAL, false, false, false);
        XYPlot xyplot = (XYPlot)chart.getPlot();
        xyplot.setNoDataMessage("NO DATA");
        xyplot.setDomainZeroBaselineVisible(true);
        xyplot.setRangeZeroBaselineVisible(true);
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)xyplot.getRenderer();
        xylineandshaperenderer.setSeriesOutlinePaint(0, Color.black);
        xylineandshaperenderer.setUseOutlinePaint(true);
        
        ChartPanel panel = new ChartPanel(chart);
        panel.setVerticalAxisTrace(false);
        panel.setHorizontalAxisTrace(false);
        panel.setPopupMenu(null);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);

		return panel;
	}
}
