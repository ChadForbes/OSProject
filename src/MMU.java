import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Class responsible for intercepting attempts to access RAM, translating
 * virtual addresses to physical addresses in the process, and managing the
 * distribution of memory to processes.
 */
public class MMU {
	//
	// static data
	//

	private static final int PAGE_SIZE = 64;
	private static final int NUM_PAGES = RAM.RAM_SIZE / PAGE_SIZE;

	//
	// instance data
	//

	private RAM ram;
	private Hashtable<Integer, List<Frame>> frameTable;
	private List<Integer> freeFrames;

	//
	// constructor
	//

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
	 * @param pcb The PCB of the process being read from.
	 * @return The word stored in memory at the virtual address for the
	 * process.
	 */
	public String read(int virtualAddress, PCB pcb) {
		int physAddr = calcEffectiveAddress(virtualAddress, pcb);

		// if there is no frame for the given process at the v. address:
		if(physAddr == -1)
			return null;

		return ram.read(physAddr);
	}

	/**
	 * Writes the given word into the given address for the process.
	 * @param virtualAddress The virtual address to write to.
	 * @param pcb The PCB of the program being written to.
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
		int numPages = (pcb.getJobSize() + PAGE_SIZE - 1) / PAGE_SIZE;

		// find pages to allocate:
		List<Integer> pages = new ArrayList<Integer>(numPages);
		for(int pageNum = 0; pageNum < NUM_PAGES; pageNum++) {
			if(frameTable.get(pageNum) == null)
				pages.add(pageNum);

			if(pages.size() == numPages)
				break;
		}
		if(pages.size() != numPages) // if not enough pages to allocate
			return false;

		// allocate the pages, back-to-front:
		int lastPage = -1;
		for(int lcv = numPages - 1; lcv >= 0; lcv--) {
			int nextFrame = freeFrames.get(0);
			freeFrames.remove(0);
			Frame newFrame = new Frame(pcb.getJobID(), nextFrame, lastPage);
			lastPage = pages.get(lcv);
			List<Frame >newFrameList = new LinkedList<Frame>();
			newFrameList.add(newFrame);
			frameTable.put(pages.get(lcv), newFrameList);
		}

		// set pc and address index for job:
		pcb.setJobMemoryIndex(lastPage * PAGE_SIZE);
		pcb.setProgramCounter(pcb.getJobMemoryIndex());

		return true; // successful allocation
	}

	/**
	 * Deallocates the memory reserved for the process with the given PCB.
	 * @param pcb The PCB of the process having its memory deallocated.
	 */
	public void deallocate(PCB pcb) {
		int nextPage = pcb.getJobMemoryIndex() / PAGE_SIZE;
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

	/**
	 * Increments the given PC to the next address of the process.
	 * @param pcb The PCB of the process being executed.
	 * @param currentPC The current program counter in the CPU.
	 * @return The next virtual address that the program counter should point
	 * to.
	 */
	public int incrementPC(PCB pcb, int currentPC) {
		// if normal increment:
		if(currentPC % PAGE_SIZE != 0)
			return currentPC + 1;

		// if increment to new page:
		List<Frame> frames = frameTable.get(calcPageNumber(currentPC));
		int nextPage = -1;
		for(Frame f : frames)
			if(f.processID == pcb.getJobID()) {
				nextPage = f.nextPage;
				break;
			}

		if(nextPage == -1) // if the frame does not exist
			return -1;

		// return the first address of the next page:
		return nextPage * PAGE_SIZE;
	}

	/**
	 * Finds the physical address mapped to the given virtual address for the
	 * process.
	 * @param virtualAddress The virtual address to find the physical mapping
	 * for.
	 * @param pcb The PCB of the process for which the physical address is
	 * being found.
	 * @return The physical address mapped to the given virtual address.
	 */
	private int calcEffectiveAddress(int virtualAddress, PCB pcb) {
		// search through all frames at the hashed page index for the one
		// with the correct process id:
		List<Frame> frames = frameTable.get(calcPageNumber(virtualAddress));
		if(frames == null) // if no frames at that index
			return -1;

		int physAddr = -1;
		for(Frame f : frames)
			if(f.processID == pcb.getJobID()) {
				physAddr = f.frameNumber * PAGE_SIZE;
				break;
			}

		// if no frame found for the process in the given virtual address:
		if(physAddr == -1)
			return -1;

		physAddr += virtualAddress % PAGE_SIZE;
		return physAddr;
	}

	private int calcPageNumber(int virtualAddress) {
		return virtualAddress / PAGE_SIZE;
	}

	private Frame getFrame(int pageNumber, PCB pcb) {
		List<Frame> frames = frameTable.get(pageNumber);
		for(Frame f : frames)
			if(f.processID == pcb.getJobID())
				return f;
		return null;
	}

	//
	// non-constituent inner type
	//

	private class Frame {
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
}
