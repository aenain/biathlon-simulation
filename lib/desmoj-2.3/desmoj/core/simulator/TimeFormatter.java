package desmoj.core.simulator;

/**
 * An object that formats TimeInstant and TimeString objects.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Felix Klueckmann
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
public interface TimeFormatter {

	/**
	 * Returns the String representation of the given instant of time.
	 * 
	 * @param instant
	 *            TimeInstant: the instant of time to be formatted
	 * @return String: the String representation of the given timeInstant
	 */
	public String buildTimeString(TimeInstant instant);
	
	/**
	 * Returns the String representation of the given span of time.
	 * 
	 * @param span
	 *            TimeSpan: the instant of time to be formatted
	 * @return String: the String representation of the given timeSpan.
	 */
	public String buildTimeString(TimeSpan span);

}
