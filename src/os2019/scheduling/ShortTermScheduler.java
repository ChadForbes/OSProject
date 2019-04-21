package os2019.scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

import os2019.Driver;
import os2019.processing.PCB;

public class ShortTermScheduler {
	//
	// static data
	//
	
	private static final Comparator<PCB> FIFO_COMPARATOR =
			(PCB pcb1, PCB pcb2) -> {
		return 0;
	};
	private static final Predicate<PCB> FIFO_PREEMPT_CRITERION = (PCB pcb) -> {
		return false;
	};
	
	private static final Comparator<PCB> PRIORITY_COMPARATOR =
			(PCB pcb1, PCB pcb2) -> {
		return pcb1.getPriority() - pcb2.getPriority();
	};
	private static final Predicate<PCB> PRIORITY_PREEMPT_CRITERION =
			(PCB pcb) -> {
		// grab in-use instance of the scheduler in order to access
		//its ready queue:
		ShortTermScheduler schedInstance =
				Driver.getInstance().shortTermScheduler;
		
		// no swap if queue is empty:
		if(schedInstance.readyQueue.size() == 0)
			return false;
		
		// swap if highest-priority job in queue has higher priority
		// than the given job on a CPU (assumes queue is appropriately
		// scheduled using priority scheduling):
		return schedInstance.readyQueue.get(0).getPriority() 
				> pcb.getPriority();
	};
	
	//
	// instance data
	//
	
	private ArrayList<PCB> readyQueue;
	private Comparator<PCB> comparator;
	private Predicate<PCB> preemptCriterion;
	
	//
	// constructor
	//
	
	public ShortTermScheduler(SchedulingType type,
			boolean allowPreemption) {
		readyQueue = new ArrayList<PCB>();
		
		switch(type) {
		case FIFO:
			comparator = FIFO_COMPARATOR;
			preemptCriterion = FIFO_PREEMPT_CRITERION;
			break;
		case PRIORITY:
			comparator = PRIORITY_COMPARATOR;
			preemptCriterion = PRIORITY_PREEMPT_CRITERION;
			break;
		}
		if(!allowPreemption)
			preemptCriterion = (PCB pcb) -> { return false; } ;
	}
	
	//
	// instance methods
	//
	
	public int numJobsReady() {
		return readyQueue.size();
	}
	
	public boolean checkPreemptionCriterion(PCB pcb) {
		return preemptCriterion.test(pcb);
	}
	
	public PCB popNextJob() {
		if(readyQueue.size() == 0)
			return null;
		
		//doScheduling(); // for JIT implementation
		PCB pcb = readyQueue.get(0);
		readyQueue.remove(0);
		return pcb;
	}
	
	public void doScheduling() {
		readyQueue.sort(comparator);
	}
	
	public void addToRQueue(PCB pcb){
		readyQueue.add(pcb);
		pcb.setStatus(PCB.Status.READY);
	}
	
	//
	// non-constituent inner type
	//
	
	public enum SchedulingType {
		FIFO, PRIORITY
	}
}
