package desmoj.core.simulator;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import desmoj.core.dist.DistributionManager;
import desmoj.core.exception.DESMOJException;
import desmoj.core.report.DebugNote;
import desmoj.core.report.ErrorMessage;
import desmoj.core.report.FileOutput;
import desmoj.core.report.Message;
import desmoj.core.report.MessageDistributor;
import desmoj.core.report.MessageReceiver;
import desmoj.core.report.OutputType;
import desmoj.core.report.OutputTypeEndToExport;
import desmoj.core.report.Reporter;
import desmoj.core.report.SimulationRunReporter;
import desmoj.core.report.TraceNote;

/**
 * Experiment is the class that provides the infrastructure for running the
 * simulation of a model. It contains all data structures necessary to simulate
 * the model and takes care of all necessary output. To actually run an
 * experiment, a new instance of the experiment class and a new instance of the
 * desired model have to be created. To link both instances, call the
 * <code>connectToExperiment(Experiment e)</code> method of the model instance
 * and pass the new experiment as a parameter.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Tim Lechler
 * @author modified by Soenke Claassen, Ruth Meyer, Nicolas Knaak, Gunnar
 *         Kiesel,Felix Klueckmann
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
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @modifier new Example Variable _traceOutput, _reportOutput for Outputclasscollection
 * 			 new class constructors 
 * 			 Experiment(String , String , ArrayList<String> , ArrayList<String> ,ArrayList<String> , ArrayList<String> ) and
 * 			 Experiment(String , String , TimeUnit ,TimeUnit , TimeFormatter ,ArrayList<String> , 
 * 					ArrayList<String> ,ArrayList<String> ,ArrayList<String> )
 * @author Xiufeng Li
 */
public class Experiment extends NamedObject {

	/**
	 * The experiment's name catalog for ensuring uniqueness of simulation
	 * object names within a single experiment.
	 */
	private NameCatalog _nameCatalog = new NameCatalog();
	
	/** 
	 * The default report output 
	 */
	public static final String DEFAULT_REPORT_OUTPUT_TYPE = "desmoj.core.report.HTMLReportOutput";

	/** 
	 * The default trace output 
	 */
	public static final String DEFAULT_TRACE_OUTPUT_TYPE = "desmoj.core.report.HTMLTraceOutput";

	/** 
	 * The default error output 
	 */
	public static final String DEFAULT_ERROR_OUTPUT_TYPE = "desmoj.core.report.HTMLErrorOutput";

	/** 
	 * The default debug output 
	 */
	public static final String DEFAULT_DEBUG_OUTPUT_TYPE = "desmoj.core.report.HTMLDebugOutput";

	/**
	 * Status of an Experiment just created without any accessories created yet.
	 */
	public static final int NOT_INITIALIZED = -3;

	/**
	 * Status of an Experiment instantiated with all needed accessories
	 * available.
	 */
	public static final int INITIALIZED = -2;

	/**
	 * Status of an Experiment connected to a Model and ready to be started.
	 */
	public static final int CONNECTED = -1;

	/**
	 * Status of an Experiment being started. Only if an Experiment is
	 */
	public static final int STARTED = 0;

	/**
	 * Status of an Experiment stopped after having run.
	 */
	public static final int STOPPED = 1;

	/**
	 * Status of an Experiment currently running the simulation.
	 */
	public static final int RUNNING = 2;

	/**
	 * Status of an Experiment finished and to be cleared.
	 */
	public static final int ABORTED = 3;

	/**
	 * The last suffix used with filenames when creating multiple batch runs of
	 * an experiment.
	 */
	private static int lastSuffix;

	/**
	 * The class reference to messages of type desmoj.core.report.TraceNote
	 */
	static Class<desmoj.core.report.TraceNote> tracenote;

	/**
	 * The class reference to messages of type desmoj.core.report.DebugNote
	 */
	static Class<desmoj.core.report.DebugNote> debugnote;

	/**
	 * The class reference to messages of type desmoj.core.report.ErrorMessage
	 */
	static Class<desmoj.core.report.ErrorMessage> errormessage;

	/**
	 * The class reference to messages of type desmoj.core.report.Reporter
	 */
	static Class<desmoj.core.report.Reporter> reporter;
	
    /**
     * Flag indicating at least one error or warning has occurred.
     */
    private boolean _error;

	/**
     * Flag indicating to suppress notifications like 'experiment started'.
     */
    private boolean _silent;
    
    /**
     * The description of this Experiment
     */
    private String _description = null;
	
	/**
	 * Flag indicating if the simulation is running.
	 */
	private int _status;

	/**
	 * The model to be run by this experiment.
	 */
	private Model _client;

	/**
	 * The scheduler used for this experiment.
	 */
	protected Scheduler clientScheduler;

	/**
	 * The distribution manager for the model's distributions.
	 */
	private DistributionManager _distMan;

	/**
	 * The message manager for the model's messages.
	 */
	private MessageDistributor _messMan;

	/**
	 * The ThreadGroup for this Experiment.
	 */
	private ThreadGroup _expThreads;

	/**
	 * The list to register all OutputType objects to close them after finishing
	 * the Experiment.
	 */
	private java.util.ArrayList<OutputType> _registryOutputType;

	/**
	 * The list to register all FileOutput objects to close them after finishing
	 * the Experiment.
	 */
	private java.util.ArrayList<FileOutput> _registryFileOutput;

	/**
	 * The resource database storing all resource allocations and requests. Also
	 * needed to detect deadlocks.
	 */
	private ResourceDB _resDB;

	/**
	 * The TimeInstant when the experiment is supposed to stop. Is initially
	 * <code>null</code> and will be set only if the user provides a time limit.
	 */
	private TimeInstant _stopTime = null;
	
   /**
     * The event to stop the experiment. Is initially <code>null</code> and will 
     * be set only if the user provides a time limit.
     */
    private ExternalEventStop _stopTimeEvent = null;
	
    /**
     * A list of <code>Condition</code>s which cause the experiment to stop. The
     * user has to implement the <code>check()</code> method of the 
     * <code>Condition</code>s in order to effectively stop an experiment.
     */
    private List<ModelCondition> _stopConditions;

	/**
	 * Flag indicating whether a progressbar for this experiment should be
	 * displayed or not.
	 */
	private boolean _showProgressBar;

	/**
	 * Specifies an output path for the report files (Modification by Nicolas
	 * Knaak, 02/2001)
	 */
	private String _pathName;

	/**
	 * Delay between steps of the scheduler in milliseconds. Necessary for
	 * online observation of experiments in FAMOS. (Modification by Nicolas
	 * Knaak, 07/2001)
	 */
	private long _delayInMillis = 0;

	/**
	 * The real time (wallclock time) start time of the simulation run.
	 * (Modification by Felix Klueckmann, 05/2009)
	 */
	private long _realTimeStartTime;

	/**
	 * The output types of the debug channel.
     */
	private ArrayList<OutputType> _debugOutput;
	
    /**
     * The output types of the report channel.
     */
	private ArrayList<OutputType> _reportOutput;
	
    /**
     * The output types of the error channel.
     */
	private ArrayList<OutputType> _errorOutput;

    /**
     * The output types of the trance channel.
     */
	private ArrayList<OutputType> _traceOutput;
	
	/**
     * Constructs a new Experiment with a given name.  
     * Data channel output (report, error, debug, trace) will either be
     * written to HTML files in the current directory. 
     * Epsilon (granularity of simulation) defaults to a microsecond,
     * reference time (default time unit) to a second. 
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 */
	public Experiment(String name) {
		this(name, true);
	}

	/**
	 * Constructs a new Experiment with the given parameters. Experiment name can be specified. 
	 * Data channel output (report, error, debug, trace) will either be
     * suppressed or written to HTML files in the current directory. 
     * Epsilon (granularity of simulation) defaults to a microsecond,
     * reference time (default time unit) to a second. 
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 * @param output
	 *            boolean : This flag indicates if the experiment should write
	 *            output files in the default format (HTML) or no output files
	 *            at all.
	 */
	public Experiment(String name, boolean output) {
		this(name, ".", TimeUnit.MICROSECONDS, TimeUnit.SECONDS, null,
				output ? DEFAULT_REPORT_OUTPUT_TYPE : null,
				output ? DEFAULT_TRACE_OUTPUT_TYPE : null,
				output ? DEFAULT_ERROR_OUTPUT_TYPE : null,
				output ? DEFAULT_DEBUG_OUTPUT_TYPE : null); // call most special
		// constructor
	}

	/**
	 * Constructs a new Experiment with the given parameters. Experiment name and output path
	 * can be specified. Data channel output (report, error, debug, trace) will either be
     * written to HTML files, epsilon (granularity of simulation) defaults to a microsecond,
     * reference time (default time unit) to a second. 
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 * @param pathName
	 *            java.lang.String : The output path for report files
	 */
	public Experiment(String name, String pathName) {

		this(name, pathName, DEFAULT_REPORT_OUTPUT_TYPE,
				DEFAULT_TRACE_OUTPUT_TYPE, DEFAULT_ERROR_OUTPUT_TYPE,
				DEFAULT_DEBUG_OUTPUT_TYPE);
		// call more special constructor

	}

	/**
	 * Constructs a new Experiment with the given parameters. This is a shortcut
	 * constructor. Parameters for the name, the granularity(epsilon), the
	 * reference time unit and a time formatter are needed. All other possible
	 * settings are set to default values. These settings for an experiment
	 * without special ExperimentOptions are:
	 * <ol>
	 * <li>seed = 979 : The initial seed setting for the seed-generator</li>
	 * </ol>
	 * The default stop condition for this experiment will never interfere,
	 * always returning false.
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 * @param epsilon
	 *            java.util.concurrent.TimeUnit: The granularity of simulation
	 *            time.
	 * @param referenceUnit
	 *            java.util.concurrent.TimeUnit : In statements without an
	 *            explicit declaration of a TimeUnit the reference unit is used.
	 * @param formatter
	 *            desmoj.core.simulator.TimeFormatter: Defines how time values
	 *            will be formatted in the output files.
	 * 
	 * @see java.util.concurrent.TimeUnit
	 */
	public Experiment(String name, TimeUnit epsilon, TimeUnit referenceUnit,
			TimeFormatter formatter) {
		// call most special constructor
		this(name, ".", epsilon, referenceUnit, formatter,
				DEFAULT_REPORT_OUTPUT_TYPE, DEFAULT_TRACE_OUTPUT_TYPE,
				DEFAULT_ERROR_OUTPUT_TYPE, DEFAULT_DEBUG_OUTPUT_TYPE);

	}
	
