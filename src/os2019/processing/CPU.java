package os2019.processing;

import os2019.Driver;
import os2019.util.Conversions;

public class CPU implements Runnable {
	//
	// static data
	//
	
	public static final int NUM_REGISTERS = 16;
	
	//
	// instance data
	//
	
	public final int cpuID;
	
	PCB loadedPCB;
	int programCounter;
	/**<p>Register 0 is the accumulator register.  It will be read into or
	 * written from when performing RD/WR instructions.
	 * <p>Register 1 is the zero-register and will always contain the value
	 * {@code 0}. */
	int[] registers = new int[NUM_REGISTERS];
	int inputBuffer;
	int outputBuffer;
	int jobBaseAddress;
	int priorityNum;
	int tempBuffer;
	int jobInstructionCount;
	String[] cache;
	
	private boolean idle;
	//private int dmaAddress;
	private boolean terminatedSuccessfully;
	private boolean halted;
	
	/** Source Register 1 for arithmetic-type instructions. */
	private int sReg1;
	/** Source Register 2 for arithmetic-type instructions. */
	private int sReg2;
	/** Destination Register for arithmetic- and immediate/branch-type
	 * instructions. */
	private int destReg;
	/** Base Register for immediate/branch-type instructions. */
	private int baseReg;
	/** Data/Address Field for immediate/branch-, unconditional jump-, and
	 * I/O-type instructions. */
	private int dAddress;
	/** Register 1 for I/O-type instructions. */
	private int reg1;
	/** Register 2 for I/O-type instructions. */
	private int reg2;
	
	//
	// constructor
	//
	
	public CPU(int cpuID) {
		this.cpuID = cpuID;
		idle = true;
		terminatedSuccessfully = false;
		halted = false;
		//dmaAddress = -1;
	}
	
	//
	// simple accessors
	//
	
	//@formatter:off
	public PCB getLoadedPCB() { return loadedPCB; }
	public boolean terminatedSuccessfully() { return terminatedSuccessfully; }
	public boolean idling() { return idle; }
	//@formatter:on
	
	//
	// simple mutator
	//
	
	//@formatter:off
	public void halt() { halted = true; }
	//@formatter:on
	
	//
	// other instance methods
	//
	
