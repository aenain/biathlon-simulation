package desmoj.core.report;

import desmoj.core.simulator.TimeSpan;

/**
 * Captures all relevant information about the Tally.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
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

public class TallyReporter extends desmoj.core.report.Reporter {

	/**
	 * Flag, used to know if values should be printed as TimeSpans.
	 */
	private boolean _showTimeSpansInReport = false;

	// ****** methods ******

	/**
	 * Constructor for a new TallyReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Tally are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Tally to report about.
	 */
	public TallyReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 7;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";
		columns[3] = "Mean";
		columns[4] = "Std.Dv";
		columns[5] = "Min";
		columns[6] = "Max";
		groupHeading = "Tallies";
		groupID = 1611; // see Reporter for more information about groupID
		entries = new String[numColumns];
	}

	/**
	 * Are values printed as TimeSpans in the report?
	 * 
	 * @return true if values are printed as TimeSpans, false if not.
	 */
	public boolean getShowTimeSpanInReport() {
		return _showTimeSpansInReport;
	}

	/**
	 * Sets if values should be printed as TimeSpans in the Report
	 * 
	 * @param value
	 *            boolean : true, if values should be printed as TimeSpans,
	 *            false if not.
	 */
	public void setShowTimeSpanInReport(boolean value) {
		_showTimeSpansInReport = value;
	}

	/**
	 * Returns an array of Strings each containing the data for the
	 * corresponding column in array <code>columns[]</code>. Implement this
	 * method in a way, that an array of the same length as the column titles is
	 * produced containing the data at the point of time this method is called
	 * by someone else to produce up-to-date information.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {
		if (source instanceof desmoj.core.statistic.Tally) {
			// the Tally we report about (source = informationSource)
			desmoj.core.statistic.Tally tl = (desmoj.core.statistic.Tally) source;
			desmoj.core.statistic.TallyRunning tlr = null;
			if (tl instanceof desmoj.core.statistic.TallyRunning) {
				tlr = (desmoj.core.statistic.TallyRunning) tl;
			}

			// Title
			entries[0] = tl.getName();
			// (Re)set
			entries[1] = tl.resetAt().toString();
			// Obs
			entries[2] = Long.toString(tl.getObservations());
			// Mean
			// no observations made, so Mean can not be calculated
			if (tl.getObservations() == 0) {
				entries[3] = "insufficient data";
			} else // return mean value
			{
				entries[3] = _showTimeSpansInReport ? new TimeSpan(tl.getMean())
						.toString() : Double.toString(tl.getMean());
				if (tlr != null)
					entries[3] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ (_showTimeSpansInReport ? new TimeSpan(
									tlr.getMeanLastN()).toString() : Double
									.toString(tlr.getMeanLastN())) + ")";
			}

			// Std.Dev
			// not enough observations are made, so Std.Dev can not be
			// calculated
			if (tl.getObservations() < 2) {
				entries[4] = "insufficient data";
			} else // return standard deviation
			{
				entries[4] = _showTimeSpansInReport ? new TimeSpan(
						tl.getStdDev()).toString() : Double.toString(tl
						.getStdDev());
				if (tlr != null)
					entries[4] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ (_showTimeSpansInReport ? new TimeSpan(
									tlr.getStdDevLastN()).toString() : Double
									.toString(tlr.getStdDevLastN())) + ")";
			}

			// Min
			if (tl.getObservations() == 0) {
				entries[5] = "insufficient data";
			} else {
				entries[5] = _showTimeSpansInReport ? new TimeSpan(
						tl.getMinimum()).toString() : Double.toString(tl
						.getMinimum());
				if (tlr != null)
					entries[5] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ (_showTimeSpansInReport ? new TimeSpan(
									tlr.getMinimumLastN()).toString() : Double
									.toString(tlr.getMinimumLastN())) + ")";
			}

			// Max
			if (tl.getObservations() == 0) {
				entries[6] = "insufficient data";
			} else {
				entries[6] = _showTimeSpansInReport ? new TimeSpan(
						tl.getMaximum()).toString() : Double.toString(tl
						.getMaximum());
				if (tlr != null)
					entries[6] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ (_showTimeSpansInReport ? new TimeSpan(
									tlr.getMaximumLastN()).toString() : Double
									.toString(tlr.getMaximumLastN())) + ")";
			}

		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class TallyReporter
