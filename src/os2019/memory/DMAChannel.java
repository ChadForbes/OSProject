package os2019.memory;

abstract class DMAChannel implements Runnable {
	//
	// instance data
	//
	
	private final int wordAddress;
	private final int dmaBufferIndex;
	
	//
	// constructor
	//
	
	public DMAChannel(int wordAddress, int dmaBufferIndex) {
		this.wordAddress = wordAddress;
		this.dmaBufferIndex = dmaBufferIndex;
	}
	
	//
	// instance method
	//
	
	protected int getWordAddress() { return wordAddress; }
	protected int getDMABufferIndex() { return dmaBufferIndex; }
}