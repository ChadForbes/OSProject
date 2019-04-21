package os2019.memory;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Hashtable;

import os2019.processing.PCB;

/**
 * Class responsible for intercepting attempts to access RAM, translating
 * virtual addresses to physical addresses in the process, and managing the
 * distribution of memory to processes.
 */
public class MMU {
	//
	// static data
	//
	
	/** The number of words on each page. */
	public static final int PAGE_SIZE = 64;
	private static final int DMA_BUFFER_SPACE = PAGE_SIZE;
	/** The number of pages that fit in the instance of RAM the MMU is
	 * wrapping */
	private static final int NUM_PAGES = (RAM.RAM_SIZE - DMA_BUFFER_SPACE)
			/ PAGE_SIZE;
	private static final int DMA_BUFFER_START = RAM.RAM_SIZE - DMA_BUFFER_SPACE;
	
	//
	// instance data
	//
	
	private RAM ram;
	private Hashtable<Integer, List<Frame>> frameTable;
	private List<Integer> freeFrames;
	
	private int nextDMABufferIndex;
	
	//
	// constructors
	//
	
	static {
		// just in case:
		if(RAM.RAM_SIZE % PAGE_SIZE != 0)
			System.err.println("There is un-paged space in RAM");
	}
	
	public MMU() {
		ram = new RAM();
		frameTable = new Hashtable<Integer, List<Frame>>(NUM_PAGES);
		freeFrames = new LinkedList<>();
		for(int lcv = 0; lcv < NUM_PAGES; lcv++)
			freeFrames.add(lcv);
	}
	
	//
	// instance methods
	//
	
	/**
	 * Read the contents of memory at the given address.
	 * @param virtualAddress The virtual address to read from.
	 * @param processID The ID of the process being read from.
	 * @return The word stored in memory at the virtual address for the
	 * process.
	 */
	public String read(int virtualAddress, PCB pcb) {
		int physAddr = calcEffectiveAddress(virtualAddress, pcb);
		
		// if there is no frame for the given process at the v. address:
		if(physAddr == -1) {
			System.err.println(String.format("Illegal memory access attempt:"
					+ " process '%1$d' attempted to read from virtual address"
					+ " '%2$d', but it does not own the page with that"
					+ " address.", pcb.jobID, virtualAddress));
			return null;
		}
		if(physAddr == -2) {
			System.err.println(String.format("Illegal memory access attempt:"
					+ " process '%1$d' attempted to read from virtual address"
					+ " '%2$d', which is allocated to the process, but"
					+ " contains no data.", pcb.jobID, virtualAddress));
			return null;
		}
		
		//System.out.println(virtualAddress + ": " + ram.read(physAddr));
		
		return ram.read(physAddr);
	}
	
	/**
	 * Writes the given word into the given address for the process.
	 * @param virtualAddress The virtual address to write to.
	 * @param processID The ID of the process being written to.
	 * @param word The word being written.
	 * @return {@code true} if successful; {@code false} otherwise.
	 */
	public boolean write(int virtualAddress, PCB pcb, String word) {
		int physAddr = calcEffectiveAddress(virtualAddress, pcb);
		
		// if there is no frame for the given process at the v. address:
		if(physAddr == -1)
			return false;
		
		ram.write(physAddr, word);
		return true;
	}
	
	/**
	 * Allocates memory space for the process.
	 * @param pcb The PCB of the process being allocated.
	 * @return {@code true} if allocation was successful; {@code false}
	 * otherwise.
	 */
	public boolean allocate(PCB pcb) {
		int numPages = (pcb.calcJobSize() + PAGE_SIZE - 1) / PAGE_SIZE;
		
		if(freeFrames.size() < numPages) // if not enough pages to allocate
			return false;
		
		// allocate the pages, back-to-front:
		int lastPage = -1;
		for(int lcv = numPages - 1; lcv >= 0; lcv--) {
			int nextFrame = freeFrames.get(0);
			freeFrames.remove(0);
			Frame newFrame = new Frame(pcb.jobID, nextFrame, lastPage);
			lastPage = nextFrame;
			List<Frame >newFrameList = new LinkedList<Frame>();
			newFrameList.add(newFrame);
			frameTable.put(nextFrame, newFrameList);
		}
		
		// set pc and address index for job:
		pcb.setJobStart(lastPage * PAGE_SIZE);
		pcb.resetPC();
		
		return true; // successful allocation
	}
	
	/**
	 * Deallocates the memory reserved for the process with the given PCB.
	 * @param pcb The PCB of the process having its memory deallocated.
	 */
	public void deallocate(PCB pcb) {
		int nextPage = pcb.getJobStart() / PAGE_SIZE;
		while(nextPage != -1) {
			// remove frame from the frame table:
			Frame frame = getFrame(nextPage, pcb);
			List<Frame> frames = frameTable.get(nextPage);
			if(frames.size() > 1) // if other frames exist
				frames.remove(frame);
			else // if no other frames mapped to the virtual address
				frameTable.remove(nextPage);
			// add frame to free list:
			freeFrames.add(frame.frameNumber);
			// proceed to next page:
			nextPage = frame.nextPage;
		}
	}
	
