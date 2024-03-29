package desmoj.core.report;

import desmoj.core.simulator.Reportable;
import desmoj.core.statistic.Aggregate;

/**
 * Captures all relevant information about the Aggregate.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author modified by Ruth Meyer, Johannes G&ouml;bel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

public class AggregateReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new AggregateReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Aggregate are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Aggregate to report about
	 */

	public AggregateReporter(Reportable informationSource) {
		super(informationSource); // make a Reporter

        numColumns = 7;

        columns = new String[numColumns];

        columns[0] = "Title";
        columns[1] = "Type";
        columns[2] = "(Re)set";
        columns[3] = "Obs";
        columns[4] = "Current Value";
        columns[5] = "Min";
        columns[6] = "Max";
        groupHeading = "Counts and Aggregates";

		groupID = 1311; // see Reporter for more information about groupID

		entries = new String[numColumns];
	}

	/**
	 * Returns an array of Strings each containing the data for the
	 * corresponding column in array <code>columns[]</code>. Implement this
	 * method in a way, that an array of the same length as the columntitles is
	 * produced containing the data at the point of time this method is called
	 * by someone else to produce up-to-date information.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {
		if (source instanceof Aggregate) {
			// the Aggregate we report about
			Aggregate agg = (Aggregate) source;
			// source = informationSource
			// Title
			entries[0] = agg.getName();
            // Type
            entries[1] = "Aggregate";
			// (Re)set
			entries[2] = agg.resetAt().toString();
			// Observations
			entries[3] = Long.toString(agg.getObservations());
			// current value
			entries[4] = Double.toString(agg.getValue());
			// Min
			entries[5] = Double.toString(agg.getMinimum());
			// Max
			entries[6] = Double.toString(agg.getMaximum());
		}

		else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class CountReporter
