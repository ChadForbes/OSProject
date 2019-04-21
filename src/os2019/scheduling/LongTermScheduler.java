package os2019.scheduling;

import java.util.LinkedList;
import java.util.List;

import os2019.Driver;
import os2019.processing.PCB;

public class LongTermScheduler {
	//
	// instance data
	//
	
	private List<PCB> newProcesses;
	
	//
	// constructor
	//
	
	public LongTermScheduler() {
		newProcesses = new LinkedList<PCB>();
	}
	
	//
	// instance methods
	//
	
	public int numUnloadedProcesses() {
		return newProcesses.size();
	}
	
	public void registerNewProcess(PCB pcb) {
		newProcesses.add(pcb);
		pcb.setStatus(PCB.Status.NEW);
	}
	
	public void attemptLoadToRAM() {
		//if(newProcesses.size() == 0)
		//	return;
		
		// load jobs to RAM as space becomes available:
		while(newProcesses.size() != 0) {
			PCB currentPCB = newProcesses.get(0);
			
			// allocate RAM space for the job:
			boolean allocatedSuccessfully = Driver.getInstance().mmu
					.allocate(currentPCB);
			if(!allocatedSuccessfully) // if RAM allocation failed
				break;
			
			// load job from disk into RAM:
			final int jobSize = currentPCB.calcJobSize();
			int pc = currentPCB.getJobStart();
			for(int lcv = 0; lcv < jobSize; lcv++) {
				// read word from disk:
				final String datum = Driver.getInstance().disk.read(
						lcv + currentPCB.startOnDisk);
				// write word to RAM:
				Driver.getInstance().mmu.write(pc, currentPCB, datum);
				pc = Driver.getInstance().mmu.incrementPC(currentPCB, pc);
			}
			
			// finish loading:
			Driver.getInstance().shortTermScheduler.addToRQueue(currentPCB);
			newProcesses.remove(0);
		}
	}
}