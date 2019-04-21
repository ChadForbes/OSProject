package os2019.processing;

import os2019.Driver;
import os2019.memory.MMU;

//Essentially stores a job's info and the corresponding data's info
public final class PCB {
	//
	// instance data
	//
	
	public final int jobID;
	public final int startOnDisk;
	public final int codeSize;
	public final int inputBufferSize;
	public final int outputBufferSize;
	public final int tempBufferSize;
	public final int burstTime;
	
	private final int[] registers;
	private Status status;
	private int programCounter;
	private int priority;
	private int affenitiveCPU;
	private int cyclesExecuted;
	private int startWord;
	
	//
	// constructor
	//
	
	public PCB(int jobID, int startOnDisk, int codeWords, int inputBufferWords,
			int outputBufferWords, int tempBufferWords, int priority) {
		this.jobID = jobID;
		this.startOnDisk = startOnDisk;
		codeSize = codeWords;
		inputBufferSize = inputBufferWords;
		outputBufferSize = outputBufferWords;
		tempBufferSize = tempBufferWords;
		this.priority = priority;
		burstTime = codeSize + inputBufferSize + outputBufferSize
				+ tempBufferSize;
		
		registers = new int[CPU.NUM_REGISTERS];
		status = Status.NEW;
		programCounter = -1;
		affenitiveCPU = -1;
		cyclesExecuted = 0;
	}
	
	//
	// accessors
	//
	
	//@formatter:off
	public int getPC() { return programCounter; }
	public int getPriority() { return priority; }
	public Status getStatus() { return status; }
	public int getAffenitiveCPU() { return affenitiveCPU; }
	public int getJobStart() { return startWord; }
	public int calcApproximateRemainingTime() { return burstTime - cyclesExecuted; }
	public int calcJobSize() { return codeSize + inputBufferSize
			+ outputBufferSize + tempBufferSize; }
	//@formatter:on
	
	public void offloadRegisters(int[] destRegistry) {
		for(int lcv = 0; lcv < CPU.NUM_REGISTERS; lcv++)
			destRegistry[lcv] = registers[lcv];
	}
	
	public int getVirtualAddressAtOffset(final int offsetFromJobStart) {
		final int pageOffset = offsetFromJobStart / MMU.PAGE_SIZE;
		
		int pc = getJobStart();
		for(int lcv = 0; lcv < pageOffset; lcv++) {
			pc += MMU.PAGE_SIZE - 1;
			pc = Driver.getInstance().mmu.incrementPC(this, pc);
		}
		pc += offsetFromJobStart % MMU.PAGE_SIZE;
		return pc;
	}
	
	//
	// simple mutators
	//
	
	//@formatter:off
	public void setPriority(int priority) { this.priority = priority; }
	public void setJobStart(int startWord) { this.startWord = startWord; }
	public void resetPC() { setPC(startWord); }
	public void setStatus(Status newStatus) { status = newStatus; }
	//@formatter:on
	
	//@formatter:off
	void setPC(int programCounter) { this.programCounter = programCounter; }
	void incrementCyclesExecuted(int numberOfCycles) { cyclesExecuted += numberOfCycles; }
	//@formatter:on
	
	void loadRegisters(int[] sourceRegistry) {
		for(int lcv = 0; lcv < CPU.NUM_REGISTERS; lcv++)
			registers[lcv] = sourceRegistry[lcv];
	}
	
	//
	// non-constituent inner type
	//
	
	public enum Status {
		NEW, READY, RUNNING, WAITING, TERMINATED
	}
	
	public class Builder { // TODO
		// constructor
		
		
		
		// instance methods
		
		public PCB build() {
			return null;
		}
	}
	
	/*
	public PCB(int jobID, int jobSize, int jobDiskIndex) {
		this.jobID = jobID;
		this.jobDiskIndex = jobDiskIndex;
		this.jobSize = jobSize;
		jobInMemory = false;
		registers=new int[16];
		Arrays.fill(registers,0);
	}
	
	//Data information
	private int dataDiskIndex;
	private int dataMemoryAddress;
	
	//Job instructions
	//private int jobID;
	private int jobPriority;
	private int jobDiskIndex;
	private int jobMemoryIndex;
	private int jobSize;
	
	
	//Buffer information
	//private int inputBuffer;
	//private int outputBuffer;
	//private int tempBuffer;
	
	private boolean IObound;
	private String instruction;
	private boolean jobInMemory;
	private boolean hasJobRan;
	private int[] registers;
	private int programCounter;
	private String[] cache;
	private int cacheSize;
	
	public int getCacheSize() {
		return cache.length;
	}
	
	public void setCacheSize(int cachesize) {
		this.cacheSize = cachesize;
	}
	
	public String[] getCache() {
		return cache;
	}
	
	public void setCache(String[] cache) {
		this.cache = cache;
	}
	
	public int getProgramCounter()
	{
		return programCounter;
	}
	public void setProgramCounter(int num)
	{
		programCounter =num;
	}
	public void setRegisters(int[] reg)
	{
		registers=reg;
	}
	public int[] getRegisters()
	{
		return registers;
	}
	
	public boolean isHasJobRan() {
		return hasJobRan;
	}
	
	public void setHasJobRan(boolean hasJobRan) {
		this.hasJobRan = hasJobRan;
	}
	
	public boolean isJobInMemory() {
		return jobInMemory;
	}
	
	public void setJobInMemory(boolean jobInMemory) {
		this.jobInMemory = jobInMemory;
	}
	
	public String getInstruction() {
		return instruction;
	}
	
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	
	public boolean isIObound() {
		return IObound;
	}
	
	public void setIObound(boolean IObound) {
		this.IObound = IObound;
	}
	
	public int getTempBuffer() {
		return tempBuffer;
	}
	
	public void setTempBuffer(int tempBuffer) {
		this.tempBuffer = tempBuffer;
	}
	
	public int getOutputBuffer() {
		return outputBuffer;
	}
	
	public void setOutputBuffer(int outputBuffer) {
		this.outputBuffer = outputBuffer;
	}
	
	public int getInputBuffer() {
		return inputBuffer;
	}
	
	public void setInputBuffer(int inputBuffer) {
		this.inputBuffer = inputBuffer;
	}
	
	public int getDataMemoryAddress() {
		return dataMemoryAddress;
	}
	
	public void setDataMemoryAddress(int dataMemoryAddress) {
		this.dataMemoryAddress = dataMemoryAddress;
	}
	
	public int getDataDiskIndex() {
		return dataDiskIndex;
	}
	
	public void setDataDiskIndex(int dataDiskIndex) {
		this.dataDiskIndex = dataDiskIndex;
	}
	
	public int getJobSize() {
		return jobSize;
	}
	
	public void setJobSize(int jobSize) {
		this.jobSize = jobSize;
	}
	
	public int getJobMemoryIndex() {
		return jobMemoryIndex;
	}
	
	public void setJobMemoryIndex(int jobMemoryIndex) {
		this.jobMemoryIndex = jobMemoryIndex;
	}
	
	public int getjobDiskIndex() {
		return jobDiskIndex;
	}
	
	public void setjobDiskIndex(int jobDiskIndex) {
		this.jobDiskIndex = jobDiskIndex;
	}
	
	public int getJobPriority() {
		return jobPriority;
	}
	
	public void setJobPriority(int jobPriority) {
		this.jobPriority = jobPriority;
	}
	
	public int getJobID() {
		return jobID;
	}
	
	public void setJobID(int jobID) {
		this.jobID = jobID;
	}
	*/
}