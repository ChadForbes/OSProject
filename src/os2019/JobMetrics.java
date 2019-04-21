package os2019;
import java.util.HashMap;
import java.util.Map;

// TODO
// need the following pieces copy/pasted around:
//
// when a process is loaded into memory:
//JobMetrics.Metric metric = JobMetrics.getInstance().addMetric(processID,
//    percentOfRAMUsed);
//metric.startWait();
//
// when a process is put onto a CPU:
//JobMetrics.Metric metric = JobMetrics.getInstance().getMetric(processID);
//metric.endWait();
//metric.startExecution();
//metric.assignToCPU(cpuID);
//metric.updateCacheUsage(percentOfCacheUsed);
//
// when a process finishes executing:
//JobMetrics.Metric metric = JobMetrics.getInstance().getMetric(processID);
//metric.endExecution();
//
// when a process performs an I/O operation:
//JobMetrics.Metric metric = JobMetrics.getInstance().getMetric(processID);
//metric.logIOOp();

/**
 * A wrapper for a collection of metrics regarding each job in the system.
 */
public class JobMetrics {
	//
	// static data
	//
	
	private static final JobMetrics instance;
	
	//
	// instance data
	//
	
	private Map<Integer, Metric> entries;
	
	//
	// constructors
	//
	
	static {
		instance = new JobMetrics();
	}
	
	private JobMetrics() {
		entries = new HashMap<Integer, Metric>();
	}
	
	//
	// instance methods
	//
	
	/**
	 * Add a metric for a new job into the collection.
	 * @param processID The ID of the process being registered.
	 * @param percentRAMUsage The percentage of RAM that the process is using.
	 * @return The metric created.
	 */
	public Metric addMetric(int processID, double percentRAMUsage) {
		Metric entry = new Metric(processID, percentRAMUsage); 
		entries.put(processID, entry);
		return entry;
	}
	
	/**
	 * Grabs the metrics for a process by its ID.
	 * @param processID The ID of the process for which metrics are being
	 * retrieved.
	 * @return The desired metrics object, or {@code null} if no process has
	 * been logged with the given process ID.
	 */
	public Metric getMetric(int processID) {
		return entries.get(processID);
	}
	
	/**
	 * Generate a string for pasting into a spreadsheet.
	 * @return A spreadsheet-friendly paste-able string containing all the
	 * required metrics of the system's jobs.
	 */
	public String metricDump() {
		String retStr = "";
		
		for(Metric m : entries.values())
			retStr += String.format("%1$s\t%2$s\t%3$s\t%4$s\t%5$s\t%6$s\n",
					m.processID, m.calcWaitTime(), m.calcCompletionTime(),
					m.getIOOps(), m.percentRAMUsage, m.getPercentCacheUsage());
		
		return retStr;
	}
	
	/**
	 * Generates a string for pasting into a spreadsheet of all of the data
	 * for processes assigned to the given CPU.
	 * @param cpuID The ID of the CPU to get metrics about.
	 * @return A spreadsheet-friendly paste-able string containing all of the
	 * required metrics for jobs assigned to the given processor.
	 */
	public String metricDumpByCPU(int cpuID) {
		String retStr = "";
		
		for(Metric m : entries.values())
			if(m.assignedCPU == cpuID)
				retStr += String.format("%1$s\t%2$s\t%3$s\t%4$s\t%5$s\t6$s\n",
						m.processID, m.calcWaitTime(), m.calcCompletionTime(),
						m.getIOOps(), m.percentRAMUsage,
						m.getPercentCacheUsage());
		
		return retStr;
	}
	
	//
	// static method
	//
	
	/**
	 * Gets the singleton instance of this class.
	 */
	public JobMetrics getInstance() {
		return instance;
	}
	
	
	//
	// non-constituent inner type
	//
	
	public static class Metric {
		// instance data
		
		public final int processID;
		public final double percentRAMUsage;
		private double percentCacheUsage;
		private long waitStart;
		private long waitEnd;
		private long executionStart;
		private long executionEnd;
		private int numberOfIOOps;
		private int assignedCPU;
		
		// constructor
		
		private Metric(int processID, double percentRAMUsage) {
			this.processID = processID;
			this.percentRAMUsage = percentRAMUsage;
			waitStart = 0l;
			waitEnd = 0l;
			executionStart = 0l;
			executionEnd = 0l;
		}
		
		// simple accessors
		
		/**
		 * Calculates the time between when the process was created and when it
		 * was put onto a processor.
		 * @return The time this job waited in seconds or {@code -1} if this
		 * process has not finished waiting.
		 */
		public double calcWaitTime() {
			if(waitEnd == 0l) // if wait hasn't ended
				return -1.0;
			
			return ((double)(waitEnd - waitStart)) / 1_000.0;
		}
		
		/**
		 * Calculates the time between when the process began execution and
		 * when it finished.
		 * @return The time to complete this job in seconds or {@code -1} if
		 * this process has not been recorded as ending.
		 */
		public double calcCompletionTime() {
			if(executionEnd == 0l) // if not completed
				return -1.0;
			
			return ((double)(executionEnd - executionStart)) / 1_000.0;
		}
		
		public int getIOOps() {
			return numberOfIOOps;
		}
		
		public double getPercentCacheUsage() {
			return percentCacheUsage;
		}
		
		public int getAssignedCPU() {
			return assignedCPU;
		}
		
		// other instance methods
		
		/**
		 * Mark the current system time as the beginning of this job's wait
		 * period.
		 */
		public void startWait() {
			waitStart = System.currentTimeMillis();
		}
		
		/**
		 * Mark the current system time as the end of this job's wait period.
		 */
		public void endWait() {
			waitEnd = System.currentTimeMillis();
		}
		
		/**
		 * Mark the current system time as the beginning of this job's
		 * execution.
		 */
		public void startExecution() {
			executionStart = System.currentTimeMillis();
		}
		
		/**
		 * Mark the current system time as the end of this job's execution.
		 */
		public void endExecution() {
			executionEnd = System.currentTimeMillis();
		}
		
		/**
		 * Log that this job has performed a single I/O operation.
		 */
		public void logIOOp() {
			numberOfIOOps++;
		}
		
		public void assignToCPU(int cpuID) {
			assignedCPU = cpuID;
		}
		
		/**
		 * Updates the amount of cache space that the processis using.
		 * @param percentUsed The percentage of cache on the CPU that this
		 * process uses.
		 */
		public void updateCacheUsage(double percentUsed) {
			percentCacheUsage = percentUsed;
		}
	}
}