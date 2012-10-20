package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Distribution returning triangular distributed double values.
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

public class ContDistTriangular extends ContDist {

	/**
	 * The lowest possible value.
	 */
	protected double lower;

	/**
	 * The highest possible value.
	 */
	protected double upper;

	/**
	 * The peak of the triangle.
	 */
	protected double peak;

	/**
	 * Creates a stream of pseudo random numbers following a triangular
	 * distribution. The specific lower and upper bounds as well as the most
	 * probable value have to be given here at creation time.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param lower
	 *            double : The lowest possibly generated value.
	 * @param upper
	 *            double : The highest possibly generated value.
	 * @param peak
	 *            double : The most probable value within the distribution, the
	 *            peak of the triangle.
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public ContDistTriangular(Model owner, String name, double lower,
			double upper, double peak, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		this.lower = lower;
		this.upper = upper;
		this.peak = peak;
	}

	/**
	 * Creates the default reporter for the TriangularDist distribution.
	 * 
	 * @return Reporter : The reporter for the TriangularDist distribution
	 * @see desmoj.core.report.ContDistTriangularReporter
	 */
	public desmoj.core.report.Reporter createReporter() {

		return new desmoj.core.report.ContDistTriangularReporter(this);

	}

    /**
     * Returns the minimum value of this triangular distribution.
     * 
     * @return double : The minimum value of this triangular distribution
     */
	public double getLower() {

		return lower;
	}

	/**
	 * Returns the maximum value of this triangular distribution.
	 * 
	 * @return double : The maximum value of this triangular distribution
	 */
	public double getUpper() {

		return upper;

	}

    /**
     * Returns the most likely value of this triangular distribution.
     * 
     * @return double : The most likely value of this triangular distribution
     */
	public double getPeak() {

		return peak;

	}

	/**
	 * Returns the next sample from this distribution. The value depends upon
	 * the seed, the number of values taken from the stream by using this method
	 * before and the lower and upper bounds as well as the peak value specified
	 * for this distribution.
	 * 
	 * @return Double : The next triangular distributed sample from this
	 *         distribution.
	 */
	public Double sample() {

		double newSample; // aux variable
		double randomNumber = randomGenerator.nextDouble();
		double turningPointHeight = (peak - lower) / (upper - lower);
		
		incrementObservations(); // increase count of samples

		if (isAntithetic()) {
			randomNumber = 1 - randomNumber;
		}
		do {
			if (randomNumber <= turningPointHeight) {
				newSample = Math.sqrt(randomNumber * (upper - lower) * (peak - lower)) + lower;
			} else {
				newSample = upper - Math.sqrt((1-randomNumber) * (upper - lower) * (upper - peak));
			}

		} while (nonNegative && newSample < 0);

		if (this.currentlySendTraceNotes())
			this.traceLastSample(Double.toString(newSample));

		return newSample;
	}

}
