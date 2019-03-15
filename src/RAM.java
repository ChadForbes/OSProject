public class RAM {
	//
	// static data
	//
	
	public static final int RAM_SIZE = 1024;
	
	//
	// instance data
	//
	
	private String[] contents;
	
	//
	// constructor
	//
	
	public RAM() {
		contents = new String[RAM_SIZE];
	}
	
	//
	// instance methods
	//
	
	public String read(int physicalAddress) {
		return contents[physicalAddress];
	}
	
	public void write(int physicalAddress, String word) {
		contents[physicalAddress] = word;
	}
}