    /**
     * Constructs a new Experiment with the given parameters. Experiment name, output path 
     * and a single file type per output channel can be specified.
     * Epsilon (granularity of simulation) defaults to a microsecond,
     * reference time (default time unit) to a second. 
     * 
     * @param name
     *            String : The name of the experiment determining the
     *            outputfile's names, too. So please avoid characters that your
     *            local filesystem does not support in filenames.
     * @param pathName
     *            java.lang.String : The output path for report files
     * @see desmoj.core.simulator.Units
     * @param reportOutputType
     * @param traceOutputType
     * @param errorOutputType
     * @param debugOutputType
     */
    public Experiment(String name, String pathName, String reportOutputType,
            String traceOutputType, String errorOutputType,
            String debugOutputType) {
        this(name, pathName, TimeUnit.MICROSECONDS, TimeUnit.SECONDS, null,
                reportOutputType, traceOutputType, errorOutputType,
                debugOutputType);
    }

	/**
     * Constructs a new Experiment with the given parameters. Experiment name, output path, epsilon, reference time unit, time format, 
     * can be specified. Same holds for file output channels, though this constructor assumes a single file type per output channel.  
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 * @param pathName
	 *            java.lang.String : The output path for report files
	 * @param epsilon
	 *            java.util.concurrent.TimeUnit: The granularity of simulation
	 *            time.
	 * @param referenceUnit
	 *            java.util.concurrent.TimeUnit : In statements without an
	 *            explicit declaration of a TimeUnit the reference unit is used.
	 * @param formatter
	 *            desmoj.core.simulator.TimeFormatter: Defines how time values
	 *            will be formatted in the output files.
	 * 
	 * @see java.util.concurrent.TimeUnit
	 */
	public Experiment(String name, String pathName, TimeUnit epsilon,
			TimeUnit referenceUnit, TimeFormatter formatter,
			String reportOutputType, String traceOutputType,
			String errorOutputType, String debugOutputType) {
		super(name); // create a NamedObject with an attitude ;-)
		
		ArrayList<String> reportOutputs = new ArrayList<String>(); if (reportOutputType!=null) reportOutputs.add(reportOutputType);
		ArrayList<String> traceOutputs = new ArrayList<String>();  if (traceOutputType!=null)  traceOutputs.add(traceOutputType);
		ArrayList<String> errorOutputs = new ArrayList<String>();  if (errorOutputType!=null)  errorOutputs.add(errorOutputType);
		ArrayList<String> debugOutputs = new ArrayList<String>();  if (debugOutputType!=null)  debugOutputs.add(debugOutputType);
		
		setupExperiment(name, pathName, epsilon, referenceUnit, formatter, reportOutputs, traceOutputs, errorOutputs, debugOutputs);
	}
	
	/**
	 * Constructs a new Experiment with the given parameters. This is the most
	 * flexible constructor. Experiment name, output path, epsilon, reference time unit, time format, 
	 * and multiple file types per output channel can be specified.   
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 * @param outputPath
	 *            java.lang.String : The output path for report files
	 * @param epsilon
	 *            java.util.concurrent.TimeUnit: The granularity of simulation
	 *            time.
	 * @param referenceUnit
	 *            java.util.concurrent.TimeUnit : In statements without an
	 *            explicit declaration of a TimeUnit the reference unit is used.
	 * @param formatter
	 *            desmoj.core.simulator.TimeFormatter: Defines how time values
	 *            will be formatted in the output files.
	 * 
	 * @see java.util.concurrent.TimeUnit
	 */
	public Experiment(String name, String outputPath, TimeUnit epsilon,
			TimeUnit referenceUnit, TimeFormatter formatter,
			ArrayList<String> reportOutputs, ArrayList<String> traceOutputs,
			ArrayList<String> errorOutputs, ArrayList<String> debugOutputs)
	{
		super(name);
		setupExperiment(name, outputPath, epsilon, referenceUnit, formatter, reportOutputs, traceOutputs, errorOutputs, debugOutputs);
	}
	/**
     * Constructs a new Experiment with the given parameters. Experiment name, output path 
     * and a multiple file type per output channel can be specified.
     * Epsilon (granularity of simulation) defaults to a microsecond,
     * reference time (default time unit) to a second. 
	 * 
	 * @param name
	 *            String : The name of the experiment determining the
	 *            outputfile's names, too. So please avoid characters that your
	 *            local filesystem does not support in filenames.
	 * @param outputPath
	 *            java.lang.String : The output path for report files
	 * @see desmoj.core.simulator.Units
	 */
	public Experiment(String name, String outputPath,
			ArrayList<String> reportOutputs, ArrayList<String> traceOutputs,
			ArrayList<String> errorOutputs, ArrayList<String> debugOutputs) {
        this(name, outputPath, TimeUnit.MICROSECONDS, TimeUnit.SECONDS, null,
                reportOutputs, traceOutputs, errorOutputs,
                debugOutputs);
	}
	
	
	/**
     * Private helper method to initialize the experiment; should be called
     * from all constructors.
     * 
     * @param name
     *            String : The name of the experiment determining the
     *            outputfile's names, too. So please avoid characters that your
     *            local filesystem does not support in filenames.
     * @param outputPath
     *            java.lang.String : The output path for report files
     * @param epsilon
     *            java.util.concurrent.TimeUnit: The granularity of simulation
     *            time.
     * @param referenceUnit
     *            java.util.concurrent.TimeUnit : In statements without an
     *            explicit declaration of a TimeUnit the reference unit is used.
     * @param formatter
     *            desmoj.core.simulator.TimeFormatter: Defines how time values
     *            will be formatted in the output files.
     * 
     * @see java.util.concurrent.TimeUnit
     */
    private void setupExperiment(String name, String outputPath, TimeUnit epsilon,
            TimeUnit referenceUnit, TimeFormatter formatter,
            ArrayList<String> reportOutputs, ArrayList<String> traceOutputs,
            ArrayList<String> errorOutputs, ArrayList<String> debugOutputs)
    {
        // initialize variables
        _traceOutput = new ArrayList<OutputType>();
        _debugOutput = new ArrayList<OutputType>();
        _errorOutput = new ArrayList<OutputType>();
        _reportOutput = new ArrayList<OutputType>();
        _status = NOT_INITIALIZED;
        _stopConditions = new ArrayList<ModelCondition>(); // empty, i.e. no Stopper
                                                        // can be set at
                                                        // instantiation time
        _expThreads = new ThreadGroup(name);
        _registryFileOutput = new ArrayList<FileOutput>();
        _registryOutputType = new ArrayList<OutputType>();
        lastSuffix = 0; // no batches have run so far ;-)
        _showProgressBar = true; // display a progress bar for this experiment
        _error = false; // no error or warning so far ;-)
        _silent = false; // notify the user about what is going on

        // Check and set output path
//      if (pathName == null
//              || (pathName != null && (pathName.isEmpty() || pathName
//                      .equals("."))))
//          this.pathName = System.getProperty("user.dir", ".");
//      else
            this._pathName = outputPath;

        // set class variables for basic messagetypes
        try
        {
            tracenote = (Class<TraceNote>) Class
                    .forName("desmoj.core.report.TraceNote");
            debugnote = (Class<DebugNote>) Class
                    .forName("desmoj.core.report.DebugNote");
            errormessage = (Class<ErrorMessage>) Class
                    .forName("desmoj.core.report.ErrorMessage");
            reporter = (Class<Reporter>) Class
                    .forName("desmoj.core.report.Reporter");
        } catch (ClassNotFoundException cnfEx)
        {
            System.err.println("Can not create Experiment!");
            System.err.println("Constructor of desmoj.core.Experiment.");
            System.err.println("Classes are probably not installed correctly.");
            System.err.println("Check your CLASSPATH setting.");
            System.err.println("Exception caught : " + cnfEx);
        }

        // create output system first
        _messMan = new MessageDistributor();

        // create and register the debug output
        for (String debugOutputType : debugOutputs)
        {
            try
            {
                Class<OutputType> debugOType = (Class<OutputType>) Class
                        .forName((debugOutputType != null) ? debugOutputType
                                : DEFAULT_DEBUG_OUTPUT_TYPE);
                OutputType dbg = debugOType.newInstance();
                _debugOutput.add(dbg);
                if (debugOutputType != null)
                    dbg.open(_pathName, name);
                _messMan.register(dbg, debugnote);
                _messMan.switchOff(debugnote);
                register(dbg);
            } catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }

        // create and register the report output
        for (String reportOutputType : reportOutputs)
        {
            try
            {
                Class<OutputType> reportOType = (Class<OutputType>) Class
                        .forName((reportOutputType != null) ? reportOutputType
                                : DEFAULT_REPORT_OUTPUT_TYPE);
                OutputType rpt = reportOType.newInstance();
                _reportOutput.add(rpt);
                if (reportOutputType != null)
                    rpt.open(_pathName, name);
                _messMan.register(rpt, reporter);
                register(rpt);
            } catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }

        // create and register the error output
        for (String errorOutputType : errorOutputs)
        {
            try
            {
                Class<OutputType> errorOType = (Class<OutputType>) Class
                        .forName((errorOutputType != null) ? errorOutputType
                                : DEFAULT_ERROR_OUTPUT_TYPE);
                OutputType err = errorOType.newInstance();
                _errorOutput.add(err);
                // err.setTimeFloats(timeFloats);
                if (errorOutputType != null)
                    err.open(_pathName, name);
                _messMan.register(err, errormessage);
                register(err);
            } catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }
        // create and register the trace output
        for (String traceOutputType : traceOutputs)
        {
            try
            {

                Class<OutputType> traceOType = (Class<OutputType>) Class
                        .forName((traceOutputType != null) ? traceOutputType
                                : DEFAULT_TRACE_OUTPUT_TYPE);
                OutputType trc = traceOType.newInstance();
                _traceOutput.add(trc);
                if (traceOutputType != null)
                    trc.open(_pathName, name);
                _messMan.register(trc, tracenote);
                _messMan.switchOff(tracenote);
                register(trc);
            } catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }

        // create the distributionmanager to register distributions at
        _distMan = new DistributionManager(name, 979);

        // now create the simulation runtime accessories
        _client = null; // no object connected

        // check for null reference
        if (epsilon == null)
        {
            // set to default unit
            epsilon = TimeOperations.getEpsilon();
        }
        if (referenceUnit == null)
        {
            // set to default unit
            referenceUnit = TimeOperations.getReferenceUnit();
        }
        
        // swap epsilon and reference unit if the reference unit has a
        // finer granularity than epsilon
        if (referenceUnit.compareTo(epsilon) < 0)
        {
            TimeUnit buffer = referenceUnit;
            referenceUnit = epsilon;
            epsilon = buffer;
        }
        // set epsilon and referenceUnit
        TimeOperations.setEpsilon(epsilon);
        TimeOperations.setReferenceUnit(referenceUnit);

        // set time formatter (use default if null passed)
        TimeOperations.setTimeFormatter(formatter != null ? 
                formatter : 
                TimeOperations.getDefaultTimeFormatter());

        // building the scheduler: prepare event list...
        // (for efficiency reasons, we use the TreeList-based implementation)
        EventList eventList = new EventTreeList();

        // create the scheduler (and clock)
        clientScheduler = createScheduler(name, eventList);

        // create a resource database and tell it that it belongs to this
        // experiment
        _resDB = new ResourceDB(this);

        // set status to first valid value - initialized, but not connected
        _status = INITIALIZED;
    }
	
