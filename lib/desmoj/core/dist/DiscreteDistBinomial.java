package desmoj.core.dist;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import desmoj.core.simulator.Model;

/**
 * Distribution returning binomial distributed long values. The binomial
 * distribution describes the probability of having a certain amount of
 * successes in a series of indepedent Bernoulli experiments, all having the
 * same success probability.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Peter Wueppen
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

public class DiscreteDistBinomial extends DiscreteDist<Long> {

	/**
	 * The probability of success in each separate Bernoulli experiment.
	 */
	protected double probability;

	/**
	 * The amount of separate Bernoulli experiments that lead to the result.
	 */
	protected int amount;

	private static class Entry {

		/**
		 * The entry value (amount of successes this entry is about)
		 */
		private int entryValue;

		/**
		 * The cumulative probability of the entry Value, P(X <= entryValue).
		 */
		private double entryCumProbability;

		/**
		 * Constructs a simple entry pair with the given value and cumulative
		 * probability.
		 * 
		 * @param val
		 *            int : The entry value
		 * @param freq
		 *            double : The cumulative frequency of this entry value
		 */
		private Entry(int val, double freq) {
			entryValue = val;
			entryCumProbability = freq;
		}

	}

	/**
	 * List to store the computed distribution values for each outcome.
	 */
	private List<Entry> valueList;

	/**
	 * Creates a stream of pseudo random numbers following a binomial
	 * distribution. The specific parameters p (probability) and n (amount) have
	 * to be given here at creation time.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param probability
	 *            double : The probability of success in each separate Bernoulli
	 *            experiment.
	 * @param amount
	 *            int : The amount of separate Bernoulli experiments that lead to
	 *            the result.
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public DiscreteDistBinomial(Model owner, String name, double probability,
			int amount, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		this.probability = probability;
		this.amount = amount;
		valueList = new ArrayList<Entry>();
		Entry e;
		BinomialDistribution bdist = new BinomialDistributionImpl(this.amount,
				this.probability);
		for (int i = 0; i < this.amount; i++) {
			try {
				e = new Entry(i, bdist.cumulativeProbability(i));
				valueList.add(e);

			} catch (MathException e1) {
				sendWarning(
						"Failed to compute cumulative Probability of value "
								+ Long.toString(i) + ", entry ignored",
						"DiscreteDistBinomial : " + getName()
								+ " at construction time",
						"Impossible to compute cumulative Probability",
						"Make sure the probabilty is set between 0 and 1 and the amount of trials is positive");
			}
		}
	}

	/**
	 * Creates the default reporter for the DiscreteDistBinomial distribution.
	 * 
	 * @return Reporter : The reporter for the DiscreteDistBinomial distribution
	 */
	public desmoj.core.report.Reporter createReporter() {

		return new desmoj.core.report.DiscreteDistBinomialReporter(this);

	}

	/**
	 * Returns the probability of success in each separate Bernoulli
     *         experiment
	 * 
	 * @return double : The probability of success in each separate Bernoulli
	 *         experiment.
	 */
	public double getProbability() {

		return probability;
	}

	/**
	 * Returns the amount of separate Bernoulli experiments that lead to
     *         the result.
	 * 
	 * @return int : The amount of separate Bernoulli experiments that lead to
	 *         the result.
	 */
	public int getAmount() {

		return amount;

	}

	/**
	 * Returns the next sample from this distribution. The value depends upon
	 * the seed, the number of values taken from the stream by using this method
	 * before and the probability and amount of trials specified for this
	 * distribution.
	 * 
	 * @return Long : The next binomial distributed sample from this
	 *         distribution.
	 */
	public Long sample() {

		long newSample; // aux variable
		double randomNumber = randomGenerator.nextDouble();
		incrementObservations(); // increase count of samples

		if (isAntithetic()) {
			randomNumber = 1 - randomNumber;
		}
		int i = 0;
		while ((i < valueList.size())
				&& (valueList.get(i).entryCumProbability < randomNumber)) {
			i++;
		}
		;

		newSample = valueList.get(i).entryValue;

		if (this.currentlySendTraceNotes())
			this.traceLastSample(Double.toString(newSample));

		return newSample;
	}

}
