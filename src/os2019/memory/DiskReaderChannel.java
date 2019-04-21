package os2019.memory;

import os2019.Driver;

class DiskReaderChannel extends DMAChannel {
	//
	// constructor
	//
	
	public DiskReaderChannel(int wordAddress, int dmaBufferIndex) {
		super(wordAddress, dmaBufferIndex);
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