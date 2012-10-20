package desmoj.core.simulator;

import java.util.Enumeration;
import java.util.Vector;

import desmoj.core.advancedModellingFeatures.Res;
import desmoj.core.dist.NumericalDist;
import desmoj.core.exception.DelayedInterruptException;
import desmoj.core.exception.InterruptException;
import desmoj.core.report.ErrorMessage;

/**
 * Sim-process represents entities with an own active lifecycle. Since
 * Sim-processes are in fact special entities with extended capabilities (esp.
 * the method <tt>lifeCycle()</tt>), they inherit from Entity and thus can also
 * be used in conjunction with events. So they can be handled in both ways,
 * event- and process-oriented. Clients are supposed to implement the
 * lifeCycle() method to specify the individual behaviour of a special
 * SimProcess subclass. Since implementing activity- and transaction-oriented
 * synchronization mechanisms requires significant changes in this class,
 * methods that have been implemented by Soenke Claassen have been marked.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Tim Lechler.
 * @author Methods: canCooperate, clearInterruptCode, cooperate,
 *         getInterruptCode, getMaster, getSlaveWaitQueue, IsInterrupted,
 *         resetMaster and setSlaveWaitQueue by Soenke Claassen
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
public abstract class SimProcess extends Entity {

	/**
	 * The Thread needed for implementing coroutine behaviour.
	 */
	private Thread _myThread;
	
	/**
     * The scheduling priority of the process.
     */
    private int _mySchedulingPriority;

	/**
	 * Displays the current blocked status of this SimProcess. A sim-process is
	 * blocked whenever it has to wait inside a queue or synchronization object.
	 */
	protected boolean _isBlocked;

	/**
	 * Displays the current status of this SimProcess. Is <code>true</code> if
	 * lifeCycle method has finished, <code>false</code> if it is still running
	 * or has not been started yet
	 */
	protected boolean _isTerminated;

	/**
	 * Displays if the thread in of control of this SimProcess is already the
	 * associated simthread. Is <code>true</code> if the simthread is active and
	 * is carrying on its lifeCycle. Is <code>false</code> if it has not started
	 * its lifeCycle yet or is terminated already.
	 */
	protected boolean _isRunning;

	/**
	 * If this SimProcess is cooperating as a slave with a master process, it
	 * keeps a reference to its master here. Master is set in the
	 * <code>cooperate()</code> -method, when the slave cooperates with his
	 * master and deleted every time the slave process is activated.
	 * 
	 * @author Soenke Claassen
	 */
	private SimProcess _master;

	/**
	 * If this SimProcess is cooperating as a slave it has to wait in this
	 * waitQueue until a master is cooperating with it.
	 * 
	 * @author Soenke Claassen
	 */
	private ProcessQueue<? extends SimProcess> _slaveWaitQueue;

	/**
	 * The <code>InterruptCode</code> with which this SimProcess is interrupted.
	 * 
	 * @author Soenke Claassen
	 */

	private InterruptCode _irqCode;

	/**
	 * The <code>InterruptException</code> with which this SimProcess is
	 * interrupted.
	 * 
	 * @author Soenke Claassen
	 */
	private InterruptException _irqException;

	/**
	 * The <code>Vector</code> holding all the resources this SimProcess is
	 * using at the moment.
	 * 
	 * @author Soenke Claassen
	 */
	private final Vector<Resource> _usedResources;

	/**
	 * A reference to the container this SimProcess belongs to. Is
	 * <code>null</code> as long as this SimProcess is not contained in any
	 * <code>ComplexSimProcess</code>.
	 * 
	 * @author Soenke Claassen
	 */
	private ComplexSimProcess _supervisor;

	/**
	 * The realTime deadline for this SimProcess in nanoseconds. In case of a
	 * real-time execution (i. e. the execution speed rate is set to a positive
	 * value) the Scheduler will produce a warning message if a deadline is
	 * missed.
	 */
	private long _realTimeConstraint;

	/**
	 * The Event which will interrupt the current SimProcess at a given point in
	 * simulation time if it is not removed from the event list before that time
	 * instant.
	 */
	private ExternalEvent _currentlyScheduledDelayedInterruptEvent;

	/**
	 * Constructs a sim-process.
	 * 
	 * @param name
	 *            java.lang.String : The name of the sim-process
	 * @param owner
	 *            Model : The model this SimProcess is associated to
	 * @param showInTrace
	 *            boolean : Flag for showing SimProcess in trace-files. Set it
	 *            to <code>true</code> if SimProcess should show up in trace.
	 *            Set it to <code>false</code> if SimProcess should not be shown
	 *            in trace.
	 */
	public SimProcess(Model owner, String name, boolean showInTrace) {

		super(owner, name, showInTrace);

		// init variables
		_mySchedulingPriority = 0;
		_isBlocked = false; // not waiting in queue so far
		_isRunning = false; // not running so far
		_isTerminated = false; // not terminated either
		_master = null; // this SimProcess has no master, so far
		_slaveWaitQueue = null; // this SimProcess is not waiting in any queue
		_irqCode = null; // this is not interrupted
		_irqException = null; // this is not interrupted


		// set up the Vector holding the used Resources
		_usedResources = new Vector<Resource>();

		// this SimProcess is not contained in any ComplexSimProcess yet
		_supervisor = null;

	}

	/**
	 * Schedules the sim-process to be activated at the present point in
	 * simulation time, yielding the same result as calling
	 * <code>activate(new TimeSpan(0))</code>. The process will continue
	 * executing its <code>lifeCycle</code> method.
	 */
	public void activate() {
		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method: activate()",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		// tell in the trace when the sim-process will be activated
		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself now");
			} else { // this is not the currently running SimProcess
				sendTraceNote("activates " + getQuotedName() + " now");
			}
		}

		// schedule this SimProcess
		getModel().getExperiment().getScheduler()
				.schedule(this, null, new TimeSpan(0));

		// debug output
		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		resetMaster(); // if activate(TimeSpan dt) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.
	}

	/**
	 * @deprecated Replaced by activate(TimeSpan dt). Schedules the sim-process
	 *             to be activated at the given time offset to the current
	 *             simulation time. This will allow a sim-process to continue
	 *             executing its <code>lifeCycle</code> method.
	 * 
	 * @param dt
	 *            SimTime : The offset to the current simulation time that this
	 *            SimProcess is due to be activated
	 */
	@Deprecated
	public void activate(SimTime dt) {
		activate(SimTime.toTimeSpan(dt));

	}

	/**
	 * Schedules the sim-process to be activated at the given point in
	 * simulation time. This will allow a sim-process to continue executing its
	 * <code>lifeCycle</code> method.
	 * 
	 * @param when
	 *            TimeInstant : The point in simulation time this process is to
	 *            be activated.
	 */
	public void activate(TimeInstant when) {
		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: activate(TimeInstant when)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (when == null) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method:  void activate"
							+ "(TimeInstant when)",
					"The simulation time given as parameter is a null reference",
					"Be sure to have a valid simulation time reference before "
							+ "calling this method");
			return; // no proper parameter
		}
		// tell in the trace when the sim-process will be activated
		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				if (when == presentTime()) {
					sendTraceNote("activates itself immediately (NOW)");
				} else {
					if (TimeInstant.isEqual(when, presentTime())) {
						sendTraceNote("activates itself now");
					} else {
						sendTraceNote("activates itself at " + when.toString());

					}
				}
			} else { // this is not the currently running SimProcess

				if (when == presentTime()) {
					sendTraceNote("activates " + getQuotedName()
							+ " immediately (NOW)");
				} else {
					// stand auch oben: if (dt.getTimeValue() == 0.0) {
					if (TimeInstant.isEqual(when, presentTime())) {
						sendTraceNote("activates " + getQuotedName() + " now");
					} else {
						sendTraceNote("activates " + getQuotedName() + " at "
								+ when.toString());
					}
				}
			}
		}

		// schedule this SimProcess
		getModel().getExperiment().getScheduler().schedule(this, null, when);

		// debug output
		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		resetMaster(); // if activate(TimeInstant when) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.
	}

	/**
	 * Schedules the sim-process to be activated at the specified point in
	 * simulation time. The point of time is given as an offset to the current
	 * simulation time. This will allow a sim-process to continue executing its
	 * <code>lifeCycle</code> method. Thus in contrast to the entity, no Event
	 * is needed for scheduling here.
	 * 
	 * @param dt
	 *            TimeSpan : The offset to the current simulation time this
	 *            process is to be activated
	 */
	public void activate(TimeSpan dt) {
		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: activate(TimeSpan dt)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (dt == null) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method:  void activate"
							+ "(TimeSpan dt)",
					"The simulation time given as parameter is a null reference",
					"Be sure to have a valid simulation time reference before "
							+ "calling this method");
			return; // no proper parameter
		}
		// tell in the trace when the sim-process will be activated
		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				if (dt == TimeSpan.ZERO) {
					sendTraceNote("activates itself immediately (NOW)");
				} else {
					if (TimeSpan.isEqual(dt, TimeSpan.ZERO)) {
						sendTraceNote("activates itself now");
					} else {
						sendTraceNote("activates itself at "
								+ TimeOperations.add(presentTime(), dt)
										.toString());
					}
				}
			} else { // this is not the currently running SimProcess

				if (dt == TimeSpan.ZERO) {
					sendTraceNote("activates " + getQuotedName()
							+ " immediately (NOW)");
				} else {
					if (TimeSpan.isEqual(dt, TimeSpan.ZERO)) {
						sendTraceNote("activates " + getQuotedName() + " now");
					} else {
						sendTraceNote("activates "
								+ getQuotedName()
								+ " at "
								+ TimeOperations.add(presentTime(), dt)
										.toString());
					}
				}
			}
		}

		// schedule this SimProcess
		getModel().getExperiment().getScheduler().schedule(this, null, dt);

		// debug output
		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		resetMaster(); // if activate(TimeSpan dt) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.

	}

	/**
	 * Schedules this SimProcess to be activated directly after the given
	 * Schedulable, which itself is already scheduled. Note that this
	 * Sim-process' point of simulation time will be set to be the same as the
	 * Schedulable's time. Thus this SimProcess will continue to execute its
	 * <code>lifeCycle</code> method directly after the given Schedulable but
	 * the simulation clock will not change. Please make sure that the
	 * Schedulable given as parameter is actually scheduled.
	 * 
	 * @param after
	 *            Schedulable : The Schedulable this SimProcess should be
	 *            scheduled after
	 */
	public void activateAfter(Schedulable after) {

		if (after == null) {
			sendWarning(
					"Can't activate this SimProcess after the given SimProcess "
							+ "parameter! Command ignored.", "SimProcess : "
							+ getName() + " Method: void "
							+ "activateAfter(Schedulable after)",
					"The Schedulable given as parameter is a null reference",
					"Be sure to have a valid Schedulable reference before "
							+ "calling this method");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void activateAfter(Schedulable after)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself after " + getQuotedName());
			} else {
				sendTraceNote("activates " + getQuotedName() + " after "
						+ after.getQuotedName());
			}
		}

		// schedule this SimProcess
		getModel().getExperiment().getScheduler()
				.scheduleAfter(after, this, null);

		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated after " + after.getQuotedName()
					+ " on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

	}

	/**
	 * Schedules this SimProcess to be activated directly before the given
	 * Schedulable, which itself is already scheduled. Note that this
	 * Sim-process' point of simulation time will be set to be the same as the
	 * Schedulable's time. Thus this SimProcess will continue to execute its
	 * <code>lifeCycle</code> method directly before the given Schedulable but
	 * the simulation clock will not change. Please make sure that the
	 * Schedulable given as parameter is actually scheduled.
	 * 
	 * @param before
	 *            Schedulable : The Schedulable this SimProcess should be
	 *            scheduled before
	 */
	public void activateBefore(Schedulable before) {

		if (before == null) {
			sendWarning("Can't activate this SimProcess before the given "
					+ "SimProcess parameter", "SimProcess : " + getName()
					+ " Method: void activateBefore" + "(Schedulable before)",
					"The Schedulable given as parameter is a null reference",
					"Be sure to have a valid Schedulable reference before "
							+ "calling this method");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : "
							+ getName()
							+ " Method: void activateBefore(Schedulable before)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself before "
						+ before.getQuotedName());
			} else {
				sendTraceNote("activates " + getQuotedName() + " before "
						+ before.getQuotedName());
			}
		}

		// schedule this SimProcess
		getModel().getExperiment().getScheduler()
				.scheduleBefore(before, this, null);

		if (currentlySendDebugNotes()) {
			sendDebugNote("activateBefore " + before.getQuotedName()
					+ " on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		// hand control over to scheduler only if this is
		// a running thread of SimProcess
		// if ( isRunning ) passivate();

		resetMaster(); // if activateBefore() is called for this SimProcess,
		// there is no Master anymore controlling it.

	}

	/**
	 * 
	 * Clears the currently scheduled delayed interrupt so that it wont be
	 * performed. This Method should be called to cancel a previously scheduled
	 * delayed interrupt. This is typically the case if all steps to be covered
	 * by the delayed interrupt have been performed in time (before the delayed
	 * interrupt could be executed).
	 * 
	 */
	public void cancelInterruptDelayed() {

		if (isDelayedInterruptScheduled()) {
			sendTraceNote("canceling delayed interrupt scheduled at "
					+ _currentlyScheduledDelayedInterruptEvent.scheduledNext());
			_currentlyScheduledDelayedInterruptEvent.cancel();
			_currentlyScheduledDelayedInterruptEvent = null;
		} else {
			sendWarning(
					"Cannot cancel a delayed interrupt because no delayed interrupt is scheduled. Action ignored.",
					"SimProcess " + getName()
							+ " Method: cancelInterruptDelayed()",
					"No delayed interrupt has been scheduled.",
					"You can use the Method isDelayedInterruptScheduled() on a SimProcess to test whether a delayed interrupt has been scheduled for it.");
		}
	}

	public boolean isDelayedInterruptScheduled() {
		return _currentlyScheduledDelayedInterruptEvent != null;
	}

	/**
	 * Returns <code>true</code> if this process can cooperate with another
	 * Sim-process. If this process is already cooperating with a master
	 * <code>false</code> is returned.
	 * 
	 * @return boolean : Is this process ready to cooperate with another
	 *         SimProcess?
	 * @author Soenke Claassen
	 */
	public boolean canCooperate() {
		return _master == null; // if the master is not set yet this SimProcess
		// can cooperate with another SimProcess
	}

	/**
	 * Resets the interrupt-status of this SimProcess to not interrupted. Should
	 * be called every time the sim-process has successfully dealt with the
	 * interrupt. The internal <code>InterruptCode</code> of this SimProcess
	 * will be reset to <code>null</code>.
	 * 
	 * @author Soenke Claassen
	 */
	public void clearInterruptCode() {
		_irqCode = null;
	}

	/**
	 * As there is no generally applicable means of cloning a SimProcess (which
	 * would require cloning the execution state as well), this method returns a
	 * </code>CloneNotSupportedException</code>.
	 * 
	 * @return SimProcess : A copy of this process.
	 */
	protected SimProcess clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * The current (master) process is calling this method (within
	 * <code>WaitQueue.cooperate()</code>) on the slave process to lead him
	 * through the joint cooperation. After the joint cooperation is finished
	 * the master is still active and after him the slave will be activated.
	 * 
	 * @author Soenke Claassen
	 */
	public void cooperate() {
		// this is the slave and current the master

		// check if this slave already has a master
		if (_master != null) {
			sendWarning(
					"Slaves can not cooperate with more than one master at a "
							+ "time! The attempted cooperation is ignored.",
					"SimProcess : " + getName() + " Method: cooperate () ",
					"This slave process is already cooperating with another "
							+ "master: " + _master.getName(),
					"Be sure to have finished one cooperation before starting "
							+ "the next one.");
			return; // this process has a master already
		}

		// check if this slave is not terminated yet
		if (_isTerminated) {
			sendWarning(
					"Attempt to cooperate with a terminated slave process! "
							+ "The attempted cooperation is ignored.",
					"SimProcess : " + getName() + " Method: cooperate () ",
					"This slave process is already terminated.",
					"Make sure not to cooperate with terminated processes.");
			return; // this process is already terminated
		}

		// check the master
		SimProcess currentMaster = currentSimProcess(); // the current master
		// process
		if (currentMaster == null) // if currentMaster is only a null pointer
		{
			sendWarning("A non existing process is trying to cooperate as a "
					+ "master! The attempted cooperation is ignored!",
					"SimProcess : " + getName() + " Method: cooperate ()",
					"The master process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating with other "
							+ "processes. ");
			return; // the currentMaster process is only a null pointer
		}

		if (!isModelCompatible(currentMaster)) {
			sendWarning(
					"The given master SimProcess object does not "
							+ "belong to this model. The attempted cooperation is ignored!",
					"SimProcess : " + getName() + " Method: cooperate ()",
					"The master SimProcess is not modelcompatible.",
					"Make sure that the processes all belong to this model.");
			return; // the currentMaster is not modelcompatible
		}

		// the slave must be waiting in a WaitQueue
		if (_slaveWaitQueue == null) {
			sendWarning(
					"Attempt to cooperate with a slave process, that is not "
							+ "waiting in a WaitQueue. The attempted cooperation is ignored!",
					"SimProcess : " + getName() + " Method: cooperate ()",
					"Slave processes must wait in a WaitQueue before they can get into "
							+ "a cooperation.",
					"Make sure that the slave processes are waiting in a WaitQueue.");
			return; // the slave is not waiting in a queue
		}

		// now prepare for the real cooperation
		_master = currentMaster; // set the master for this slave process

		// leave a note in the trace
		if (_master.currentlySendTraceNotes()) {
			// trace note for a cooperation without any special conditions
			sendTraceNote("cooperates " + this.getQuotedName() + " from "
					+ _slaveWaitQueue.getQuotedName());
		}

		// get this slave out of his slaveWaitQueue
		_slaveWaitQueue.remove(this);
		// this slave process is not waiting in any slaveWaitingQueue anymore
		_slaveWaitQueue = null;
		// and therefore this slave process is not blocked anymore
		_isBlocked = false;

	}

	/**
	 * Method to release the waiting scheduler when the SimThread finishes.
	 */
	synchronized void freeThread() {

		notify(); // free the scheduler

	}

	/**
	 * Returns the InterruptCode from this SimProcess. If this SimProcess is not
	 * interrupted, the InterruptCode is <code>null</code>.
	 * 
	 * @return irqCode : The InterruptCode of this SimProcess.
	 * @author Soenke Claassen
	 */
	public InterruptCode getInterruptCode() {
		return _irqCode;
	}

	public InterruptException getInterruptException() {
		return _irqException;
	}

	/**
	 * Returns the master when two SimProcesses are cooperating. If this method
	 * is called on a sim-process which is not a slave <code>null</code> is
	 * returned.
	 * 
	 * @return SimProcess : The master process during the cooperation or
	 *         <code>null</code> if this process is not a slave process.
	 * @author Soenke Claassen
	 */
	public SimProcess getMaster() {
		return _master;
	}

	/**
	 * Returns the realTime deadline for this SimProcess (in nanoseconds). In
	 * case of a real-time execution (i. e. the execution speed rate is set to a
	 * positive value) the Scheduler will produce a warning message if a
	 * deadline is missed.
	 * 
	 * @return the realTimeConstraint in nanoseconds
	 */
	public long getRealTimeConstraint() {
		return _realTimeConstraint;
	}

	/**
	 * Returns the waiting-queue in which this SimProcess is waiting as a slave
	 * to cooperate with a master. If this method is called on a sim-process
	 * which is not a slave <code>null</code> is returned.
	 * 
	 * @return ProcessQueue : The waiting-queue in which this SimProcess is
	 *         waiting as a slave or <code>null</code> if this SimProcess is not
	 *         waiting as a slave for cooperation.
	 * @author Soenke Claassen
	 */
	public ProcessQueue<? extends SimProcess> getSlaveWaitQueue() {
		return _slaveWaitQueue;
	}

	/**
	 * Returns the supervising <code>ComplexSimProcess</code> this SimProcess is
	 * contained in.
	 * 
	 * @return desmoj.ComplexSimProcess : The supervising
	 *         <code>ComplexSimProcess</code> this SimProcess is contained in.
	 *         Is <code>null</code> if this SimProcess is not contained in any
	 *         <code>ComplexSimProcess</code>.
	 * @author Soenke Claassen
	 */
	public ComplexSimProcess getSupervisor() {

		return _supervisor;
	}

	/**
	 * Returns a clone of the internal <code>Vector</code> containing all the
	 * <code>Resource</code> objects this SimProcess is using at the moment.
	 * 
	 * @return java.util.Vector : the internal <code>Vector</code> containing
	 *         all the <code>Resource</code> objects this SimProcess is using at
	 *         the moment.
	 * @author Soenke Claassen
	 */
	protected Vector<Resource> getUsedResources() {

		// clone the internal Vector
		@SuppressWarnings("unchecked")
		Vector<Resource> usedRes = (Vector<Resource>) _usedResources.clone();

		// return the cloned Vector
		return usedRes;
	}

	/**
	 * @deprecated Replaced by hold(TimeSpan dt). Passivates a sim-process for
	 *             the time given. The simthread of this SimProcess is put into
	 *             a lock and the scheduler, resp. the experiment's main thread
	 *             is released from its block and continues with the next
	 *             EventNote to be processed.
	 * 
	 * @param dt
	 *            desmoj.SimTime : The duration of the sim-process' passivation
	 */
	@Deprecated
	public void hold(SimTime dt) throws DelayedInterruptException,
			InterruptException {
		hold(SimTime.toTimeSpan(dt));

	}

	/**
	 * Passivates a sim-process until the given point in simulation time. The
	 * simthread of this SimProcess is put into a lock and the scheduler, resp.
	 * the experiment's main thread is released from its block and continues
	 * with the next event-note to be processed.
	 * 
	 * @param until
	 *            TimeInstant : The point in simulation time when the
	 *            SimProcess' passivation ends.
	 * 
	 */
	public void hold(TimeInstant until) throws DelayedInterruptException,
			InterruptException {
		if ((until == null)) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeInstant until)",
					"The TimeInstant given as parameter is a null reference.",
					"Be sure to have a valid TimeInstant reference before calling this method.");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: hold(TimeInstant until)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be set to be activated by other "
							+ "SimProcesses or events!");
			return; // is blocked in some synch construction
		}

		if (isScheduled()) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeInstant until)",
					"The sim-process to be scheduled is already scheduled.",
					"Use method reActivate(TimeInstant when) to shift the sim-process "
							+ "to be scheduled at some other point of time.");
			return; // was already scheduled
		}

		if (TimeInstant.isBefore(until, presentTime())) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeInstant until)",
					"The instant given is in the past.",
					"To hold a sim-process, use a TimeInstant no earlier than the present time. "
							+ "The present time can be obtained using the "
							+ "presentTime() method.");
			return; // do not hold
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("holds until " + until.toString());
			} else {
				sendTraceNote("holds " + getQuotedName() + "until "
						+ until.toString());
			}
			skipTraceNote(); // skip passivate message
		}

		// schedule to be reactivated at the point of simulation time "until"
		getModel().getExperiment().getScheduler().schedule(this, null, until);

		if (currentlySendDebugNotes()) {
			sendDebugNote("holds on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		// hand control over to scheduler only if this is
		// a running thread of SimProcess
		passivate();
	}

	/**
	 * Passivates a sim-process for the given span of time. The simthread of
	 * this Sim-process is put into a lock and the scheduler, resp. the
	 * experiment's main thread is released from its block and continues with
	 * the next EventNote to be processed.
	 * 
	 * @param dt
	 *            TimeSpan : The duration of the sim-process' passivation
	 */
	public void hold(TimeSpan dt) throws DelayedInterruptException,
			InterruptException {
		if ((dt == null)) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeSpan dt)",
					"The TimeSpan given as parameter is a null reference.",
					"Be sure to have a valid TimeSpan reference before calling this method.");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method: hold(TimeSpan dt)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be set to be activated by other "
							+ "SimProcesses or events!");
			return; // is blocked in some synch construction
		}

		if (isScheduled()) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeSpan dt)",
					"The sim-process to be scheduled is already scheduled.",
					"Use method reActivate(TimeSpan dt) to shift the sim-process "
							+ "to be scheduled at some other point of time.");
			return; // was already scheduled
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("holds for " + dt.toString() + " until "
						+ TimeOperations.add(presentTime(), dt).toString());
			} else {
				sendTraceNote("holds " + getQuotedName() + "for "
						+ dt.toString() + " until "
						+ TimeOperations.add(presentTime(), dt).toString());
			}
			skipTraceNote(); // skip passivate message
		}

		// schedule to be reactivated in dt
		getModel().getExperiment().getScheduler().schedule(this, null, dt);

		if (currentlySendDebugNotes()) {
			sendDebugNote("holds on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		// hand control over to scheduler only if this is
		// a running thread of SimProcess
		passivate();
	}
	
	/**
     * Passivates a SimProcess for span of time sampled from the distribution
     * provided to the method. The sample is interpreted in the reference time 
     * unit. The SimThread of this SimProcess is put into a lock and the scheduler, resp. the
     * experiment's main thread is released from its block and continues with
     * the next EventNote to be processed.
     * 
     * @param dist
     *            NumericalDist<?> : Numerical distribution to sample the 
     *            duration of the sim-process' passivation from
     */
    public void hold(NumericalDist<?> dist) throws DelayedInterruptException,
            InterruptException {
        
        if ((dist == null)) {
            sendWarning("Can't schedule SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: void hold(NumericalDist<?> dist)",
                    "The NumericalDist given as parameter is a null reference.",
                    "Be sure to have a valid NumericalDist reference before calling this method.");
            return; // no proper parameter
        }

        if (isBlocked()) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName() + " Method: hold(NumericalDist<?> dist)",
                    "The sim-process to be activated is blocked inside "
                            + "a higher level synchronization object.",
                    "Simprocesses waiting inside higher synchronization "
                            + "constructs can not be set to be activated by other "
                            + "SimProcesses or events!");
            return; // is blocked in some synch construction
        }

        if (isScheduled()) {
            sendWarning("Can't schedule SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: void hold(NumericalDist<?> dist)",
                    "The sim-process to be scheduled is already scheduled.",
                    "Use method reActivate(TimeSpan dt) to shift the sim-process "
                            + "to be scheduled at some other point of time.");
            return; // was already scheduled
        }
                
        // determine time span
        TimeSpan dt = dist.sampleTimeSpan();

        if (currentlySendTraceNotes()) {
            if (this == currentSimProcess()) {
                sendTraceNote("holds for " + dt.toString() + " until "
                        + TimeOperations.add(presentTime(), dt).toString() + " as sampled from " + dist.getQuotedName());
            } else {
                sendTraceNote("holds " + getQuotedName() + "for "
                        + dt.toString() + " until "
                        + TimeOperations.add(presentTime(), dt).toString() + " as sampled from " + dist.getQuotedName());
            }
            skipTraceNote(); // skip passivate message
        }

        // schedule to be reactivated in dt
        getModel().getExperiment().getScheduler().schedule(this, null, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("holds on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

        // hand control over to scheduler only if this is
        // a running thread of SimProcess
        passivate();
    }

	/**
	 * Interrupts the sim-process setting the given InterruptCode as the reason
	 * for the interruption. Blocked, terminated or already interrupted
	 * Sim-processes can not be interrupted. In this case a warning message will
	 * be produced and the interrupt will be ignord. If the sim-process is
	 * cooperating as a slave the interrupt will be passed to the master.
	 * 
	 * @param interruptReason
	 *            desmoj.InterruptCode
	 */
	public void interrupt(InterruptCode interruptReason) {

		if (interruptReason == null) {
			sendWarning(
					"Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"The InterruptCode given as parameter is a null reference.",
					"Be sure to have a valid InterruptCode reference before "
							+ "calling this method.");
			return; // no proper parameter
		}

		// if the sim-process is cooperating as a slave
		if (_master != null) {
			if (currentlySendTraceNotes()) {
				sendTraceNote("interrupts '" + this.getName() + "' , who ...");
			}

			// interrupt the master, too. (with the same reason/InterruptCode)
			_master.interrupt(interruptReason);
		}

		if (isBlocked()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"Blocked SimProcesses can not be interrupted.",
					"You can check if a sim-process is blocked using method "
							+ "isBlocked().");
			return; // is Blocked
		}

		if (isTerminated()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"Terminated SimProcesses can not be interrupted.",
					"You can check if a sim-process is terminated using method "
							+ "isTerminated().");
			return; // is Terminated
		}

		if (isInterrupted()) {
			sendAWarningThatTheCurrentSimProcessHasAlreadyBeenInterrupted("SimProcess : "
					+ getName()
					+ " Method: void "
					+ "interrupt(InterruptCode interruptReason)");
			return;
		}

		if (this == currentSimProcess()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"SimProcess is the currently active SimProcess.",
					"Make sure not to interrupt the currently active "
							+ "SimProcess.");
			return; // is currentSimProcess
		}

		if (currentlySendTraceNotes()) {
			sendTraceNote("interrupts '" + this.getName() + "', with reason "
					+ interruptReason.getName() + " ["
					+ interruptReason.getCodeNumber() + "]");
		}

		_irqCode = interruptReason; // set the InterruptCode

		// if on EventList, remove first ...
		if (isScheduled()) {
			skipTraceNote(2);
			cancel();
		} else {
			skipTraceNote();
		}
		// ... then activate after the one interrupting this SimProcess
		activateAfter(current());
	}

	/**
	 * Interrupts the sim-process by throwing the given InterruptException in
	 * it's lifeCylce() method. The InterruptException contains an InterruptCode
	 * as the reason for the interruption. Blocked, terminated or already
	 * interrupted Sim-processes can not be interrupted. In this case a warning
	 * message will be produced and the interrupt will be ignord. If the
	 * sim-process is cooperating as a slave the interrupt will be passed to the
	 * master.
	 * 
	 * @param interruptReason
	 *            desmoj.InterruptException
	 */
	public void interrupt(InterruptException interruptReason) {
		if (interruptReason == null
				|| interruptReason.getInterruptCode() == null) {
			sendWarning(
					"Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptException interruptReason)",
					"Either the InterruptException given as parameter or the InterruptCode contained in that Exception is a null reference.",
					"Be sure to have a valid InterruptCode reference before "
							+ "calling this method.");
			return; // no proper parameter
		}

		// if the sim-process is cooperating as a slave
		if (_master != null) {
			if (currentlySendTraceNotes()) {
				sendTraceNote("interrupts '" + this.getName() + "' , who ...");
			}

			// interrupt the master, too. (with the same reason/InterruptCode)
			_master.interrupt(interruptReason);
		}

		if (isBlocked()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"Blocked SimProcesses can not be interrupted.",
					"You can check if a sim-process is blocked using method "
							+ "isBlocked().");
			return; // is Blocked
		}

		if (isTerminated()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptException interruptReason)",
					"Terminated SimProcesses can not be interrupted.",
					"You can check if a sim-process is terminated using method "
							+ "isTerminated().");
			return; // is Terminated
		}

		if (isInterrupted()) {
			sendAWarningThatTheCurrentSimProcessHasAlreadyBeenInterrupted("SimProcess : "
					+ getName()
					+ " Method: void "
					+ "interrupt(InterruptException interruptReason)");
			return;
		}

		if (this == currentSimProcess()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptException interruptReason)",
					"SimProcess is the currently active SimProcess.",
					"Make sure not to interrupt the currently active "
							+ "SimProcess.");
			return; // is currentSimProcess
		}

		if (currentlySendTraceNotes()) {
			sendTraceNote("interrupts '" + this.getName() + "', with reason "
					+ interruptReason.getInterruptCode().getName() + " ["
					+ interruptReason.getInterruptCode().getCodeNumber() + "]");
		}

		_irqException = interruptReason; // set the InterruptException

		// if on EventList, remove first ...
		if (isScheduled()) {
			skipTraceNote(2);
			cancel();
		} else {
			skipTraceNote();
		}
		// ... then activate after the one interrupting this SimProcess
		activateAfter(current());
	}

	/**
	 * Schedules this process to be interrupted at the given point in simulation
	 * time. Only one delayed interrupt can be scheduled at a time. If a delayed
	 * interrupt is scheduled after another delayed interrupt has already been
	 * scheduled a warning message will be produced and the new delayed
	 * interrupt will not be scheduled.
	 * 
	 * A delayed Interrupt must be cleared manually by calling
	 * clearInterruptDelayed() on the SimProcess if it wasn't performed.
	 * 
	 * @param when
	 *            The Point in time when the interrupt is to be performed.
	 * 
	 * @return The event which will (at the given point in time) triger the
	 *         interrupt of this process
	 */
	public ExternalEvent interruptDelayed(TimeInstant when) {
		ExternalEvent delayedInterruptEvent;

		// when pr�fen

		if (_currentlyScheduledDelayedInterruptEvent != null) {
			sendWarning(
					"Can't schedule a delayed interrupt of this SimProcess! CommandIgnored",
					"SimProcess: "
							+ getName()
							+ " method: void interruptDelayed(TimeInstant when)",
					"Another delayed interrupt has already been scheduled.",
					"A delayed interrupt may only be scheduled if no other delayed interrupt has been scheduled on that SimProcess."
							+ " Did you maybe forget to clear the last delayed interrupt. Delyaed interrupts aren't cleared automatically but must be cleared manually by a call to the method "
							+ "SimProcess#clearInterruptDelayed().");
			return null;
		}

		delayedInterruptEvent = new ExternalEvent(getModel(),
				"DelayedInterruptEvent", true) {

			@Override
			public void eventRoutine() {
				// Interrupt the SimProcess
				SimProcess.this.interrupt(new DelayedInterruptException(
						new InterruptCode("InternalDelayedInterrupt")));
				// Unset the _currentlyScheduledDelayedInterruptEvent so the
				// user is free to schedule another delyed interrupt.
				_currentlyScheduledDelayedInterruptEvent = null;
			}
		};
		if (currentlySendTraceNotes()) {
			sendTraceNote("scheduling a delayed interrupt at " + when);
		}
		skipTraceNote();
		delayedInterruptEvent.schedule(when);

		if (delayedInterruptEvent.isScheduled()) {
			// If the delayed interrupt event has been scheduled auccessfully
			// save it to an instance variable so it may be unscheduled if
			// necessary.
			_currentlyScheduledDelayedInterruptEvent = delayedInterruptEvent;
		} else {
			// For some reason the delayed interrupt event wasn't scheduled. A
			// warning should already haven been sent. So do nothing.
		}

		return _currentlyScheduledDelayedInterruptEvent;
	}

	/**
	 * Schedules this process to be interrupted after the given delay. Only one
	 * delayed interrupt can be scheduled at a time. If a delayed interrupt is
	 * scheduled after another delayed interrupt has already been scheduled a
	 * warning message will be produced and the delayed interrupt will not be
	 * scheduled.
	 * 
	 * A delayed Interrupt must be cleared manually by calling
	 * clearInterruptDelayed() on the SimProcess if it wasn't performed.
	 * 
	 * @param delay
	 *            The delay after which the interrupt is to be performed.
	 * 
	 * @return The event which will (after the given delay) triger the interrupt
	 *         of this process
	 */
	public ExternalEvent interruptDelayed(TimeSpan delay) {
		TimeInstant when;

		when = TimeOperations.add(presentTime(), delay);

		return interruptDelayed(when);
	}

	/**
	 * Returns the current block-status of the sim-process. If a sim-process is
	 * blocked, it is waiting inside a queue or synchronization block for it's
	 * release.
	 * 
	 * @return boolean : Is <code>true</code> if SimProcess is blocked,
	 *         <code>false</code> otherwise
	 */
	public boolean isBlocked() {

		return _isBlocked;

	}

	/**
	 * Returns the current component status of this SimProcess. If a sim-process
	 * is a component of a <code>ComplexSimProcess</code> it is blocked and
	 * passivated. It exists only within the <code>ComplexSimProcess</code>;
	 * it's own lifeCycle is stopped and will only be activated again when it is
	 * removed from the <code>ComplexSimProcess</code>.
	 * 
	 * @return boolean :<code>true</code> if and only if this SimProcess is a
	 *         component (part of) a <code>ComplexSimProcess</code>;
	 *         <code>false</code> otherwise.
	 * @author Soenke Claassen
	 */
	public boolean isComponent() {

		return (_supervisor != null);
	}

	/**
	 * Returns the current interrupt-status of this SimProcess. If a sim-process
	 * is interrupted, it should deal with the interrupt and then call the
	 * <code>clearInterruptCode()</code> -method.
	 * 
	 * @return boolean : Is <code>true</code> if this SimProcess is interrupted,
	 *         <code>false</code> otherwise.
	 * @author Soenke Claassen
	 */
	public boolean isInterrupted() {
		return (_irqCode != null || _irqException != null);
	}

	/**
	 * Returns the current running status of the sim-process. If a sim-process
	 * is not ready, it has already finished its <code>lifeCycle()</code> method
	 * and can not further be used as a sim-process. A terminated SimProcess can
	 * still be used like any other Entity which it is derived from.
	 * 
	 * @return boolean : Is <code>true</code> if the sim-process is terminated,
	 *         <code>false</code> otherwise
	 * @see Entity
	 */
	boolean isReady() {

		return _isRunning;

	}

	/**
	 * Returns the current status of the sim-process. If a sim-process is
	 * terminated, it has already finished its <code>lifeCycle()</code> method
	 * and can not further be used as a sim-process. A terminated SimProcess can
	 * still be used like any other Entity which it is derived from.
	 * 
	 * @return boolean : Is <code>true</code> if the sim-process is terminated,
	 *         <code>false</code> otherwise
	 * @see Entity
	 */
	public boolean isTerminated() {

		return _isTerminated;

	}

	/**
	 * Override this method in a subclass of SimProcess to implement that
	 * Sim-process' specific behaviour. This method starts after a sim-process
	 * has been created and activated by the scheduler.
	 */
	public abstract void lifeCycle();

	/**
	 * Makes the sim-process obtain an array of resources and store them for
	 * further usage.
	 * 
	 * @param obtainedResources
	 *            Resource[] : The array of resources obtained.
	 * 
	 * @author Soenke Claassen
	 */
	public void obtainResources(Resource[] obtainedResources) {
		if (obtainedResources.length <= 0) {
			sendWarning("Attempt to obtain resources, but got none! Command "
					+ "ignored!", "SimProcess : " + getName()
					+ " Method:  void obtain"
					+ "Resources(Resource[] obtainedResources)",
					"The array of obtained resources is empty.",
					"Make sure to obtain at least one resource. Check if the "
							+ "resource pool can provide any resources.");
			return; // parameter contains nothing
		}

		// put all the obtained resources in the Vector of used resources
		for (Resource obtainedResource : obtainedResources) {
			_usedResources.addElement(obtainedResource);
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string of all resources used by this SimProcess
			String t = "uses: ";

			for (Resource resource : _usedResources) {
				t += "<br>" + (resource).getName();
			}

			sendDebugNote(t);
		}
	}

	/**
	 * Passivates the sim-process for an indefinite time. This method must be
	 * called by the sim-process' own Thread only. The sim-process can only be
	 * reactivated by another SimProcess or Entity.
	 */
	public synchronized void passivate() throws DelayedInterruptException,
			InterruptException {

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("passivates");
			} else {
				sendTraceNote("passivates " + getQuotedName());
			}
		}

		notify(); // frees the scheduler after wait()

		try {
			wait();
		} catch (InterruptedException ioEx) {
			// create eror message
			ErrorMessage errmsg = new ErrorMessage(getModel(),
					"Simulation stopped!",
					"Exception thrown by Java VM" + ioEx,
					"Thread conflict assumed.", "Check Java VM.", presentTime());
			// throw it back to Experiment's start routine
			throw (new desmoj.core.exception.DESMOJException(errmsg));
		}

		// if simulation is not running, throw SimFinishedException to stop
		// thread
		if (getModel().getExperiment().isAborted()) {
			throw (new desmoj.core.exception.SimFinishedException(getModel(),
					getName(), presentTime()));
		}

		if (_irqException != null) {
			// The SimProcess has been interrupted. First reset the
			// _irqException, then throw the exception so that it can be caught
			// in the lifeCycle() method of this SimProcess.

			if (currentlySendTraceNotes()) {
				sendTraceNote("throwing "
						+ _irqException.getClass().getSimpleName()
						+ " to interrupt the process");
			}

			InterruptException tmpIrqException = _irqException;
			_irqException = null;

			throw tmpIrqException;
		}
	}

	/**
	 * The current (master) process is calling this method (within
	 * <code>TransportJunction.cooperate()</code>) on the slave process to make
	 * him prepare for the transportation. After the transport is finished the
	 * master is still active and after him the slave will be activated.
	 * 
	 * @author Soenke Claassen
	 */
	public void prepareTransport() {
		// this is the slave and current the master

		// check if this slave already has a master
		if (_master != null) {
			sendWarning(
					"Slaves can not be transported from more than one master at "
							+ "a time! The attempted transport is ignored.",
					"SimProcess : " + getName()
							+ " Method: prepareTransport () ",
					"This slave process is already transported by another "
							+ "master: " + _master.getName(),
					"Be sure to have finished one transportation before starting "
							+ "the next one.");
			return; // this process has a master already
		}

		// check if this slave is not terminated yet
		if (_isTerminated) {
			sendWarning("Attempt to transport a terminated slave process! "
					+ "The attempted transport is ignored.", "SimProcess : "
					+ getName() + " Method: prepareTransport () ",
					"This slave process is already terminated.",
					"Make sure not to transport terminated processes.");
			return; // this process is already terminated
		}

		// check the master
		SimProcess currentMaster = currentSimProcess(); // the current master
		// process
		if (currentMaster == null) // if currentMaster is only a null pointer
		{
			sendWarning(
					"A non existing process is trying to transport other "
							+ "processes as a master! The attempted transport is ignored!",
					"SimProcess : " + getName()
							+ " Method: prepareTransport ()",
					"The master process is only a null pointer.",
					"Make sure that only real SimProcesses are transporting other "
							+ "processes. ");
			return; // the currentMaster process is only a null pointer
		}

		if (!isModelCompatible(currentMaster)) {
			sendWarning(
					"The given master SimProcess object does not "
							+ "belong to this model. The attempted transport is ignored!",
					"SimProcess : " + getName()
							+ " Method: prepareTransport ()",
					"The master SimProcess is not modelcompatible.",
					"Make sure that the processes all belong to this model.");
			return; // the currentMaster is not modelcompatible
		}

		// the slave must be waiting in a WaitQueue
		if (_slaveWaitQueue == null) {
			sendWarning(
					"Attempt to transport a slave process, that is not "
							+ "waiting in a TransportJunction. The attempted transport is ignored!",
					"SimProcess : " + getName()
							+ " Method: prepareTransport ()",
					"Slave processes must wait in a TransportJunction before they can be "
							+ "transported.",
					"Make sure that the slave processes are waiting in a "
							+ "TransportJunction.");
			return; // the slave is not waiting in a queue
		}

		// now prepare for the real cooperation
		_master = currentMaster; // set the master for this slave process

		// leave a note in the trace
		if (_master.currentlySendTraceNotes()) {
			// trace note for a transport without any special conditions
			sendTraceNote("transports " + this.getQuotedName() + " from "
					+ _slaveWaitQueue.getQuotedName());
		}

		// get this slave out of his slaveWaitQueue
		_slaveWaitQueue.remove(this);
		// this slave process is not waiting in any slaveWaitingQueue anymore
		_slaveWaitQueue = null;
		// and therefore this slave process is not blocked anymore
		_isBlocked = false;

	}

	/**
	 * Re-schedules the sim-process to be activated at the given TimeSpan offset
	 * to the current simulation time. The Simprocess has already been scheduled
	 * but is now supposed to be reactivated at some other point of simulation
	 * time.
	 * 
	 * @param dt
	 *            TimeSpan : The offset to the current simulation time that this
	 *            SimProcess is due to be re-activated
	 */
	public void reActivate(TimeSpan dt) {
		if (isBlocked()) {
			sendWarning(
					"Can't reactivate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: reActivate(TimeSpan dt)",
					"The sim-process to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (!isScheduled()) {
			sendWarning("Can't reactivate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: reActivate(TimeSpan dt)",
					"The sim-process to be reactivated is not scheduled.",
					"Use method activate(TimeSpan dt) to activate a sim-process"
							+ "that is not scheduled yet.");
			return; // was already scheduled
		}

		if (dt == null) {
			sendWarning(
					"Can't reactivate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method:  void reActivate"
							+ "(TimeSpan dt)",
					"The simulation time given as parameter is a null reference",
					"Be sure to have a valid simulation time reference before "
							+ "calling this method");
			return; // no proper parameter
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				if (dt == TimeSpan.ZERO) {
					sendTraceNote("reactivates itself now");
				} else {
					sendTraceNote("reactivates itself at "
							+ TimeOperations.add(presentTime(), dt));
				}
			} else {
				if (dt == TimeSpan.ZERO) {
					sendTraceNote("reactivates " + getQuotedName() + " now");
				} else {
					sendTraceNote("reactivates " + getQuotedName() + " at "
							+ TimeOperations.add(presentTime(), dt));
				}
			}
		}

		getModel().getExperiment().getScheduler().reSchedule(this, dt);

		resetMaster(); // if reActivate(TimeSpan dt) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.
	}

	/**
	 * Gets the InterruptCode from the master and resets the master to
	 * <code>null</code>.
	 * 
	 * 
	 * @author Soenke Claassen
	 */
	public void resetMaster() {
		if (_master != null) {
			_irqCode = _master.getInterruptCode();
			_irqException = _master.getInterruptException();
		}

		_master = null;
	}

	/**
	 * Used to synchronize the change of control between scheduler and
	 * Sim-processes. This method must only be called by the scheduler resp. the
	 * experiment's main thread in order to prevent multiple SimProcess' threads
	 * running in parallel which has to be avoided.
	 */
	synchronized void resume() {

		// check that the SimThread has not finished yet
		if (_isTerminated) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method: void resume()",
					"The sim-process' lifeCycle method has already terminated.",
					"Be sure to check the sim-process' status before resuming."
							+ " Use method isTerminated() to check the current status");
			return;
		}

		// wake up the SimThread waiting in a block for the sim-process' lock
		// to be released
		notify();

		// now go wait until the next notification by the SimThread
		// of this SimProcess
		try {
			wait();
		} catch (InterruptedException irqEx) { // must be caught when using
			// wait
			// create eror message
			ErrorMessage errmsg = new ErrorMessage(getModel(),
					"Simulation stopped!",
					"InterruptedException thrown by Java VM : " + irqEx,
					"Thread conflict assumed.", "Check Java VM.", presentTime());
			// throw it back to Experiment's start routine
			throw (new desmoj.core.exception.DESMOJException(errmsg));
		}

	}

	/**
	 * Makes the sim-process return all resources it holds at the moment to all
	 * the different Res pools it is holding resources from. This is useful in
	 * situations the Simprocess is about to terminate.
	 * 
	 * @author Soenke Claassen
	 */
	public void returnAllResources() {
		// check if something can be returned
		if (_usedResources.isEmpty()) {
			sendWarning(
					"Attempt to return all resources, but the "
							+ "SimProcess does not hold any resources! Command ignored!",
					"SimProcess : " + getName()
							+ " Method: returnAllResources()",
					"If the sim-process does not hold any resources it is "
							+ "impossible to return any.",
					"Make sure that the sim-process holds resources that "
							+ "should be returned!");
			return; // return nothing, go to where you came from
		}

		// repeat while vector of usedResources is not empty
		while (!_usedResources.isEmpty()) {
			// get the first resource and check the Res pool it belongs to
			Res crntResPool = _usedResources.firstElement().getResPool();

			// counter how many resources of that res pool are used
			int n = 1;

			// search the whole vector of usedResources for resources of the
			// current
			// Res pool
			for (int i = 1; i < _usedResources.size(); i++) {
				// is the resource of the desired Res pool?
				if (_usedResources.elementAt(i).getResPool() == crntResPool) {
					n++; // increase the counter
				}
			} // end for-loop

			// make the array to store the resources which will be returned
			Resource[] returningRes = new Resource[n];

			// counter for the index of the array
			int k = 0;

			// collect all the resources from the Vector of usedResources
			for (int j = 0; j < _usedResources.size(); j++) {
				// is the resource of the desired Res pool?
				if ((_usedResources.elementAt(j)).getResPool() == crntResPool) {
					// put res in array
					returningRes[k] = _usedResources.elementAt(j);
					k++; // increase counter of array
				}
				if (k == n) {
					break; // stop the for-loop
				}
			}

			// return the array of resources to the Res pool they belong to
			crntResPool.takeBack(returningRes);

			// remove the returned resources from the vector of usedResources
			for (int m = 0; m < n; m++) // go through the array of
			// returningResources
			{
				// remove each resource that is in the array of
				// returningResources
				_usedResources.removeElement(returningRes[m]);
			}

		} // end while

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string including all elements of the vector usedResources
			String s = "All resources returned! Contents of vector usedResources: ";

			if (_usedResources.isEmpty()) // anything left ?
			{
				s += "<br>none";
			}

			for (Enumeration<Resource> e = _usedResources.elements(); e
					.hasMoreElements();) {
				s += e.nextElement();
			}

			// send a debugNote representing the state of the vector
			// usedResources
			sendDebugNote(s);
		}

	} // end method returnAllResources

	/**
	 * Makes the sim-process return a certain number of resources of the given
	 * resource pool.
	 * 
	 * @param resPool
	 *            Res : The resource pool which resources will be returned.
	 * @param n
	 *            int : The number of resources which will be returned.
	 * @return Resource[] : the array containing the resources which will be
	 *         returned.
	 * @author Soenke Claassen
	 */
	public Resource[] returnResources(Res resPool, int n) {
		// check if nothing should be returned
		if (n <= 0) {
			sendWarning(
					"Attempt to return no or a negative number of resources! "
							+ " Command ignored!", "SimProcess : " + getName()
							+ " Method:  Resource[] "
							+ "returnResources(Res resPool, int n)",
					"It makes no sense to return nothing or a negative number "
							+ "of resources.",
					"Make sure to return at least one resource. Only resources "
							+ "which have been obtained once can be returned!");
			return null; // return nothing, go to where you came from
		}

		// check if nothing can be returned
		if (_usedResources.isEmpty()) {
			sendWarning(
					"Attempt to return a number of resources, but the "
							+ "SimProcess does not hold any resources! Command ignored!",
					"SimProcess : " + getName() + " Method:  Resource[] "
							+ "returnResources(Res resPool, int n)",
					"If the sim-process does not hold any resources it is "
							+ "impossible to return any.",
					"Make sure that the sim-process holds the resources that "
							+ "should be returned!");
			return null; // return nothing, go to where you came from
		}

		// make the array to store the resources which will be returned
		Resource[] returningRes = new Resource[n];

		// counter for the index of the array
		int j = 0;

		// collect all the resources from the Vector of usedResources
		for (int i = 0; i < _usedResources.size(); i++) {
			// is the resource of the desired kind?
			if ((_usedResources.elementAt(i)).getResPool() == resPool) {
				// put res in array
				returningRes[j] = _usedResources.elementAt(i);
				j++; // increase counter of array
			}
			if (j == n) {
				break; // stop the for-loop
			}
		}

		// for debugging: make a string of all returning resources
		String s = "<b>returns</b> to Res '" + resPool.getName() + "' : ";

		// remove the returning resources from the vector of usedResources
		for (int m = 0; m < j; m++) // go through the array of
		// returningResources
		{
			// remove each resource that is in the array of returningResources
			_usedResources.removeElement(returningRes[m]);

			// add them to string of returning resources
			s += "<br>" + returningRes[m].getName();
		}

		if (j < n) // array is not full
		{
			sendWarning("Attempt to return " + n
					+ " resources to the Res pool. "
					+ "But the sim-process holds only" + j
					+ "resources of that " + "kind. The " + j
					+ "resources will be returned.", "SimProcess : "
					+ getName() + " Method:  Resource[] "
					+ "returnResources(Res resPool, int n)",
					"The sim-process can not return " + n + " resources, "
							+ "because it holds only" + j + "resources.",
					"Make sure that the sim-process holds at least as many "
							+ "resources as it should return.");
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			sendDebugNote(s);

			// make a string of all resources still held by this SimProcess
			String t = "still holds: ";

			if (_usedResources.isEmpty()) // anything left ?
			{
				t += "<br>none";
			}

			for (Resource resource : _usedResources) {
				t += "<br>" + (resource).getName();
			}

			sendDebugNote(t);
		}

		return returningRes; // return the array of resources
	}

	private void sendAWarningThatTheCurrentSimProcessHasAlreadyBeenInterrupted(
			String location) {
		String reason;

		if (_irqCode != null) {
			reason = "The SimProcess already has an InterruptCode set: "
					+ _irqCode.getName();
		} else if (_irqException != null) {
			reason = "The SimProcess already has an InterruptException set which contains the following InterruptCode: "
					+ _irqException.getInterruptCode().getName();
		} else {
			throw new RuntimeException(
					"Apparently this method has been called although the current process hasn't been interrupted.");
			// This should never happen.
		}

		// is Interrupted
		sendWarning(
				"Can't interrupt SimProcess! Command ignored",
				location,
				reason,
				"SimProcesses may only be interrupted if no other "
						+ "InterruptCode or InterruptException is set on that SimProcess. You can check "
						+ "on that using the mehtod isInterrupted(), which must return "
						+ "false if no other InterruptCode or InterruptException is set.");
	}

	/**
	 * Sets the sim-process' blocked status to the boolean value given. This is
	 * necessary for some operations in conjunction with some synchronization
	 * classes.
	 * 
	 * @param blockStatus
	 *            boolean : The new value for the blocked status
	 */
	public void setBlocked(boolean blockStatus) {

		_isBlocked = blockStatus;

	}

	/**
	 * Sets the realTime deadline for this SimProcess (in nanoseconds). In case
	 * of a real-time execution (i. e. the execution speed rate is set to a
	 * positive value) the Scheduler will produce a warning message if a
	 * deadline is missed.
	 * 
	 * @param realTimeConstraint
	 *            the realTimeConstraint in nanoseconds to set
	 */
	public void setRealTimeConstraint(long realTimeConstraint) {
		_realTimeConstraint = realTimeConstraint;
	}

	/**
	 * Sets the sim-process' running status to the boolean value given. This is
	 * necessary for some operations in conjunction with synchronization
	 * classes.
	 * 
	 * @param runStatus
	 *            boolean : The new value for the running status
	 */
	void setRunning(boolean runStatus) {

		_isRunning = runStatus;

	}

	/**
	 * Sets the sim-process' slaveWaitQueue variable to the ProcessQueue in
	 * which this SimProcess is waiting as a slave to cooperate with a master.
	 * 
	 * @param slvWaitQueue
	 *            ProcessQueue : The waiting-queue in which this SimProcess is
	 *            waiting as a slave to cooperate with a master.
	 * @author Soenke Claassen
	 */
	public void setSlaveWaitQueue(
			ProcessQueue<? extends SimProcess> slvWaitQueue) {
		_slaveWaitQueue = slvWaitQueue;
	}

	/**
	 * Sets the supervising <code>ComplexSimProcess</code> this SimProcess is
	 * contained in. Setting it to <code>null</code> indicates that this
	 * Sim-process is not contained in any <code>ComplexSimProcess</code>
	 * (anymore).
	 * 
	 * @param complexProcess
	 *            desmoj.ComplexSimProcess : The <code>ComplexSimProcess</code>
	 *            which serves as a container for this SimProcess.
	 * @author Soenke Claassen
	 */
	protected void setSupervisor(ComplexSimProcess complexProcess) {

		_supervisor = complexProcess;
	}

	/**
	 * Sets the attribute indicating that this SimProcess' simthread has
	 * finished to the given value. This method is used by class
	 * <code>SimThread<code> only.
	 * 
	 * @param termValue
	 *            boolean : The new value for the attribute indicating the
	 *            SimThread's end
	 */
	void setTerminated(boolean termValue) {

		_isTerminated = termValue; // Hasta la vista, baby!

	}
	
    /**
     * Returns the process' scheduling priority. The scheduling priority is used
     * to determine which process to execute first if two or more processes are activated
     * at the same instant. The default priority is zero.
     * Higher priorities are positive, lower priorities negative.
     * 
     * @return int : The process' priority
     */
    public int getSchedulingPriority() {

        return _mySchedulingPriority;

    }

    /**
     * Sets the process' scheduling priority to a given integer value. The default 
     * priority (unless assigned otherwise) is zero. 
     * Negative priorities are lower, positive priorities are higher.
     * All values should be inside the range defined by Java's integral
     * <code>integer</code> data type [-2147483648, +2147483647].
     * 
     * An process' scheduling priority it used to determine which process is 
     * executed first if activated at the same time instant.
     * Should the priority be the same, order of event execution depends on the 
     * <code>EventList</code> in use, e.g. activated first is executed 
     * first (<code>EventTreeList</code>) or random (<code>RandomizingEventTreeList</code>). 
     *
     * @param newPriority
     *            int : The new scheduling priority value
     */
    public void setSchedulingPriority(int newPriority) {

        this._mySchedulingPriority = newPriority;

    }
    
	/**
	 * Starts the simthread associated with this SimProcess. This is method must
	 * be called the first time a sim-process is supposed to start processing
	 * its <code>lifeCycle()</code> method.
	 */
	synchronized void start() {
	    
        // set up simthread
        _myThread = new SimThread(getModel().getExperiment().getThreadGroup(), this);

		// setting this flag shows that the simthread is now ready to take over
		// control from the scheduler's thread
		_isRunning = true;

		// start thread and let it run into the block
		_myThread.start();

		// put thread in to a wait for synchronization
		try {
			wait();
		} catch (InterruptedException irqEx) {
			// create eror message
			ErrorMessage errmsg = new ErrorMessage(getModel(),
					"Simulation stopped!", "Exception thrown by Java VM"
							+ irqEx, "Thread conflict assumed.",
					"Check Java VM.", presentTime());
			// throw it back to Experiment's start routine
			throw (new desmoj.core.exception.DESMOJException(errmsg));
		}

		// check if simulation has been stopped in between and throw SimFinished
		if (getModel().getExperiment().isAborted()) {
			throw (new desmoj.core.exception.SimFinishedException(getModel(),
					getName(), presentTime()));
		}
	}
} // end class SimProcess