	public int secureDMABufferIndex() {
		int ret = nextDMABufferIndex;
		
		// increment buffer index for next use:
		nextDMABufferIndex = nextDMABufferIndex + 1 % DMA_BUFFER_SPACE;
		
		// return secured index:
		return ret;
	}
	
	public String readFromDMABuffer(int index) {
		return ram.read(DMA_BUFFER_START + index);
	}
	
	public void writeToDMABuffer(int index, String datum) {
		ram.write(DMA_BUFFER_START + index, datum);
	}
	
	public String generateRAMDump() {
		String retStr = "";
		
		List<JobDump> jobDumps = new ArrayList<JobDump>();
		for(int lcv = 0; lcv < NUM_PAGES; lcv++) {
			List<Frame> frames = frameTable.get(lcv);
			for(Frame f : frames) {
				// find job dump for the process if it exists:
				JobDump dump = null;
				for(JobDump jd : jobDumps)
					if(jd.processID == f.processID) {
						dump = jd;
						break;
					}
				if(dump == null) { // if job dump does not already exist
					dump = new JobDump(f.processID);
					jobDumps.add(dump);
				}
				
				// fill-in dump data for this frame:
				
			}
		}
		
		return retStr;
	}
	
	/**
	 * Increments the given PC to the next address of the process.
	 * @param pcb The PCB of the process being executed.
	 * @param currentPC The current program counter in the CPU.
	 * @return The next virtual address that the program counter should point
	 * to.
	 */
	public int incrementPC(PCB pcb, int currentPC) {
		// do increment:
		currentPC += 1;
		
		// if normal increment:
		if(currentPC % PAGE_SIZE != 0)
			return currentPC;
		
		// if increment to new page:
		// find the current frame for the pc:
		Frame currentFrame = getFrame(calcPageNumber(currentPC - 1), pcb);
		
		if(currentFrame == null) // if the frame at the address does not exist
			return -1;
		
		// return the first address of the next page:
		return getFrame(currentFrame.nextPage, pcb).frameNumber * PAGE_SIZE;
	}
	
	/**
	 * Finds the physical address mapped to the given virtual address for the
	 * process.  Excessively complicated due to the use of a frame table.
	 * @param virtualAddress The virtual address to find the physical mapping
	 * for.
	 * @param pcb The PCB of the process being accessed in memory.
	 * @return The physical address mapped to the given virtual address or an
	 * error code.  An error code of {@code -1} indicates that the given pcb
	 * has no address assigned at the given virtual address, and an error code
	 * of {@code -2} indicates that the end of the program has been reached.
	 */
	private int calcEffectiveAddress(int virtualAddress, PCB pcb) {
		// get the frame of the page at the address for the given process
		Frame frame = getFrame(calcPageNumber(virtualAddress), pcb);
		if(frame == null) // if no frame found for the process at the address 
			return -1;
		
		// determine if end of program reached:
		if(frame.nextPage == -1 &&
				virtualAddress % PAGE_SIZE >= pcb.calcJobSize() % PAGE_SIZE)
			return -2;
		
		// return correct physical address
		return frame.frameNumber * PAGE_SIZE + virtualAddress % PAGE_SIZE;
	}
	
	private int calcPageNumber(int virtualAddress) {
		return virtualAddress / PAGE_SIZE;
	}
	
	/**
	 * Retrieves a frame from the frame table based on its page number and
	 * associated process.
	 * @param pageNumber The page number that the frame corresponds to.
	 * @param pcb The PCB of the process that the frame should be allocated to.
	 * @return The frame belonging to the given process with the given frame
	 * number, or {@code null} if no such frame exists.
	 */
	private Frame getFrame(int pageNumber, PCB pcb) {
		List<Frame> frames = frameTable.get(pageNumber);
		for(Frame f : frames)
			if(f.processID == pcb.jobID)
				return f;
		return null;
	}
	
	//
	// non-constituent inner types
	//
	
	private static class Frame {
		// instance data
		
		public final int processID;
		public final int frameNumber;
		public final int nextPage;
		
		// constructor
		
		public Frame(int processID, int frameNumber, int nextPage) {
			this.processID = processID;
			this.frameNumber = frameNumber;
			this.nextPage = nextPage;
		}
	}
	
	private static class JobDump {
		// instance data
		
		public final int processID;
		public List<String> instructions;
		public List<String> inputBuffer;
		public List<String> outputBuffer;
		public List<String> tempBuffer;
		
		// constructor
		
		public JobDump(int processID) {
			this.processID = processID;
		}
	}
}