package desmoj.extensions.grafic.util;

import org.jfree.data.*;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

import desmoj.core.statistic.TimeSeries;

/**
 * Class to convert DesmoJ timeseries data into jFreeChart format
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
public class TimeSeriesDataSetAdapter extends AbstractXYDataset
    implements XYDataset, DomainInfo, RangeInfo
{

    public TimeSeriesDataSetAdapter(TimeSeries timeSeries)
    {
        seriesCount = 1;
        itemCount = (int)timeSeries.getObservations();
        xValues = new Double[seriesCount][itemCount];
        yValues = new Double[seriesCount][itemCount];
        if(itemCount > 0){
	        double min = Double.MAX_VALUE;
	        double max = Double.MIN_VALUE;
	        
	        for (int j = 0; j < itemCount; j++) {
	            xValues[0][j] = (Double) timeSeries.getTimeValues().get(j);
	            yValues[0][j] = (Double) timeSeries.getDataValues().get(j);
	            if (yValues[0][j] < min) min = yValues[0][j];
	            if (yValues[0][j] > max) max = yValues[0][j];
	        }
	        //if(timeSeries.getTimeValues() == null) System.out.println("is null  itemCount: "+itemCount);
	        domainMin = new Double(0);
	        domainMax = (Double) timeSeries.getTimeValues().get(itemCount-1);
	        domainRange = new Range(domainMin.doubleValue(), domainMax.doubleValue());
	        rangeMin = new Double(min);
	        rangeMax = new Double(max);
	        range = new Range(min, max);
        }else{
        	//System.out.println("TimeSeriesDataSetAdapter: itemCount: "+itemCount);
        }
    }

    public Number getX(int i, int j)
    {
        return xValues[i][j];
    }

    public Number getY(int i, int j)
    {
        return yValues[i][j];
    }

    public int getSeriesCount()
    {
        return seriesCount;
    }

    public Comparable getSeriesKey(int i)
    {
        return "Sample " + i;
    }

    public int getItemCount(int i)
    {
        return itemCount;
    }

    public double getDomainLowerBound()
    {
        return domainMin.doubleValue();
    }

    public double getDomainLowerBound(boolean flag)
    {
        return domainMin.doubleValue();
    }

    public double getDomainUpperBound()
    {
        return domainMax.doubleValue();
    }

    public double getDomainUpperBound(boolean flag)
    {
        return domainMax.doubleValue();
    }

    public Range getDomainBounds()
    {
        return domainRange;
    }

    public Range getDomainBounds(boolean flag)
    {
        return domainRange;
    }

    public Range getDomainRange()
    {
        return domainRange;
    }

    public double getRangeLowerBound()
    {
        return rangeMin.doubleValue();
    }

    public double getRangeLowerBound(boolean flag)
    {
        return rangeMin.doubleValue();
    }

    public double getRangeUpperBound()
    {
        return rangeMax.doubleValue();
    }

    public double getRangeUpperBound(boolean flag)
    {
        return rangeMax.doubleValue();
    }

    public Range getRangeBounds(boolean flag)
    {
        return range;
    }

    public Range getValueRange()
    {
        return range;
    }

    public Number getMinimumDomainValue()
    {
        return domainMin;
    }

    public Number getMaximumDomainValue()
    {
        return domainMax;
    }

    public Number getMinimumRangeValue()
    {
        return domainMin;
    }

    public Number getMaximumRangeValue()
    {
        return domainMax;
    }

    private Double xValues[][];
    private Double yValues[][];
    private int seriesCount;
    private int itemCount;
    private Number domainMin;
    private Number domainMax;
    private Number rangeMin;
    private Number rangeMax;
    private Range domainRange;
    private Range range;
}
