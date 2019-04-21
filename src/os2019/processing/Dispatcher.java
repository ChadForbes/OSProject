package os2019.processing;

import os2019.Driver;

public final class Dispatcher {
	public static void dispatch(int cpuID) {
		CPU cpu = Driver.getInstance().cpus[cpuID];
		PCB job = Driver.getInstance().shortTermScheduler.popNextJob();
		
		if(job == null) { // if no jobs available to dispatch
			//System.out.println("Attempt to dispatch, but no jobs are ready.");
			return;
		}
		
		// set data on cpu:
		cpu.loadedPCB = job;
		cpu.programCounter = job.getPC();
		cpu.priorityNum = job.getPriority();
		cpu.inputBuffer = job.inputBufferSize;
		cpu.outputBuffer = job.outputBufferSize;
		cpu.tempBuffer = job.tempBufferSize;
		cpu.jobBaseAddress = job.getJobStart();
		cpu.jobInstructionCount = job.codeSize;
		job.offloadRegisters(cpu.registers);
		
		// build the cache:
		int cacheSize = cpu.inputBuffer + cpu.outputBuffer + cpu.tempBuffer
				+ cpu.jobInstructionCount;
		cpu.cache = new String[cacheSize];
		// fill the cache:
		int pc = job.getJobStart();
		final int cacheStop = cacheSize < job.calcJobSize() ? cacheSize :
				job.calcJobSize();
		for (int i = 0; i < cacheStop; i++) {
			cpu.cache[i] = Driver.getInstance().mmu.read(pc, job);
			pc = Driver.getInstance().mmu.incrementPC(job, pc);
		}
		
		// send the CPU on its way:
		cpu.finishLoading();
	}
}