	/**
	 * Creates a scheduler for this experiment.
	 * 
	 * @param name
	 *            experiment name
	 * @return a new scheduler
	 */
	protected Scheduler createScheduler(String name, EventList evl) {
		Scheduler s = new Scheduler(this, name, evl);
		return s;
	}

	/**
	 * Adds a messagereceiver for debugnotes to the experiment. Whenever a model
	 * produces a message of that type, it will also be sent to the given
	 * messagereceiver for further processing. Note that the given receiver must
	 * be capable of handling debugnotes.
	 * 
	 * @param trcRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver for
	 *            the given type of messages
	 */
	public void addDebugReceiver(MessageReceiver trcRec) {

		if (trcRec == null) {
			sendWarning("Can not add receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', method 'void addDebugReceiver("
							+ "MessageReceiver trcRec)'",
					"The parameter 'trc' passed was a null reference.",
					"Make sure to construct a valid MessageReciever before adding it to "
							+ "the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.register(trcRec, debugnote);

	}

	/**
	 * Adds a messagereceiver for error messages to the experiment. Whenever a
	 * model produces a message of that type, it will also be sent to the given
	 * messagereceiver for further processing. Note that the given receiver must
	 * be capable of handling messagereceiver.
	 * 
	 * @param trcRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver for
	 *            the given type of messages
	 */
	public void addErrorReceiver(MessageReceiver trcRec) {

		if (trcRec == null) {
			sendWarning("Can not add receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', method 'void addErrorReceiver("
							+ "MessageReceiver trcRec)'",
					"The parameter 'trc' passed was a null reference.",
					"Make sure to construct a valid MessageReciever before adding it to "
							+ "the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.register(trcRec, errormessage);

	}

	/**
	 * Returns the experiments name catalog for ensuring unique names of
	 * simulation objects within a single experiment.
	 */
	NameCatalog getNameCatalog() {
		return _nameCatalog;
	}

	/**
	 * Adds a messagereceiver for the given subtype of message to the
	 * experiment. Whenever a model produces a message of that type, it will
	 * also be sent to the given messagereceiver for further processing.
	 * 
	 * @param trcRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver for
	 *            the given type of messages
	 * @param messageType
	 *            Class : The type of message to be sent to the given
	 *            messagereceiver
	 */
	public void addReceiver(MessageReceiver trcRec, Class<?> messageType) {

		if (trcRec == null) {
			sendWarning("Can not add receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', method 'void addReceiver(MessageReceiver "
							+ "trcRec, Class messageType)'",
					"The parameter 'trc' passed was a null reference.",
					"Make sure to construct a valid MessageReciever before adding it to "
							+ "the experiment's messaging system.");
			return; // do nothing
		}

		if (messageType == null) { // again these damned null values
			sendWarning("Can not add receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', method 'void addReceiver(MessageReceiver "
							+ "trcRec, Class messageType)'",
					"The parameter 'messageType' passed was a null reference.",
					"Make sure to construct a valid Class object before adding it to "
							+ "the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.register(trcRec, messageType);

	}

	/**
	 * Adds a messagereceiver for tracenotes to the experiment. Whenever a model
	 * produces a message of that type, it will also be sent to the given
	 * messagereceiver for further processing. Note that the given Receiver must
	 * be capable of handling tracenotes.
	 * 
	 * @param trcRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver for
	 *            the given type of messages
	 */
	public void addTraceReceiver(MessageReceiver trcRec) {

		if (trcRec == null) {
			sendWarning("Can not add receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', method 'void addTraceReceiver("
							+ "MessageReceiver trcRec)'",
					"The parameter 'trc' passed was a null reference.",
					"Make sure to construct a valid MessageReciever before adding it to "
							+ "the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.register(trcRec, tracenote);

	}

	/**
	 * Returns a boolean indicating whether debug notes are forwarded to the
	 * debug ouput or not. Debug ouput can be switched on and off using the
	 * methods <code>debugOn(TimeInstant startTime)</code> or
	 * <code>debugOff(TimeInstant stopTime)</code>
	 * 
	 * @return boolean
	 */
	public boolean debugIsOn() {

		return _messMan.isOn(debugnote);

	}

	/**
	 * Switches the debug output off at the given point of simulation time.
	 * 
	 * @param stopTime
	 *            TimeInstant : The point in simulation time to switch off debug
	 */
	public void debugOff(TimeInstant stopTime) {

		// check initial TimeInstant parameter
		if (stopTime == null) {
			sendWarning(
					"Invalid start time parameter for debug output given! "
							+ "StopTime is set to current time.",
					"Experiment '" + getName()
							+ "', method 'void debugOn(TimeInstant startTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object, otherwise use method "
							+ "start() without TimeInstant parameter.");
			stopTime = clientScheduler.presentTime();
		}

		// check if parameter is in future
		if (TimeInstant.isAfter(clientScheduler.presentTime(), stopTime)) {
			sendWarning("Invalid start time parameter for debug output given! "
					+ "StopTime is set to current time.", "Experiment '"
					+ getName()
					+ "', method 'void debugOn(TimeInstant stopTime)'",
					"The stopTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			stopTime = clientScheduler.presentTime();
		}

		ExternalEvent debugOff = new ExternalEventDebugOff(_client, true);

		debugOff.schedule(stopTime);

	}	
	   
    /**
     * @deprecated Use debugOff(TimeInstant startTime). Switches the debug output off at the given point of simulation time.
     * 
     * @param stopTime
     *            SimTime : The point in simulation time to switch debug off
     */
    public void debugOff(SimTime stopTime) {
        this.debugOff(SimTime.toTimeInstant(stopTime));        
    }

	/**
	 * Switches the debug output on at the given point of simulation time.
	 * 
	 * @param startTime
	 *            TimeInstant : The point in simulation time to switch on debug
	 */
	public void debugOn(TimeInstant startTime) {

		// check initial TimeInstant parameter
		if (startTime == null) {
			sendWarning(
					"Invalid start time parameter for debug output given! "
							+ "StartTime is set to current time.",
					"Experiment '" + getName()
							+ "', method 'void debugOn(TimeInstant startTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object, otherwise use method "
							+ "start() without TimeInstant parameter.");
			startTime = clientScheduler.presentTime();
		}

		// check if parameter is in future
		if (TimeInstant.isAfter(clientScheduler.presentTime(), startTime)) {
			sendWarning("Invalid start time parameter for debug output given! "
					+ "StartTime is set to current time.", "Experiment '"
					+ getName()
					+ "', method 'void debugOn(TimeInstant startTime)'",
					"The startTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			startTime = clientScheduler.presentTime();
		}

        // if parameter equals current time, set trace on immediately, e.g. 
        // to include initial scheduling
        if (TimeInstant.isEqual(clientScheduler.presentTime(), startTime)) {
            this.getMessageManager().switchOn(Experiment.debugnote);
            _client.sendTraceNote("Debug switched on");
        // Otherwise schedule an appropriate event    
        } else {
            ExternalEvent debugOn = new ExternalEventDebugOn(_client, true);
            debugOn.schedule(startTime);
        }

	}
    
    /**
     * @deprecated Use debugOn(TimeInstant startTime). Switches the debug output on at the given point of simulation time.
     * 
     * @param startTime
     *            SimTime : The point in simulation time to switch debug on
     */
    public void debugOn(SimTime startTime) {
        this.debugOn(SimTime.toTimeInstant(startTime));        
    }

	/**
	 * Switches the debug output on for the given period of simulation time. If
	 * the second parameter (off) is "sooner" then the first parameter (on),
	 * they will be swapped automatically. Same parameters will result in no
	 * debug output at all!
	 * 
	 * @param startTime
	 *            TimeInstant : The point in simulation time to switch debug on
	 * @param stopTime
	 *            TimeInstant : The point in simulation time to switch debug off
	 */
	public void debugPeriod(TimeInstant startTime, TimeInstant stopTime) {
		// check initial TimeInstant parameter
		if (startTime == null) {
			sendWarning(
					"Invalid start time parameter for debug output given! Command ignored",
					"Experiment '" + getName()
							+ "', Method 'debugPeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object.");
			return;
		}

		// check initial TimeInstant parameter
		if (stopTime == null) {
			sendWarning(
					"Invalid stop time parameter for debug output given! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'debugPeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object.");
			return;
		}

		// check for correct order in parameters
		if (TimeInstant.isAfter(startTime, stopTime)) {

			// swap parameters
			TimeInstant buffer = stopTime;
			stopTime = startTime;
			startTime = buffer;

		}

		// check if stop parameter is in future
		if (TimeInstant.isAfter(clientScheduler.presentTime(), stopTime)) {
			sendWarning(
					"Invalid stop time parameter for debug output given! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'debugPeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"The stopTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			return;
		}

		// check if start parameter is in past
		if (TimeInstant.isAfter(clientScheduler.presentTime(), startTime)) {
			sendWarning("Invalid start time parameter for debug output given! "
					+ "Debug output has been set to start immediately.",
					"Experiment '" + getName()
							+ "', Method 'debugPeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"The startTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			startTime = clientScheduler.presentTime();
		}

		// set debug to switch on
		debugOn(startTime);

		// set debug to switch off
		debugOff(stopTime);
	}

	/**
	 * @deprecated Replaced by debugTime(TimeInstant a, TimeInstant b). Switches
	 *             the debug output on for the given period of simulation time.
	 *             If the second parameter (off) is "sooner" then the first
	 *             parameter (on), they will be swapped automatically. Same
	 *             parameters will result in no debug output at all!
	 * 
	 * @param startTime
	 *            SimTime : The point in simulation time to switch debug on
	 * @param stopTime
	 *            SimTime : The point in simulation time to switch debug off
	 */
	@Deprecated
	public void debugPeriod(SimTime startTime, SimTime stopTime) {
		debugPeriod(SimTime.toTimeInstant(startTime), SimTime
				.toTimeInstant(stopTime));
	}

	/**
	 * De-registers a file at the experiment. Registered files will be flushed
	 * and closed after the experiment has finished. If the file is manually
	 * closed by the user and has been registered at the Experiment, deRegister
	 * it
	 * 
	 * @param file
	 *            desmoj.report.FileOutput : The file to be closed with the end
	 *            of an Experiment
	 */
	public void deRegister(FileOutput file) {

		if (file == null) {
			sendWarning("Can not de-register FileOutput! Command ignored.",
					"Experiment '" + getName()
							+ "' method 'void deRegister(FileOutput file).'",
					"The parameter given was a null reference.",
					"Make sure to only connect valid FileOutputs at the Experiment.");
			return;
		}

		_registryFileOutput.remove(file);
		// remove whether it was inside or not

	}

	/**
	 * Stopps all running simprocesses that might still be scheduled and closes
	 * the output files.
	 */
	public void finish() {

		// check if experiment has not been aborted before
		if (_status >= ABORTED)	{
			return;
		}

		if (_traceOutput != null)		{
			for (OutputType trc : _traceOutput)			
			{
				if (trc instanceof OutputTypeEndToExport)				{
					((OutputTypeEndToExport) trc).export(_pathName, getName());
				}
			}
		}
		if (_debugOutput != null) {
			for (OutputType dbg : _debugOutput)	{
				if (dbg instanceof OutputTypeEndToExport) {
					((OutputTypeEndToExport) dbg).export(_pathName, getName());
				}
			}
		}
		if (_errorOutput != null) {
			for (OutputType err : _errorOutput) {
				if (err instanceof OutputTypeEndToExport) {
					((OutputTypeEndToExport) err).export(_pathName, getName());
				}
			}
		}
		if (_reportOutput != null){
			for (OutputType rpt : _reportOutput) {
				if (rpt instanceof OutputTypeEndToExport){
					((OutputTypeEndToExport) rpt).export(_pathName, getName());
				}
			}
		}

		// set status to let all simthreads be killed
		_status = ABORTED;

		// close all files still open
		for (OutputType o : _registryOutputType)
			o.close();
		for (FileOutput f : _registryFileOutput)
			f.close();

		// kill all SimThreads still active
		Thread[] survivors = new Thread[_expThreads.activeCount()];
		_expThreads.enumerate(survivors);

		for (int i = 0; i < survivors.length; i++)		{

			// print existing threads for controlling purposes only
			// System.out.println(survivors[i]);

			// if we get the enumeration of survivors, some of them
			// might not have made it until here and die in between
			// so an occasional NullPointerException is perfectly
			// alright and no reason to worry -> we just dump it.
			if (survivors[i] instanceof SimThread)			{
				try				{
					((SimThread) survivors[i]).kill();
				} catch (NullPointerException e)				{
					; // forget it anyway...
				}
			}
		}

	}

	/**
	 * Returns the distributionmanager for this experiment. Distributions need
	 * access to the distributionmanager for handling antithetic modes,
	 * resetting and their initial seeds.
	 * 
	 * @return desmoj.dist.DistributionManager : The distributionmanager for
	 *         this experiment
	 */
	public DistributionManager getDistributionManager() {

		return _distMan;

	}

	/**
	 * Returns the epsilon value representing the granularity 
	 * of simulation time for this experiment. So far, Hour, Minute,
	 * Second and Millisecond are supported.
	 * Default (unless set explicitly) is TimeUnit.MICROSECONDS.
	 * 
	 * @return TimeUnit : The Granularity of the simulation time
	 */	
	public TimeUnit getEpsilonUnit() {

		return TimeOperations.getEpsilon();
	}
	
    /**
     * @deprecated Use getEpsilonUnit(). 
     * Returns a SimTime representation of the granularity of simulation time 
     * for this experiment. So far, Hour, Minute,
     * Second and Millisecond are supported.
     * 
     * @return SimTime : The Granularity of the simulation time
     */ 
    public SimTime getEpsilon() {

        return SimTime.toSimTime(new TimeSpan (1L, TimeOperations.getEpsilon()));
    }

	/**
	 * Returns the current execution Speed Rate.
	 * 
	 * @return double : The current execution speed rate.
	 */
	public double getExecutionSpeedRate() {
		return this.clientScheduler.getExecutionSpeedRate();
	}

	/**
	 * Returns the messagemanager for this experiment. Messages need access to
	 * the MessageManager for distributing the messages to one or more specified
	 * output streams.
	 * 
	 * @return desmoj.dist.MessageManager : The messagemanager for this
	 *         experiment
	 */
	public MessageDistributor getMessageManager() {

		return _messMan;

	}
	
	/**
	 * Returns the model that is connected to this experiment or
	 * <code>null</code> if no model is connected so far.
	 * 
	 * @return Model : The model that this experiment is connected to or
	 *         <code>null</code> if no connection is established.
	 */
	public Model getModel() {

		return _client;

	}

	/**
	 * Returns the name of the path the experiment's report-, trace-, debug- and
	 * error-files are written to.
	 * 
	 * @return String the experiment's output path
	 */
	public String getOutputPath() {
		return new File(_pathName).getAbsolutePath();
	}

	public List<List<String>> getOutputAppendixes() {
	    
	    List<List<String>> appendixes = new ArrayList<List<String>>();
	    
	    ArrayList<String> debugAppendixes = new ArrayList<String>(); 
	    for (OutputType o : this._debugOutput) {
	        debugAppendixes.add(o.getAppendix());
	    }
	    appendixes.add(debugAppendixes);
	    
        ArrayList<String> traceAppendixes = new ArrayList<String>(); 
        for (OutputType o : this._debugOutput) {
            traceAppendixes.add(o.getAppendix());
        }
        appendixes.add(traceAppendixes);
        
        ArrayList<String> errorAppendixes = new ArrayList<String>(); 
        for (OutputType o : this._debugOutput) {
            errorAppendixes.add(o.getAppendix());
        }
        appendixes.add(errorAppendixes);
        
        ArrayList<String> reportAppendixes = new ArrayList<String>(); 
        for (OutputType o : this._debugOutput) {
            reportAppendixes.add(o.getAppendix());
        }
        appendixes.add(reportAppendixes);
	    
		return appendixes;
	}

	public long getRealTimeStartTime() {
		return _realTimeStartTime;
	}

	/**
	 * Returns the reference unit for this experiment. This is the time unit
	 * mapped to a time step of 1.0 in simulation time. So far, Hour, Minute,
	 * Second and Millisecond are supported. 
	 * Default (unless set explicitly) is TimeUnit.SECONDS.
	 * 
	 * @return TimeUnit : The reference unit.
	 */
	public TimeUnit getReferenceUnit() {
		return TimeOperations.getReferenceUnit();
	}

	/**
	 * Returns the resource database for this experiment. The <code>Res</code>
	 * objects need access to the resource database to note their resource
	 * allocations and requests and for deadlock detection.
	 * 
	 * @return desmoj.ResourceDB : the resource database storing all resource
	 *         allocations and requests.
	 * @author Soenke Claassen
	 */
	public ResourceDB getResourceDB() {

		return _resDB;
	}

	/**
	 * Returns the scheduler for this experiment. ModelComponents need access to
	 * the scheduler for identifying the current active entity or process and to
	 * schedule themselves or other schedulables to activate at a given time in
	 * the future.
	 * 
	 * @return Scheduler : The scheduler for this experiment
	 */
	public Scheduler getScheduler() {

		return clientScheduler;

	}

	/**
	 * Returns the simclock for this experiment. ModelComponents need access to
	 * the simclock for retrieveing the current simulation time.
	 * 
	 * @return SimCLock : The simclock for this experiment
	 */
	public SimClock getSimClock() {

		return clientScheduler.getSimClock();

	}

	/**
	 * Returns the TimeInstant when the experiment is expected to stop running.
	 * 
	 * @return TimeInstant : The time at which the experiment is expected to stop running.
	 */
	public TimeInstant getStopTime() {

		return _stopTime;
	}
	
    /**
     * Returns the Conditions which can cause an experiment to stop.
     * May be empty if there are no such Conditions.
     *
     * @return Condition
     * @author Tim Janz
     */
     public List<ModelCondition> getStopConditions() {
     
            return new java.util.ArrayList<ModelCondition>(this._stopConditions);
     }
     
     /**
      * Removes all conditions set to stop the experiment.
      */
      public void removeStopConditions() {
      
             this._stopConditions.clear();
      }

	/**
	 * Returns the threadgroup associated to this experiment. All Threads are
	 * associated to this threadgroup to get control of their number and state
	 * and to have a means to differentiate them from possible other
	 * experiments' threads.
	 * 
	 * @return java.lang.ThreadGroup
	 */
	ThreadGroup getThreadGroup() {

		return _expThreads;

	}
	
	/**
     * @deprecated Depends on TimeFormatter in use. Returns the experiment's number 
     * of floating point digits of simulation time that are displayed in the various output files,
     * as read from the SingleUnitTimeFormatter, if in use. Otherwise, 0 will be returned.
     * 
     * @return int : The number of floating point digits of simulation time to
     *         be displayed in output files
     */
    public int getTimeFloats() {
        
        if (TimeOperations.getTimeFormatter() instanceof SingleUnitTimeFormatter) {
            return (int) ((SingleUnitTimeFormatter)TimeOperations.getTimeFormatter())._floats;
        } else 
            return 0;
    }

	/**
	 * Displays the current state of the simulation run. If an experient is
	 * aborted, it can not be proceeded. All SimThreads still active are
	 * stopped, the main routine can finish.
	 * 
	 * @return boolean : Is <code>true</code> if the simulation is aborted,
	 *         <code>false</code> if it has not started yet or is still running
	 */
	public boolean isAborted() {

		return (_status >= ABORTED);

	}

	/**
	 * Shows if this experiment has already been connected to a model.
	 * 
	 * @return boolean : Is <code>true</code>, if experiment is connected to a
	 *         model, <code>false</code> otherwise
	 */
	public boolean isConnected() {

		return (_status >= CONNECTED); // model connected

	}

	/**
	 * Returns if the event-list processes concurrent Events in random order or
	 * not. Default is not.
	 * 
	 * @return boolean: <code>true</code> if concurrent Events are randomized,
	 *         <code>false</code> otherwise
	 * @author Ruth Meyer
	 */
	public boolean isRandomizingConcurrentEvents() {
		return clientScheduler.isRandomizingConcurrentEvents();
	}

	/**
	 * Displays the current state of the simulation run.
	 * 
	 * @return boolean : Is <code>true</code> if the simulation is running,
	 *         <code>false</code> if it has not started yet or has already
	 *         finished
	 */
	public boolean isRunning() {

		return (_status == RUNNING);

	}

	/**
	 * Returns if a progress bar should be displayed for this experiment or not.
	 * 
	 * @return boolean :<code>true</code> if a progress bar should be displayed
	 *         for this experiment, <code>false</code> otherwise.
	 */
	public boolean isShowProgressBar() {

		return _showProgressBar;
	}

	/**
	 * Displays the current state of the simulation run. If an experient is
	 * stopped, it can be proceeded by calling proceed().
	 * 
	 * @return boolean : Is <code>true</code>, if experiment is stopped,
	 *         <code>false</code> otherwise
	 */
	public boolean isStopped() {

		return (_status == STOPPED); // model stopped

	}
	
	/**
     * Determines whether or not an error or warning has yet occurred during 
     * this experiment.
     * 
     * @return boolean : <code>True</code> if at least one error has occurred
     *    in the model connected to this experiment or one of its submodels,
     *        <code>false</code> otherwise
     */
    public boolean hasError() {
        
        return _error; 
        
    }


	/**
	 * Proceeds with a stopped experiment. An experiment can be stopped, if
	 * either its status is changed from <code>RUNNING</code> to some other
	 * state, the scheduler runs out of scheduled events or if the
	 * <code>check()</code> method of the given stop <code>Condition</code>
	 * returns <code>true</code> after an event has been processed.
	 */
	public void proceed() {

		if (_status < STARTED) {
			sendWarning(
					"Can not proceed with Experiment! Command ignored.",
					"Experiment: " + getName() + " Method: void proceed().",
					"The Experiment has not been started yet.",
					"Only Experiments that have been stopped after method 'start()' has "
							+ "been called can use method 'proceed()' to continue.");
			return;
		}

		if (_status > STOPPED) {
			sendWarning("Can not proceed with Experiment! Command ignored.",
					"Experiment " + getName() + " Method: void proceed().",
					"The Experiment has already been aborted.",
					"Use method 'proceed()' only on stopped experiments.");
			return;
		}
		if (_status == STARTED) {
			// print status message to calm users waiting long, long, long
			// hours...
			if (!_silent) System.out.println("***** DESMO-J version " + getDesmoJVersion()
					+ " ***** \n" + getName() + " starts at simulation time "
					+ getScheduler().presentTime() + ".\n ...please wait...");
		}
		else{
		    if (!_silent) System.out.println(getName() + " resumes at simulation time "
					+ getScheduler().presentTime() + ".\n ...please wait...");
		}
		// display a progress bar if stop time is known and showProgressBar is
		// true
		if (_stopTime != null && _showProgressBar) {
			JFrame frame = new ExpProgressBar(this);

			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

			frame.pack();
			// frame.setSize(380,90);
			frame.setVisible(true);
		}

		_status = RUNNING; // now checked to run
		boolean gotEvent = false; // buffer to check if scheduler works

		try {

			while (_status == RUNNING) {
				// infinite loop until condition/time expired
				gotEvent = clientScheduler.processNextEventNote();
				if (gotEvent == false) {
					_status = STOPPED;
				}

				// check potential stop conditions
				if (!_stopConditions.isEmpty()) {
				    for (ModelCondition c : _stopConditions) {
    					if (c.check()) {
    						_status = STOPPED;
    						break;
    					}
				    }
				}

				// Sleep a while (modified by N. Knaak)
				if (_status == RUNNING && _delayInMillis != 0)
					Thread.sleep(_delayInMillis);
			}
		} catch (DESMOJException e) {
			System.err.println("desaster recovery");
			// this is the desaster recovery routine to stop simulation and save
			// the report to disc before exiting the faulty experiment
			_messMan.receive(e.getErrorMessage());
			report();
			finish();
			_status = ABORTED;
			e.printStackTrace();
		} catch (java.lang.InterruptedException e) {
			System.err.println("desaster recovery");
			// this is the disaster recovery routine to stop simulation and save
			// the report to disc before exiting the faulty experiment
			// messMan.receive(e.getMessage());
			report();
			finish();
			_status = ABORTED;
		}

		// give warning if reason for stopping was empty EventList
		if (gotEvent == false) {
			sendWarning("No more events scheduled! Experiment is stopped.",
					"Experiment '" + getName() + "' method void proceed().",
					"The scheduler has run out of events to handle.",
					"Make sure to always have events to be scheduled i.e. by letting an "
							+ "Entity create and schedule its successor.");
		}

		// print status message to user...
		if (!_silent) { 
		    System.out.println(getName() + " stopped at simulation time "
				+ getScheduler().presentTime() + ".");
		    if (hasError()) {
		        System.out.println("At least one error or warning has occurred.");
                if (_errorOutput.isEmpty()) {
                    System.out.println("Please re-run the siumulation with error output enabled.");
                } else {
                    System.out.println("Please refer to the error output for details.");
                }
		    }
	    }
	}

	/**
	 * Sets the delay between each step of the scheduler.
	 * 
	 * @param delay
	 *            : Delay time in milliseconds as a long value
	 * @author Nicolas Knaak
	 */
	public void setDelayInMillis(long delay) {
		_delayInMillis = delay;
	}

	/**
	 * Returns the delay between each step of the scheduler
	 * 
	 * @return A long value representing the delay time in milliseconds
	 * @author Nicolas Knaak
	 * 
	 */
	public long getDelayInMillis() {
		return _delayInMillis;
	}


	/**
	 * Registers a file output (Report, Trace, Error, Debug) in specific formats
	 * (e.g. HTML, ASCII, XML) at the experiment. Registered files will be
	 * flushed and closed after the experiment has finished. This is handy for
	 * modellers producing their own output who want their files to be closed at
	 * the end of the experiment.
	 * 
	 * @param file
	 *            desmoj.report.FileOutput : The file to be closed with the end
	 *            of an experiment
	 */
	public void register(OutputType file) {

		if (file == null) {
			sendWarning("Can not register OutputType! Command ignored.",
					"Experiment '" + getName()
							+ "' method void register(OutputType file).",
					"The parameter given was a null reference.",
					"Make sure to only connect valid OutputType at the Experiment.");
			return;
		}

		if (_registryOutputType.contains(file))
			return; // file already registered

		_registryOutputType.add(file);

	}

	/**
	 * Registers a custom file output at the experiment, e.g. TimeSeries
	 * plotting data to a file. Registered files will be flushed and closed
	 * after the experiment has finished. This is handy for modellers producing
	 * their own output who want their files to be closed at the end of the
	 * experiment.
	 * 
	 * @param file
	 *            desmoj.report.FileOutput : The file to be closed with the end
	 *            of an experiment
	 */
	public void registerFileOutput(FileOutput file) {

		if (file == null) {
			sendWarning("Can not register FileOutput! Command ignored.",
					"Experiment '" + getName()
							+ "' method void register(OutputType file).",
					"The parameter given was a null reference.",
					"Make sure to only connect valid FileOutput at the Experiment.");
			return;
		}

		if (_registryFileOutput.contains(file))
			return; // file already registered

		_registryFileOutput.add(file);

	}

	/**
	 * Connects a model to this experiment. The given model must not be submodel
	 * of other models and not already be connected to some other experiment.
	 * Otherwise an errormessage will be given and the experiment will be
	 * stopped.
	 */
	void registerModel(Model mainModel) {

		if (mainModel == null) {
			sendWarning(
					"Can not register model at experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void registerModel(Model mainModel)'",
					"The parameter passed was a null reference.",
					"Make sure to connect a valid main model to this experiment.");
			return; // no connection possible.
		}

		if (mainModel.getModel() != null) {
			sendWarning(
					"Can not register model at experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void registerModel(Model mainModel)'",
					"The model references another model as its owner, thus can not be the "
							+ "main model.",
					"Make sure to connect a valid main model to this experiment.");
			return; // no connection possible.
		}

		if (isConnected()) {
			sendWarning(
					"Can not register model at experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void registerModel(Model mainModel)'",
					"This experiment is already connected to model : "
							+ _client.getName(),
					"An experiment may only be connected to one main model at a time.");
			return; // no connection possible.
		}

		_status = CONNECTED;
		_client = mainModel;
		_client.setMain();

	}

	/**
	 * Removes a messagereceiver for debugnotes from the experiment's
	 * messagedistributor. Whenever a model produces a message of that type, it
	 * will not be sent to the given messagereceiver anymore. Note that if the
	 * messagereceiver is also registered for other types of messages, these
	 * will not be affected. Use method
	 * <code>removeReceiverAll(MessageReceiver msgRec)</code> to remove a
	 * messagereceiver from all types of messages.
	 * 
	 * @param msgRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver to be
	 *            removed from the messagedistributor's list for the given
	 *            messagetype
	 */
	public void removeDebugReceiver(MessageReceiver msgRec) {

		if (msgRec == null) {
			sendWarning(
					"Can not remove receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void removeDebugReceiver"
							+ "(MessageReceiver msgRec)'",
					"The parameter 'msgRec' passed was a null reference.",
					"Make sure to give a valid MessageReciever reference before removing it "
							+ "from the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.deRegister(msgRec, debugnote);

	}

	/**
	 * Removes a messagereceiver for errormessages from the experiment's
	 * messagedistributor. Whenever a model produces a message of that type, it
	 * will not be sent to the given messagereceiver anymore. Note that if the
	 * messagereceiver is also registered for other types of messages, these
	 * will not be affected. Use method
	 * <code>removeReceiverAll(MessageReceiver msgRec)</code> to remove a
	 * messagereceiver from all types of messages.
	 * 
	 * @param msgRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver to be
	 *            removed from the vessagedistributor's list for the given
	 *            messagetype
	 */
	public void removeErrorReceiver(MessageReceiver msgRec) {

		if (msgRec == null) {
			sendWarning(
					"Can not remove receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void removeErrorReceiver"
							+ "(MessageReceiver msgRec)'",
					"The parameter 'msgRec' passed was a null reference.",
					"Make sure to give a valid MessageReciever reference before removing it "
							+ "from the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.deRegister(msgRec, errormessage);

	}

	/**
	 * Removes a messagereceiver from the experiment's messagedistributor. The
	 * given messagereceiver will not receive messages of any type any more Use
	 * method <code>removeReceiver(MessageReceiver msgRec, Class
	 * messageType)</code> to remove the messagereceiver from one type of
	 * messages only.
	 * 
	 * @param msgRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver to be
	 *            removed from the messagedistributor's list for the given
	 *            messagetype
	 */
	public void removeReceiver(MessageReceiver msgRec) {

		if (msgRec == null) {
			sendWarning(
					"Can not remove receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void removeReceiver(MessageReceiver "
							+ "msgRec)'",
					"The parameter 'msgRec' passed was a null reference.",
					"Make sure to give a valid MessageReciever reference before removing it "
							+ "from the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.deRegister(msgRec);

	}

	/**
	 * Removes a messagereceiver for the given subtype of message from the
	 * Experiment's messagedistributor. Whenever a model produces a message of
	 * that type, it will not be sent to the given messagereceiver anymore. Note
	 * that if the messagereceiver is also registered for other types of
	 * messages, these will not be affected. Use method
	 * <code>removeReceiverAll(MessageReceiver msgRec)</code> to remove a
	 * messagereceiver from all types of messages.
	 * 
	 * @param msgRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver to be
	 *            removed from the messagedistributor's list for the given
	 *            messagetype
	 * @param messageType
	 *            Class : The type of message not to be sent to the given
	 *            messagereceiver
	 */
	public void removeReceiver(MessageReceiver msgRec, Class<?> messageType) {

		if (msgRec == null) {
			sendWarning(
					"Can not remove receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void removeReceiver(MessageReceiver "
							+ "msgRec, Class messageType)'",
					"The parameter 'msgRec' passed was a null reference.",
					"Make sure to give a valid MessageReciever reference before removing it "
							+ "from the experiment's messaging system.");
			return; // do nothing
		}

		if (messageType == null) {
			sendWarning(
					"Can not remove receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void removeReceiver(MessageReceiver "
							+ "msgRec, Class messageType)'",
					"The parameter 'msgRec' passed was a null reference.",
					"Make sure to give a valid MessageReciever reference before removing it "
							+ "from the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.deRegister(msgRec, messageType);

	}

	/**
	 * Removes a messagereceiver for tracenotes from the experiment's
	 * messagedistributor. Whenever a model produces a message of that type, it
	 * will not be sent to the given messagereceiver anymore. Note that if the
	 * messagereceiver is also registered for other types of messages, these
	 * will not be affected. Use method
	 * <code>removeReceiverAll(MessageReceiver msgRec)</code> to remove a
	 * messagereceiver from all types of messages.
	 * 
	 * @param msgRec
	 *            desmoj.report.MessageReceiver : The new messagereceiver to be
	 *            removed from the messagedistributor's list for the given
	 *            messagetype
	 */
	public void removeTraceReceiver(MessageReceiver msgRec) {

		if (msgRec == null) {
			sendWarning(
					"Can not remove receiver to experiment! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'void removeTraceReceiver"
							+ "(MessageReceiver msgRec)'",
					"The parameter 'msgRec' passed was a null reference.",
					"Make sure to give a valid MessageReciever reference before removing it "
							+ "from the experiment's messaging system.");
			return; // do nothing
		}

		_messMan.deRegister(msgRec, tracenote);

	}

	/**
	 * Overrides inherited <code>NamedObjectImp.rename(String newName)</code>
	 * method to prevent the user from changing the experiment's name during an
	 * experiment. Renaming is not allowed with experiments, since it would not
	 * allow the user to identify the reports produced by an experiment. The
	 * method simply returns without changing the experiment's name, ignoring
	 * the given parameter.
	 * 
	 * @param newName
	 *            java.lang.String : The parameter given is not taken as the new
	 *            name, method simply returns
	 */
	public void rename(String newName) {

		// do nothing since renaming experiments is not allowed
		// would do too much confusion

	}

	/**
	 * Writes a report about the model connected top this experiment, its
	 * reportable components and all related submodels into the report output.
	 * Note that a report can only be produced, if a valid main model is already
	 * connected to the experiment.
	 */
	public void report() {

		// just pass on the call with main model as parameter
		report(_client);

	}

	/**
	 * Writes a report about the given model which has to be connected to this
	 * experiment as main model or as a submodel. Note that this will report
	 * about a branch of the tree of submodels constructed. A report will only
	 * be produced, if the model given is connected to this experiment. All
	 * reportable components of this model and all related submodels will be
	 * sent to the report output configured at the experiment's
	 * messagedistributor. Note that a report can only be produced, if a valid
	 * main model is already connected to the experiment.
	 */
	public void report(Model m) {

		List<Reporter> reporters;
		// buffer for the reportmanager returned by client

		if (_status < CONNECTED) {
			sendWarning(
					"Can not produce report! Command ignored.",
					"Experiment: " + getName()
							+ " Method: void report(Model m).",
					"The Experiment has not been connected to a model to report about yet.",
					"Connect a model to the experiment first using the model's method "
							+ "connectToExperiment(Experiment exp).");
			return; // no client there to be reported
		}

		if (_status >= ABORTED) {
			// do nothing since experiment has already been aborted and all
			// output channels are already shut down
			return; // Experiment aborted
		}

		if (m == null) {
			sendWarning("Can not produce report! Command ignored.",
					"Experiment: " + getName()
							+ " Method: void report(Model m).",
					"The model parameter given is a null reference.",
					"Always make sure to use valid references.");
			return; // no model there to be reported
		}

		if (m.getExperiment() != this) {
			sendWarning(
					"Can not produce report! Command ignored.",
					"Experiment: " + getName()
							+ " Method: void report(Model m).",
					"The model parameter given is connected to a different experiment.",
					"Only experiments connected to theat model can produce reports "
							+ "about that model.");
			return; // model connected to other experiment
		}

		// get the client's reportmanager containing all reporters in sorted
		// order
		reporters = m.report();

		// get all out according to sorted order and send them to the report
		// output
		// registered at the experiment's messagemanager
		for (Reporter r : reporters) {

			_messMan.receive(r);

		}
	}

	/**
	 * Creates and sends a debugnote to the messagedistributor. Be sure to have
	 * a correct location, since the object and method that the error becomes
	 * apparent is not necessary the location it was produced in. The
	 * information about the simulation time is extracted from the Experiment
	 * and must not be given as a parameter.
	 * 
	 * @param description
	 *            java.lang.String : The description of the error that occured
	 */
	void sendDebugNote(String component, String description) {

		// comnpose the DebugNote and send it in one command
		sendMessage(new DebugNote(clientScheduler.getCurrentModel(),
				clientScheduler.getSimClock().getTime(), component, description));

	}

	/**
	 * Sends a message to the messagedistributor. Note that there are other
	 * shorthands for sending the standard DESMO-J messages.
	 * 
	 * @param m
	 *            Message : The Message to be transmitted
	 * @see ModelComponent#sendTraceNote
	 * @see ModelComponent#sendDebugNote
	 * @see ModelComponent#sendWarning
	 */
	void sendMessage(Message m) {

		if (m == null) {
			sendWarning("Can't send Message!", "Experiment :" + getName()
					+ " Method: SendMessage(Message m)",
					"The Message given as parameter is a null reference.",
					"Be sure to have a valid Message reference.");
			return; // no proper parameter
		}
		
		if (!_error && m instanceof ErrorMessage) {
		    _error = true;
		}		    

		_messMan.receive(m);
	}

	/**
	 * Creates and sends an error message to the messagedistributor to warn the
	 * modeller that some conditions required by the framework are not met. Be
	 * sure to have a correct location, since the object and method that the
	 * error becomes apparent is not necessary the location it was produced in.
	 * The information about the simulation time is extracted from the
	 * experiment and must not be given as a parameter.
	 * 
	 * @param description
	 *            java.lang.String : The description of the error that occured
	 * @param location
	 *            java.lang.String : The class and method the error occured in
	 * @param reason
	 *            java.lang.String : The reason most probably responsible for
	 *            the error to occur
	 * @param prevention
	 *            java.lang.String : The measures a user should take to prevent
	 *            this warning to be issued again
	 */
	void sendWarning(String description, String location, String reason,
			String prevention) {

		// comnpose the WarningMessage and send it in one command
		sendMessage(new ErrorMessage(clientScheduler.getCurrentModel(),
				description, location, reason, prevention, clientScheduler
						.getSimClock().getTime()));

	}

	/**
	 * Sets the TimeFormatter to be used for output of time Strings.
	 * 
	 * @param format
	 *            TimeFormatter : the formatter to be used for formatting time
	 *            Strings.
	 * 
	 */
	public static void setTimeFormatter(TimeFormatter format) {
		TimeOperations.setTimeFormatter(format);
	}

	/**
	 * Determines if the event-list processes concurrent Events in random order
	 * or not. Default is not, i.e. when a new experiment is constructed, the
	 * event-list is set to "linear" order. Note: If you want the event-list to
	 * randomize concurrent Events you should call this method BEFORE scheduling
	 * any events. Otherwise any connections between events established via
	 * scheduleBefore() or scheduleAfter() are lost. So it's a good idea to call
	 * this method only once and right after constructing the experiment.
	 * 
	 * @param randomizing
	 *            boolean :<code>true</code> forces random order,
	 *            <code>false</code> forces "linear" order
	 * @author Ruth Meyer
	 */
	public void randomizeConcurrentEvents(boolean randomizing) {
		clientScheduler.setRandomizingConcurrentEvents(randomizing);
	}

	/**
	 * Sets the speed rate for an execution that is proportional to wall-clock
	 * time (real time). Set the speed rate to a value bigger than zero for a
	 * simulation that will progress proportional to wall-clock time. The
	 * following equation applies for speed rates >0 : rate*simulation time =
	 * wallclock-time. If the speed rate is 0 or less the simulation will be
	 * executed as fast as possible. Default is 0 (as-fast-as-possible).
	 * 
	 * @param rate
	 *            double : The execution speed rate
	 */
	public void setExecutionSpeedRate(double rate) {
		clientScheduler.setExecutionSpeedRate(rate);
	}

	/**
	 * Sets the seed of the SeedGenerator to the given value. If the seed is not
	 * set here, its default is 979, unless specified different in the
	 * ExperimentOptions.
	 * 
	 * @param seed
	 *            long : The seed for the SeedGenerator
	 */
	public void setSeedGenerator(long seed) {

		_distMan.setSeed(seed);

	}

	/**
	 * Sets the underlying pseudo random number generator to be used by all
	 * distributions created from now on. The default generator is
	 * LinearCongruentialRandomGenerator; any other generator to be used must
	 * implement the interface UniformRandomGenerator.
	 * 
	 * @see desmoj.core.dist.LinearCongruentialRandomGenerator
	 * @see desmoj.core.dist.UniformRandomGenerator
	 * 
	 * @param randomNumberGenerator
	 *            Class : The random number generator class to be used
	 */
	public void setRandomNumberGenerator(
			Class<? extends desmoj.core.dist.UniformRandomGenerator> randomNumberGenerator) {

		boolean classValid = false;
		// // Verify that a class implementing interface
		// desmoj.desmoj.core.dist.UniformRandomGenerator was passed
		// for (int i = 0; i < randomNumberGenerator.getInterfaces().length;
		// i++) {
		// if
		// (randomNumberGenerator.getInterfaces()[i].equals(desmoj.core.dist.UniformRandomGenerator.class))
		// {
		// classValid = true;
		// break;
		// }
		// }

		// Verify the class provided is not abstract
		if ((randomNumberGenerator.getModifiers() & java.lang.reflect.Modifier.ABSTRACT) > 0
				|| (randomNumberGenerator.getModifiers() & java.lang.reflect.Modifier.INTERFACE) > 0)
			classValid = false;

		// Update the random number generator...
		if (classValid) {

			this._distMan.setRandomNumberGenerator(randomNumberGenerator);

			// ...or otherwise return an error
		} else {

			this
					.sendWarning(
							"Invalid random number generator given! Method call ignored!",
							"Experiment '"
									+ getName()
									+ "', Method 'setRandomNumberGenerator(Class randomNumberGenerator)'",
							"The class provided '"
									+ randomNumberGenerator.getSimpleName()
									+ "' is abstract or does not implement the interface"
									+ " desmoj.desmoj.core.dist.UniformRandomGenerator.",
							"Make sure to use a non-abstract class that implements the interface"
									+ " desmoj.desmoj.core.dist.UniformRandomGenerator.");
		}
	}

	/**
	 * Sets the new value for showing the progress bar for this experiment or
	 * not.
	 * 
	 * @param newShowProgressBar
	 *            boolean : set it to <code>true</code> if a progress bar should
	 *            be displayed; for not showing the progress bar of this
	 *            experiment set it to <code>false</code>.
	 */
	public void setShowProgressBar(boolean newShowProgressBar) {

		this._showProgressBar = newShowProgressBar;
	}
	
	/**
     * Sets the new value for displaying basic experiment notifications like
     * 'experiment started', 'experiment stopped' oder 'experiment resumed'
     * at the system output.
     * 
     * @param silent
     *            boolean : set it to <code>true</code> to suppress notifications
     *            or <code>false</code> to print them.
     */
    public void setSilent(boolean silent) {

        this._silent = silent;
    }

	/**
	 * Sets the experiment's status to the given integer value. The value must
	 * be in the legal range of [-1,5], otherwise a warning is issued.
	 * 
	 * @param newStatus
	 *            int : The integer value of the experiments' new status
	 */
	void setStatus(int newStatus) {

		if ((newStatus < -1) || (newStatus > 5)) {
			sendWarning(
					"Can not start experiment! Command ignored.",
					"Experiment '" + getName() + "', Method 'start'",
					"No main model's connectToExperiment(Experiment e) method was called.",
					"Make sure to connect a valid main model first before starting "
							+ "this experiment.");
			return;
		} else
			_status = newStatus;

	}

	/**
	 * Starts the simulation with default start time 0. This method can only be
	 * used once on an experiment. it initializes the connected model and starts
	 * the simulation. Note that in order to stop the simulation, the
	 * <code>stop(TimeInstant stopTime)</code> method has to be called first!
	 */
	public void start() {

		// this allows us to use start() in loops for multiple experiment runs
		// in other words, this is a shortcut for the lazy programmer
		if (_status == STOPPED)
			proceed();

		// here's what start was supposed to be at first
		// a shortcut for startig an Experiment at TimeInstant(0)
		else {
			// now prepare connected model to start simulation
			start(new TimeInstant(0));
		}

	}

	/**
	 * Starts the experiment with the given simulation time as starting time.
	 * The experiment will not start unless a valid model has been connected to
	 * it before. Note that in order to stop the simulation at some point of
	 * time, the <code>stop</code> method has to be called first.
	 * <code>StopCondition</code> s can be given alternatively.
	 * 
	 * @param initTime
	 *            TimeInstant : The starting time instant
	 */
	public void start(TimeInstant initTime) {

		if (_status < CONNECTED) {
			sendWarning(
					"Can not start experiment! Command ignored.",
					"Experiment: " + getName()
							+ " Method: void start(SimTime initTime)",
					"The Experiment has not been connected to a model to report about yet.",
					"Connect a model to the experiment first using the model's method "
							+ "connectToExperiment(Experiment exp).");
			return;
		}
		if (_status > CONNECTED) {
			sendWarning(
					"Can not start experiment! Command ignored.",
					"Experiment: " + getName()
							+ " Method: void start(SimTime initTime)",
					"The Experiment has already been started before.",
					"An experiment can only be started once. If it has been stopped, "
							+ "it can be issued to continue using method proceed()");
			return;
		}

		// check initial TimeInstant parameter
		if (initTime != null) {
			clientScheduler.getSimClock().setInitTime(initTime);
			if(!TimeInstant.isEqual(initTime,new TimeInstant(0))){
			_client.reset();
			}
		} else {
			clientScheduler.getSimClock().setTime(new TimeInstant(0));
			sendWarning(
					"Invalid start time parameter given! Start time set to "
							+ clientScheduler.presentTime() + ".",
					"Experiment: '" + getName()
							+ "', Method: void start(SimTime initTime)",
					"A null calue or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstnat object, otherwise use method "
							+ "start() without TimeInstant parameter.");
		}

		// client.init(); already done in connectToExperiment
		_client.doInitialSchedules();
		_client.doSubmodelSchedules();
		TimeOperations.setStartTime(initTime);
		_client.register(new SimulationRunReporter.SimulationRunReporterProvider(_client));
		// now everything is set up, go on and process events
		_status = STARTED;
		this._realTimeStartTime = System.nanoTime();

		proceed();

	}
	
    /**
     * @deprecated Use start(TimeInstant initTime). 
     * Starts the experiment with the given simulation time as starting time.
     * The experiment will not start unless a valid model has been connected to
     * it before. Note that in order to stop the simulation at some point of
     * time, the <code>stop</code> method has to be called first.
     * <code>StopCondition</code> s can be given alternatively.
     * 
     * @param initTime
     *            TimeInstant : The starting time instant
     */
    public void start(SimTime initTime) {
        start(SimTime.toTimeInstant(initTime));
    }

	/**
	 * Specifies a ModelCondition to stop the simulation. Note that this methode can
	 * be called muliple times, defining alternative conditions to terminate the 
	 * simulation. Once at least one of the conditions passed using this method 
	 * returns true, the experiment will stop. 
	 * Beware that the simulation will run endlessly if none of the conditions 
	 * are met; thus it is recommended to additionally always use a time limit 
	 * if none of the conditions in question can be proven to be met during the 
	 * run of the simulation!
	 * 
	 * @param stopCond
	 *            ModelCondition : A condition to stop the simulation once
     *            it's check() methode returns true.
	 */
	public void stop(ModelCondition stopCond) {

		if (stopCond == null) {
			sendWarning("Can not set stop-condition! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'stop(Condition stopCond)'",
					"The parameter passed was either null or a not initialized "
							+ "Condition reference.",
					"Make sure to provide a valid stop Condition for "
							+ "this experiment.");
		} else {
			this._stopConditions.add(stopCond);
		}

	}
	
	@Deprecated
    /**
     * @deprecated Replaced by <code>stop(ModelCondition stopCond)</code>
     * 
     * Specifies a Condition to stop the simulation. 
     * 
     * @param stopCond
     *            Condition<?> : A condition to stop the simulation once
     *            it's check() methode returns true.
     */
    public void stop(Condition<?> stopCond) {

        if (stopCond == null) {
            sendWarning("Can not set stop-condition! Command ignored.",
                    "Experiment '" + getName()
                            + "', Method 'stop(Condition stopCond)'",
                    "The parameter passed was either null or a not initialized "
                            + "Condition reference.",
                    "Make sure to provide a valid stop Condition for "
                            + "this experiment.");
        } else {
            this.stop(new ModelCondition.ConditionWrapper(this.getModel(), stopCond));
        }

    }

	/**
	 * Stops the simulation at the given point of simulation time. If no valid
	 * simulation time is given, the default is 0 which would not
	 * let the simulation run past that time. Repeatedly calling this method
	 * will override stop times specified before. 
	 * 
	 * @param stopTime
	 *            desmoj.TimeInstant : The point of simulation time to stop the
	 *            simulation
	 */
	public void stop(TimeInstant stopTime) {
		if (stopTime == null) {
			sendWarning(
					"Can not set stop-time! The stop-time will be set to 0",
					"Experiment '" + getName()
							+ "', Method: 'stop(TimeInstant stopTime)'",
					"The parameter passed was either null or a not initialized "
							+ "TimeInstance reference.",
					"Pass an initialized TimeInstant object as stop time.");

			ExternalEventStop stopper = new ExternalEventStop(_client,
					"Simulation stopped", true);
			stopper.schedule(new TimeInstant(0));

		} else {

			this._stopTime = stopTime;
			if (this._stopTimeEvent != null) this._stopTimeEvent.cancel();

			this._stopTimeEvent = new ExternalEventStop(_client, "Simulation stopped", true);
			_stopTimeEvent.schedule(stopTime);
		}
	}

	/**
	 * @deprecated Stops the simulation at the given point of simulation time.
	 *             If no valid simulation time is given, the default is 0.0
	 *             which would not let the simulation run past that time.
	 * 
	 * @param stopTime
	 *            desmoj.SimTime : The point of simulation time to stop the
	 *            simulation
	 */
	@Deprecated
	public void stop(SimTime stopTime) {
		stop(SimTime.toTimeInstant(stopTime));
	}

	/**
	 * Stops the simulation at the current simulation time (immediately). A
	 * stopped Simulation run can be resumed by calling proceed().
	 */	
	public void stop() {
		setStatus(STOPPED);
		clientScheduler.signalStop();
	}

	/**
	 * Returns a boolean indicating whether trace notes are forwarded to the
	 * trace ouput or not. Trace ouput can be switched on and off using the
	 * methods <code>traceOn(TimeInstant startTime)</code> and
	 * <code>traceOff(TimeInstant stopTime)</code>
	 * 
	 * @return boolean : Is <code>true</code>
	 */
	public boolean traceIsOn() {

		return _messMan.isOn(tracenote);

	}

	/**
	 * Switches the trace output off at the given point of simulation time.
	 * 
	 * @param stopTime
	 *            TimeInstant : The point in simulation time to switch trace off
	 */
	public void traceOff(TimeInstant stopTime) {

		// check initial TimeInstant parameter
		if (stopTime == null) {
			sendWarning(
					"Invalid start time parameter for trace output given! "
							+ "Trace output is set to start immediately.",
					"Experiment '" + getName()
							+ "', Method 'traceOn(TimeInstant startTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object, otherwise use method "
							+ "start() without TimeInstant parameter.");
			stopTime = clientScheduler.presentTime();
		}

		// check if parameter is in future
		if (TimeInstant.isAfter(clientScheduler.presentTime(), stopTime)) {
			sendWarning("Invalid start time parameter for trace output given! "
					+ "Trace output is set to start immediately.",
					"Experiment '" + getName()
							+ "', Method 'traceOn(TimeInstant stopTime)'",
					"The stopTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			stopTime = clientScheduler.presentTime();
		}

		ExternalEvent traceOff = new ExternalEventTraceOff(_client, true);
		traceOff.schedule(stopTime);
	}
	
    /**
     * @deprecated Use traceOff(TimeInstant startTime). Switches the trace output off at the given point of simulation time.
     * 
     * @param stopTime
     *            SimTime : The point in simulation time to switch trace off
     */
    public void traceOff(SimTime stopTime) {
        this.traceOff(SimTime.toTimeInstant(stopTime));        
    }

	/**
	 * Switches the trace output on at the given point of simulation time.
	 * 
	 * @param startTime
	 *            TimeInstant : The point in simulation time to switch trace on
	 */
	public void traceOn(TimeInstant startTime) {

		// check initial TimeInstant parameter
		if (startTime == null) {
			sendWarning(
					"Invalid start time parameter for trace output given! "
							+ "Trace output is set to start immediately.",
					"Experiment '" + getName()
							+ "', Method 'traceOn(TimeInstant startTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object, otherwise use method "
							+ "start() without TimeInstant parameter.");
			startTime = clientScheduler.presentTime();
		}

		// check if parameter is in future
		if (TimeInstant.isAfter(clientScheduler.presentTime(), startTime)) {
			sendWarning("Invalid start time parameter for trace output given! "
					+ "Trace output is set to start immediately.",
					"Experiment '" + getName()
							+ "', Method 'traceOn(TimeInstant startTime)'",
					"The startTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			startTime = clientScheduler.presentTime();
		}

        // if parameter equals current time, set trace on immediately, e.g. 
        // to include initial scheduling
        if (TimeInstant.isEqual(clientScheduler.presentTime(), startTime)) {
            this.getMessageManager().switchOn(Experiment.tracenote);
            _client.sendTraceNote("Trace switched on");
        // Otherwise schedule an appropriate event    
        } else {
            ExternalEvent traceOn = new ExternalEventTraceOn(_client, true);
            traceOn.schedule(startTime);
        }

	}
	
	/**
     * @deprecated Use traceOn(TimeInstant startTime). Switches the trace output on at the given point of simulation time.
     * 
     * @param startTime
     *            SimTime : The point in simulation time to switch trace on
     */
    public void traceOn(SimTime startTime) {
        this.traceOn(SimTime.toTimeInstant(startTime));        
    }

	/**
	 * Switches the trace output on for the given period of simulation time. If
	 * the second parameter (off) is "sooner" then the first parameter (on),
	 * they will be swapped automatically. Same parameters will result in no
	 * trace output at all.
	 * 
	 * @param startTime
	 *            TimeInstant : The point in simulation time to switch trace on
	 * @param stopTime
	 *            TimeInstant : The point in simulation time to switch trace off
	 */
	public void tracePeriod(TimeInstant startTime, TimeInstant stopTime) {
		if (startTime == null) {
			sendWarning(
					"Invalid start time parameter for trace output given! Command ignored",
					"Experiment '" + getName()
							+ "', Method 'tracePeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object.");
			return;
		}

		// check initial TimeInstant parameter
		if (stopTime == null) {
			sendWarning(
					"Invalid stop time parameter for trace output given! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'tracePeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"A null value or a not initialized TimeInstant reference has been passed.",
					"Make sure to have a valid TimeInstant object.");
			return;
		}

		// check for correct order in parameters
		if (TimeInstant.isAfter(startTime, stopTime)) {

			// swap parameters
			TimeInstant buffer = stopTime;
			stopTime = startTime;
			startTime = buffer;

		}

		// check if stop parameter is in future
		if (TimeInstant.isAfter(clientScheduler.presentTime(), stopTime)) {
			sendWarning(
					"Invalid stop time parameter for trace output given! Command ignored.",
					"Experiment '" + getName()
							+ "', Method 'tracePeriod(TimeInstant startTime, "
							+ "TimeInstant stopTime)'",
					"The stopTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			return;
		}

		// check if start parameter is in past
		if (TimeInstant.isAfter(clientScheduler.presentTime(), startTime)) {
			sendWarning("Invalid start time parameter for trace output given! "
					+ "Trace output has been set to start immediately.",
					"Experiment '" + getName()
							+ "', Method 'tracePeriod(TimeInstant startTime, "
							+ "TimeInstant startTime)'",
					"The startTime given is in the past.",
					"Make sure to give a TimeInstant parameter larger than the current time.");
			startTime = clientScheduler.presentTime();
		}

		// set trace to switch on
		traceOn(startTime);

		// set trace to switch off
		traceOff(stopTime);
	}

	/**
	 * @deprecated Replaced by tracePeriod(TimeInstant startTime, TimeInstant
	 *             stopTime). Switches the trace output on for the given period
	 *             of simulation time. If the second parameter (off) is "sooner"
	 *             then the first parameter (on), they will be swapped
	 *             automatically. Same parameters will result in no trace output
	 *             at all.
	 * 
	 * @param startTime
	 *            SimTime : The point in simulation time to switch trace on
	 * @param stopTime
	 *            SimTime : The point in simulation time to switch trace off
	 */
	@Deprecated
	public void tracePeriod(SimTime startTime, SimTime stopTime) {
		tracePeriod(SimTime.toTimeInstant(startTime), SimTime
				.toTimeInstant(stopTime));
	}

	/**
	 * Triggers the reporters of the given model or submodel to write their
	 * report data into the report output registered at the experiment's
	 * messagemanager. The string given will be added as a suffix to the report
	 * filename to help identify teh report when more than one report is
	 * produced by one Experiment at differnet points of simulation time.
	 * 
	 * @param m
	 *            desmoj.Model
	 * @param suffix
	 *            java.lang.String : Suffix for report filename if multiple
	 *            reports are drawn
	 */
	public void writeReport(Model m, String suffix) {

		if (suffix == null)
			suffix = "";

		// buffer used for storing the filename in
		String nameBuffer = null;

		// now flush and close all files and reopen with new names
		for (FileOutput f : _registryFileOutput) {

			// remember the name the file had
			nameBuffer = f.getFileName();

			// flush buffer and close file
			f.close();

			// open new file with old name stripping off old suffix and
			// adding new suffix
			nameBuffer = nameBuffer.substring(0, nameBuffer.lastIndexOf("."));
			f.open(nameBuffer.substring(0, (nameBuffer.length() - lastSuffix))
					+ suffix + "html");
		}
		lastSuffix = suffix.length(); // remember last suffix length
		report(m);
	}

	/**
	 * Triggers the reporters to write their data into the report output
	 * registered at the experiment's messagemanager. The string given will be
	 * added as a suffix to the report filename to help identification when more
	 * than one report is produced by one Experiment at differnet points of
	 * simulation time.
	 * 
	 * @param suffix
	 *            java.lang.String : Suffix for report filename if multiple
	 *            reports are drawn
	 */
	public void writeReport(String suffix) {

		if (suffix == null)
			suffix = "";

		// write report data about main model
		report(_client);

		// buffer used for storing the filename in
		String nameBuffer = null;

		// now flush and close all files and reopen with new names
		for (FileOutput f : _registryFileOutput) {

			// remember the name the file had
			nameBuffer = f.getFileName();

			// flush buffer and close file
			f.close();

			// open new file with old name stripping off old suffix and
			// adding new suffix
			// strip the _debug / _error / _trace / _report part
			nameBuffer = nameBuffer.substring(0, nameBuffer.lastIndexOf("_"));
			// strip the previous suffix
			nameBuffer = nameBuffer.substring(0, nameBuffer.length()
					- lastSuffix);
			// add new suffix
			nameBuffer = nameBuffer + suffix;
			// now open file with new name
			f.open(nameBuffer);
		}
		lastSuffix = suffix.length(); // remember last suffix length
	}

	/**
	 * Returns the current DESMO-J version
	 * 
	 * @return The string "2.3.4".
	 */
	public static String getDesmoJVersion() {
		return "2.3.4";
	}

	/**
	 * Returns the DESMO-J license
	 * 
	 * @param html
	 *            boolean: Include link (HTML, true) or not (plain text, false)
	 * 
	 * @return The string "Apache License, Version 2.0", embedded in a HTML link
	 *         tag (currently http://www.apache.org/licenses/LICENSE-2.0) if
	 *         <code>html</code> is set true.
	 */
	public static String getDesmoJLicense(boolean html) {
		return html ? "<A HREF=http://www.apache.org/licenses/LICENSE-2.0>Apache License, Version 2.0</A>"
				: "Apache License, Version 2.0";
	}
	
    public void setDescription(String description) {
        this._description = description;
    }

    public String getDescription() {
        return _description;
    }
}