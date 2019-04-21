package os2019.memory;

import os2019.Driver;

public class DiskWriterChannel extends DMAChannel {
	//
	// instance data
	//
	
	private final String datum;
	
	//
	// constructor
	//
	
	public DiskWriterChannel(int wordAddress, int dmaBufferIndex,
			String datum) {
		super(wordAddress, dmaBufferIndex);
		this.datum = datum;
	}
	
	//
	// instance method
	//
	
	@Override
	public void run() {
		String datum = Driver.getInstance().disk.read(getWordAddress());
		Driver.getInstance().mmu.writeToDMABuffer(getDMABufferIndex(), datum);
	}
}