package os2019.memory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import os2019.Driver;

public class DMAManager {
	//
	// instance data
	//
	
	private final ExecutorService channelThreadPool;
	
	//
	// constructor
	//
	
	public DMAManager() {
		channelThreadPool = Executors.newCachedThreadPool();
	}
	
	public DMAManager(int maxDMAChannels) {
		channelThreadPool = Executors.newFixedThreadPool(maxDMAChannels);
	}
	
	//
	// instance methods
	//
	
	public int readDisk(int address) {
		final int dmaBufferIndex = Driver.getInstance().mmu
				.secureDMABufferIndex();
		
		channelThreadPool.execute(new DiskReaderChannel(address,
				dmaBufferIndex));
		
		return dmaBufferIndex;
	}
	
	public int writeDisk(int address, String datum) {
		final int dmaBufferIndex = Driver.getInstance().mmu
				.secureDMABufferIndex();
		
		channelThreadPool.execute(new DiskWriterChannel(address,
				dmaBufferIndex, datum));
		
		return dmaBufferIndex;
	}
}