	/**
	 * Runs this cpu until it is halted from outside or until it reaches a halt
	 * instruction.
	 * <p>The CPU will wait {@code CLOCK_PERIOD_MILLIS} between each
	 * compute-only cycle.
	 * @see Runnable#run()
	 * @see Driver#CLOCK_PERIOD_MILLIS
	 * @see CPU#doCycle()
	 */
	@Override
	public void run() {
		idle = false;
		halted = false;
		terminatedSuccessfully = false;
		
		while(!halted) {
			doCycle();
			
			try {
				Thread.sleep(Driver.CLOCK_PERIOD_MILLIS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		idle = true;
	}
	
	/**
	 * Finalizes loading data into this cpu by setting some flags.
	 */
	void finishLoading() {
		halted = false;
		terminatedSuccessfully = false;
	}
	
	/**
	 * Fetches the instruction from the cache that corresponds with the program
	 * counter.
	 * @return A String representation of the instruction.
	 * @see CPU#programCounter
	 */
	private String fetch() {
		String instruction = readFromCache(
				programCounter - loadedPCB.getJobStart());
		return instruction;
	}
	
	private int decode(String instruction) {
		// parse instruction type and opcode:
		String binInstruction = Conversions.hexStringToBinaryString(
				instruction.substring(2)); // ignores preceding '0x'
		
		int instructionType =
				Integer.parseInt(binInstruction.substring(0, 2), 2);
		int opCode = Integer.parseInt(binInstruction.substring(2, 8), 2);
		
		// fill related registers with appropriate data:
		switch(instructionType) {
		case 0b00:
			sReg1 = Integer.parseInt(binInstruction.substring(8, 12), 2);
			sReg2 = Integer.parseInt(binInstruction.substring(12, 16), 2);
			destReg = Integer.parseInt(binInstruction.substring(16, 20), 2);
			break;
		case 0b01:
			baseReg = Integer.parseInt(binInstruction.substring(8, 12), 2);
			destReg = Integer.parseInt(binInstruction.substring(12,16), 2);
			dAddress = Integer.parseInt(binInstruction.substring(16), 2);
			break;
		case 0b10:
			dAddress = Integer.parseInt(binInstruction.substring(8), 2);
			break;
		case 0b11:
			reg1 = Integer.parseInt(binInstruction.substring(8, 12), 2);
			reg2 = Integer.parseInt(binInstruction.substring(12, 16), 2);
			dAddress = Integer.parseInt(binInstruction.substring(16), 2);
			break;
		default:
			System.err.println(String.format("Unexpected instruction type "
					+ "in job '%1$d': '%2$d'", loadedPCB.jobID,
					instructionType));
			break;
		}
		return opCode;
	}
	
	private void execute(int opCode) {
		boolean jumped = false;
		switch(opCode) {
		case 0x00: // RD; type 0b11, I/O
			// determine offset of contents to read from:
			int readOffset;
			if(reg2 > 0) // if source register provided
				readOffset = registers[reg2];
			else // if no source register provided
				readOffset = dAddress;
			
			// read contents into specified accumulator:
			registers[reg1] = Conversions.hexToDecimal(
					readFromCache(readOffset / 4).substring(2));
			break;
		case 0x01: // WR; type 0b11, I/O
			// determine offset of write buffer:
			int writeOffset;
			if(reg2 > 0) // if destination register provided
				writeOffset = registers[reg2];
			else // if no destination register provided
				writeOffset = dAddress;
			
			// write contents to cache:
			writeToCache(writeOffset / 4,
					"0x" + Conversions.decimalToHex(registers[reg1]));
			break;
		case 0x02: // ST; type 0b01, immediate
			final int datum = registers[baseReg];
			int storeOffset = dAddress;
			if(baseReg > 0) // if source register provided
				storeOffset += registers[reg2];
			//else // if no source register provided
			//	storeOffset += tempAddress;
			
			//registerSpace[destReg] = Conversions.hexToDecimal(
			//		readFromCache(storeOffset / 4).substring(2));
			writeToCache(storeOffset / 4,
					"0x" + Conversions.decimalToHex(datum));
			break;
		case 0x03: // LW; type 0b01, immediate
			int loadOffset = dAddress;
			if(baseReg > 0) // if source register provided
				loadOffset += registers[reg2];
			//else // if no source register provided
			//	loadOffset += tempAddress;
			
			registers[destReg] = Conversions.hexToDecimal(
					readFromCache(loadOffset / 4).substring(2));
			break;
		case 0x04: // MOV; type 0b00, arithmetic
			registers[destReg] = registers[baseReg];
			break;
		case 0x05: // ADD; type 0b00, arithmetic
			registers[destReg] = registers[sReg1] + registers[sReg2];
			break;
		case 0x06: // SUB ; type 0b00, arithmetic
			registers[destReg] = registers[sReg1] - registers[sReg2];
			break;
		case 0x07: // MUL; type 0b00, arithmetic
			registers[destReg] = registers[sReg1] * registers[sReg2];
			break;
		case 0x08: // DIV; type 0b00, arithmetic
			registers[destReg] = registers[sReg1] / registers[sReg2];
			break;
		case 0x09: // AND; type 0b00, arithmetic
			registers[destReg] = registers[sReg1] & registers[sReg2];
			break;
		case 0x0A: // OR; type 0b00, arithmetic
			registers[destReg] = registers[sReg1] | registers[sReg2];
			break;
		case 0x0B: // MOVI; type 0b01, immediate
			registers[destReg] = dAddress;
			break;
		case 0x0C: // ADDI; type 0b01, immediate
			registers[destReg] += dAddress;
			break;
		case 0x0D: // MULI; type 0b01, immediate
			registers[destReg] *= dAddress;
			break;
		case 0x0E: // DIVI; type 0b01, immediate
			registers[destReg] /= dAddress;
			break;
		case 0x0F: // LDI; type 0b01, immediate
			registers[destReg] = dAddress;
			break;
		case 0x10: // SLT; type 0b00, arithmetic
			if(registers[sReg1] < registers[sReg2])
				registers[destReg] = 1;
			else
				registers[destReg] = 0;
			break;
		case 0x11: // SLTI; type 0b01, immediate
			if(registers[sReg1] < dAddress)
				registers[destReg] = 1;
			else
				registers[destReg] = 0;
			break;
		case 0x12: // HLT; type 0b10, unconditional jump (?)
			terminatedSuccessfully = true;
			halt();
			break;
		case 0x13: // NOP; 'no type'
			// no operation; increment PC only
			break;
		case 0x14: // JMP; type 0b10, unconditional jump
			// unconditional jump
			programCounter = dAddress/4;
			jumped = true;
			break;
		case 0x15: // BEQ; type 0b01, conditional jump
			// conditional jump (base == dest):
			if(registers[baseReg] == registers[destReg]) {
				programCounter = loadedPCB.getVirtualAddressAtOffset(
						//(registerSpace[BReg] + tempAddress) / 4);
						dAddress / 4);
				jumped = true;
			}
			break;
		case 0x16: // BNE; type 0b01, conditional jump
			// conditional jump (base != dest):
			if(registers[baseReg] != registers[destReg]) {
				programCounter = loadedPCB.getVirtualAddressAtOffset(
						//(registerSpace[BReg] + tempAddress) / 4);
						dAddress / 4);
				jumped = true;
			}
			break;
		case 0x17: // BEZ; type 0b01, conditional jump
			if(registers[baseReg] == 0) { // conditional jump (base == 0)
				programCounter = loadedPCB.getVirtualAddressAtOffset(
						dAddress / 4);
				jumped = true;
			}
			break;
		case 0x18: // BNZ; type 0b01, conditional jump
			if(registers[baseReg] != 0) { // conditional jump (base != 0)
				programCounter = loadedPCB.getVirtualAddressAtOffset(
						dAddress / 4);
				jumped = true;
			}
			break;
		case 0x19: // BGZ; type 0b01, conditional jump
			if(registers[baseReg] > 0) { // conditional jump (base > 0)
				programCounter = loadedPCB.getVirtualAddressAtOffset(
						dAddress / 4);
				jumped = true;
			}
			break;
		case 0x1A: // BLZ; type 0b01, conditional jump
			if(registers[baseReg] < 0) { // conditional jump (base < 0)
				programCounter = loadedPCB.getVirtualAddressAtOffset(
						dAddress / 4);
				jumped = true;
			}
			break;
		default:
			System.out.println("System error: invalid operation");
			break;
		}
		
		// increment PC if a jump did not occur this cycle:
		if(!jumped)
			programCounter = Driver.getInstance().mmu.incrementPC(
					loadedPCB, programCounter);
	}
	
	/**
	 * Performs one computer-only cycle (fetch, decode, and execute) and
	 * increments the program counter.
	 * @see CPU#fetch()
	 * @see CPU#decode(String)
	 * @see CPU#execute(int)
	 */
	private void doCycle() {
		System.out.println(programCounter);//TODO: remove
		String instruction = fetch();
		int opCode = decode(instruction);
		execute(opCode);
	}
	
	/**
	 * Reads a word from the cache.
	 * @param offsetFromStart The offset that the word is in the address space
	 * from the start of the loaded program.
	 * @return The word at the address mapped to the given offset from the
	 * start of the current program.
	 */
	private String readFromCache(int offsetFromStart) {
		return cache[offsetFromStart];
	}
	
	/**
	 * Writes a word into the cache of this CPU.
	 * @param offsetFromStart The offset that the word to be written to is in
	 * the address space from the start of the loaded program.
	 * @param datum The contents to be written to the space in the cache.
	 */
	private void writeToCache(int offsetFromStart, String datum) {
		cache[offsetFromStart] = datum;
	}
	
	//
	// non-constituent inner type
	//
	
	//private static class Arguments {
	//	// instance data
	//	
	//	private final int register1;
	//	private final int register2;
	//	private final int dAddress;
	//	
	//	// constructors
	//	
	//	
	//}
}
