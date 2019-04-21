package os2019;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import os2019.memory.DMAManager;
import os2019.memory.Disk;
import os2019.memory.MMU;
import os2019.processing.CPU;
import os2019.processing.Dispatcher;
import os2019.scheduling.LongTermScheduler;
import os2019.scheduling.ShortTermScheduler;

public final class Driver {
	//
	// static data
	//
	
	private static Driver instance;
	
	public static final int NUM_CPUS = 1;
	/** The time between simulated clock ticks.  The Driver and all CPU's will
	 * wait for this many milliseconds between loop iterations.
	 * @see Driver#doSimulation()
	 * @see CPU#run() */
	public static final long CLOCK_PERIOD_MILLIS = 10l;
	
	private static final String JOBS_FILE_PATH = "res/program-file.txt";
	
	//
	// instance data
	//
	
	public final MMU mmu;
	public final Disk disk;
	public final CPU[] cpus;
	public final ShortTermScheduler shortTermScheduler;
	public final LongTermScheduler longTermScheduler;
	public final DMAManager dmaManager;
	
	private final ExecutorService cpuThreadPool;
	
	//
	// constructors
	//
	
	private Driver() {
		mmu = new MMU();
		disk = new Disk();
		cpus = new CPU[NUM_CPUS];
		for(int lcv = 0; lcv < NUM_CPUS; lcv++)
			cpus[lcv] = new CPU(lcv);
		shortTermScheduler = new ShortTermScheduler(
				ShortTermScheduler.SchedulingType.FIFO, false);
		longTermScheduler = new LongTermScheduler();
		dmaManager = new DMAManager();
		
		cpuThreadPool = Executors.newFixedThreadPool(NUM_CPUS);
	}
	
	//
	// instance method
	//
	
	/**
	 * Checks to see whether the simulation is still running or not.
	 * @return {@code true} if simulation has remaining jobs on a processor or
	 *  in its new, ready, or wait queues.  Returns {@code false} if there are
	 *  no such processes in the system.
	 */
	public boolean simulationInProgress() {
		// check to make sure no CPU's are doing anything:
		for(CPU cpu : cpus)
			if(!cpu.idling())
				return true;
		
		// check new queue, ready queue, and wait queue for processes:
		return longTermScheduler.numUnloadedProcesses() > 0
				|| shortTermScheduler.numJobsReady() > 0; //TODO: Wait queue
	}
	
	public void doSimulation() throws InterruptedException {
		System.out.println("Simulation beginning.");
		
		disk.loadFromFile(JOBS_FILE_PATH);
		do {
			longTermScheduler.attemptLoadToRAM();
			
			shortTermScheduler.doScheduling();
			
			for(CPU cpu : cpus) // dispatch if possible
				if(cpu.idling()) {
					// if old job completed and needs to be removed from sys:
					if(cpu.terminatedSuccessfully()
							&& cpu.getLoadedPCB() != null)
						mmu.deallocate(cpu.getLoadedPCB());
					
					// dispatch a new job if possible:
					if(shortTermScheduler.numJobsReady() > 0) {
						Dispatcher.dispatch(cpu.cpuID); // dispatch job
						cpuThreadPool.execute(cpu); // start the cpu
					}
				} else if(shortTermScheduler // preempt if necessary
						.checkPreemptionCriterion(cpu.getLoadedPCB()))
					cpu.halt();
			
			// wait for next 'clock tick':
			Thread.sleep(CLOCK_PERIOD_MILLIS);
		} while(simulationInProgress());
	}
	
	public void endSimulation() {
		System.out.println("Simulation ending.");
		cpuThreadPool.shutdown();
	}
	
	public void endSimulationNow() {
		System.out.println("Simulation ending prematurely.");
		cpuThreadPool.shutdownNow();
	}
	
	//
	// static method
	//
	
	public static boolean init() {
		if(instance != null)
			return false;
		
		instance = new Driver();
		return true;
	}
	
	public static Driver getInstance() {
		return instance;
	}
	
	public static void main(String... args) {
		init();
		
		try {
			getInstance().doSimulation();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// ensure end of simulation to close threadpools:
			getInstance().endSimulation();
		}
	}
}