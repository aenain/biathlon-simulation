package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Empirically distributed stream of pseudo random numbers of type
 * <code>double</code>. Values produced by this distribution follow an empirical
 * distribution which is specified by entries consisting of the observed value
 * and the frequency (probability) this value has been observed to occur. These
 * entries are made by using the <code>addEntry()</code> method.
 *  
 * @see desmoj.core.dist.Distribution
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Tim Lechler
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
public class DiscreteDistEmpirical<N extends Number> extends DiscreteDist<N> {

	/**
	 * Vector to store the entries of Value/cumulative frequency pairs.
	 */
	private java.util.ArrayList<Entry> _values;

	/**
	 * Shows if the empirical distribution has been properly initialized.
	 */
	private boolean _isInitialized;

	/**
	 * Sum of the probabilities of all added entries.
	 */
	private double _totalProbabilities;

	/**
	 * Inner class for entries
	 */
	private class Entry {

		/**
		 * The value of the empirical entry.
		 */
		private N entryValue;

		/**
		 * The cumulative frequency of the empirical entry.
		 */
		private double entryFrequency;

		/**
		 * Constructs a simple entry pair with the given value and cumulative
		 * frequency.
		 * 
		 * @param val
		 *            double : The value of the empirical sample
		 * @param freq
		 *            double : The cumulative frequency of the empirical sample
		 */
		private Entry(N val, double freq) {
			entryValue = val;
			entryFrequency = freq;
		}

	}

	/**
	 * Constructs an empirical distribution producing double values. Empirical
	 * distributions have to be initialized manually before use. This is done by
	 * calling the <code>addEntry(double, double)</code> method to add values
	 * defining the behaviour of the desired distribution.s
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public DiscreteDistEmpirical(Model owner, String name,
			boolean showInReport, boolean showInTrace) {

		super(owner, name, showInReport, showInTrace);
		_values = new java.util.ArrayList<Entry>(); // Initialize Vector for
		// values
		_isInitialized = false; // No entries made yet
		_totalProbabilities = 0.0;
	}

	/**
	 * Adds a new entry of an empirical value and its associated frequency.
	 * Unlike in deprecated IntDistEmpirical, the frequency that has to be given
	 * here is the relative frequency of the value itself, not the value of the
	 * distribution function at that value.
	 * 
	 * @param value
	 *            N : The empirical value observed
	 * @param frequency
	 *            double : The corresponding frequency of the empirical value
	 */
	public void addEntry(N value, double frequency) {

		// frequency must be positive
		if (frequency < 0) {
			sendWarning("Can't add empirical entry! Command ignored.",
					"DiscreteDistEmpirical " + getName()
							+ " Method: void addEntry (double value, double "
							+ "frequency)",
					"The frequency parameter given is invalid because it is negative: "
							+ frequency,
					"Be sure to add entries with positive frequency");
			return; // no proper parameter

		}

		// everythings fine so far, so go ahead and add the new val/freq pair
		_values.add(new Entry(value, frequency));
		_isInitialized = true;
		_totalProbabilities += frequency;
	}

	/**
	 * Removes all entries of an empirical value.
	 * 
	 * @param value
	 *            N : The value to be removed
	 */
	public void removeEntry(N value) {

        for (Entry e : _values) {
            if (e.entryValue == value) {
                _totalProbabilities -= e.entryFrequency;
                _values.remove(e);
            }

        }

        if (_values.isEmpty()) {
            _isInitialized = false;
        }

	}
	
    /**
     * Removes all entries of all empirical values close
     * to a given value. 
     * This method is useful for removing floating point values whose
     * internal precision may be subject to rounding errors and
     * depend on the JVM in use.<br/>   
     * Example: In a DiscreteDistEmprirical<Double> after calling 
     * <code>addEntriy(42.42, 5)</code>, you might find the value 
     * actually stored is 42.42000000000001.
     * To remove this entry independently of its precise internal
     * representation, use e.g. <code>removeEntry(42.42, 0.0001)</code>.
     * (This assumes all other values stored in this distribution
     * differ by more than 0.001 from 42.42.)
     * 
     * @param value
     *            N : The value to be removed
     * @param tolerance
     *            double : The value to be removed
     */
    public void removeEntry(N value, double tolerance) {

        for (Entry e : _values) {
            if (e.entryValue.doubleValue() - value.doubleValue() < tolerance) {
                _totalProbabilities -= e.entryFrequency;
                _values.remove(e);
            }

        }

        if (_values.isEmpty()) {
            _isInitialized = false;
        }

    }

	/**
	 * Creates the default reporter for the EmpiricalDiscreteDist distribution.
	 * 
	 * @return Reporter : The reporter for the EmpiricalDiscreteDist
	 *         distribution
	 * @see desmoj.core.report.DiscreteDistEmpiricalReporter
	 */
	public desmoj.core.report.Reporter createReporter() {

		return new desmoj.core.report.DiscreteDistEmpiricalReporter(this);

	}

	/**
	 * Shows if the EmpiricalDiscreteDist distribution already is initialized.
	 * Being initialized means that at least one value has already been added
	 * via the <code>addEntry(double, double)</code> method.
	 * 
	 * @return boolean
	 */
	public boolean isInitialized() {

		return _isInitialized;

	}

	/**
	 * Returns the next sample specified by the empirical distribution. In
	 * contrast to RealDistEmpirical here is no interpolation needed.
	 * 
	 * @return N : The next sample for this empirical distribution or
	 *         returns zero (0) with a warning if the distribution has not been
	 *         properly initialized yet
	 */
	public N sample() {

		if (!_isInitialized) {
			sendWarning(
					"Invalid sample returned!",
					"DiscreteDistEmpirical: " + getName()
							+ " Method: double sample()",
					"The distribution has not been initialized properly yet, "
							+ "thus no valid samples can be taken from it!",
					"Be sure to have the distribution initialized properly "
							+ "before using it. You can make sure by calling method "
							+ "isInitialized() which returns a boolean telling "
							+ "you wether the distribution is initilaized or not.");
			return null; // return null if its not initialized
		}

		incrementObservations(); // increase count of samples

		double q = randomGenerator.nextDouble(); // the random number to
		// derive the value from
		if (antithetic) {
			q = 1 - q; // check for antithetic
		}

		double currentCumProbability = 0;
		int i = 0;

		while (true) {
		    currentCumProbability += (_values.get(i).entryFrequency)
					/ _totalProbabilities;
		    if (currentCumProbability >= q) break; 
            i++;
		}

		N newSample = _values.get(i).entryValue;

		double parseDouble = Double.parseDouble(newSample.toString());
		if (nonNegative && parseDouble < 0) {
			sendWarning(
					"You get a sample from a DiscreteDistEmpirical distribution which "
							+ "is set to nonNegative. But the sample is negative!",
					"DiscreteDistEmpirical: " + this.getName()
							+ " Method: public double sample() ",
					"The given distribution has negative values but all negative values "
							+ "should be ignored.",
					"Make sure not to set a DiscreteDistEmpirical distribution with "
							+ "negative values to nonNegative.");
		}

		if (this.currentlySendTraceNotes())
			this.traceLastSample(newSample.toString());

		return newSample;

	}
}