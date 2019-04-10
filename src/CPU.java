// Author: Ryan Wheeler

public class CPU implements Runnable
{
	// global instruction variables
	public int opCode;
	public int instructionType;

	// global register variables
	public int SReg1;
	public int SReg2;
	public int destReg;
	public int BReg;
	public int address;
	public int tempAddress;
	public int reg1;
	public int reg2;

	// global addressing and buffer variables
	public int inputBuffer;
	public int outputBuffer;
	public int jobBaseAddress;
	public int priorityNum;
	public int tempBuffer;
	public int jobInstructionCount;
	public int cacheSize;

	// initialize jump to false
	public boolean jump = false;
	// public int jobCounter;

	// set cpu number and thread ID
	public int CPUNum = 1;
	private long threadID;

	/**
	 * flag[0] = idle flag[1] = terminate current queue flag[2] = unload process
	 * flag[3] = load, a job has been loaded flag[4] = running
	 */
	public boolean[] flags = { true, false, false, false, false };

	private int jobNumber;

	public int programCounter;
	public String[] cache;

	public int[] registerSpace = new int[16];

	// method that fetches next instruction
	public String fetch(int progCounter) 
	{
		String instruction = cache[progCounter];
		return instruction;
	}

	/*
	 * method that accepts next instruction and decodes it a method call to the
	 * conversions class is necessary to convert from hex to binary method will then
	 * use switch case statements to determine the instruction type the method will
	 * return the opcode of the processed instruction to be used in execute() method
	 */
	public int decode(String instruction) 
	{
		String binInString = Conversions.hexStringToBinaryString(instruction.substring(2));
		String tempInString = binInString;

		instructionType = Integer.parseInt(tempInString.substring(0, 2));
		opCode = Conversions.binaryStringToLiteralInteger(tempInString.substring(2, 8));

		switch (instructionType)
		{
		case 00: {
			SReg1 = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			SReg2 = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			destReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(16, 20));
			break;
		}
		case 01: 
		{
			BReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			destReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			tempAddress = Conversions.binaryStringToLiteralInteger(tempInString.substring(16));
			break;
		}
		case 10: 
		{
			tempAddress = Conversions.binaryStringToLiteralInteger(tempInString.substring(8));
			break;
		}
		case 11: 
		{
			reg1 = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			reg2 = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			tempAddress = Conversions.binaryStringToLiteralInteger(tempInString.substring(16));
			break;
		}
		default: 
		{
			System.out.println("EXCEPTION: Invalid instruction type");
		}
		}
		return opCode;
	}

	/*
	 * method that accepts an instruction as a parameter in hex method calls to the
	 * conversions class are necessary to convert from hex to binary the method will
	 * then use switch case statements to carry out the proper operations based on
	 * the input parameter instruction
	 */
	public void execute(int instruction)
	{
		int opCode = instruction;

		switch (opCode)
		{
		// read
		case 0:
		{
			if (reg2 > 0) 
			{
				registerSpace[reg1] = Conversions.hexToDecimal(cache[registerSpace[reg2] / 4].substring(2));
			} else 
			{
				registerSpace[reg1] = Conversions.hexToDecimal(cache[tempAddress / 4].substring(2));
			}
			break;
		}
		// write
		case 1: 
		{
			if (reg2 > 0)
			{
				registerSpace[reg2] = registerSpace[reg1];
			} else 
			{
				cache[tempAddress / 4] = "0x" + Conversions.decimalToHex(registerSpace[reg1]);
			}

			break;
		}
		// store
		case 2:
		{
			cache[registerSpace[destReg] / 4] = "0x" + Conversions.decimalToHex(registerSpace[BReg]);
			break;
		}
		// load
		case 3: 
		{
			registerSpace[destReg] = Conversions
					.hexToDecimal(cache[(registerSpace[BReg] / 4) + tempAddress].substring(2));

			break;
		}
		// move
		case 4: 
		{
			registerSpace[destReg] = registerSpace[BReg];
			break;
		}
		// add
		case 5: 
		{
			registerSpace[destReg] = registerSpace[SReg1];
			registerSpace[destReg] += registerSpace[SReg2];

			break;
		}
		// subtract
		case 6: 
		{
			registerSpace[destReg] = registerSpace[SReg1];
			registerSpace[destReg] = registerSpace[destReg] - registerSpace[SReg2];

			break;
		}
		// multiply
		case 7: 
		{
			registerSpace[destReg] = registerSpace[SReg1] * registerSpace[SReg2];

			break;
		}
		// divide
		case 8: 
		{
			registerSpace[destReg] = registerSpace[SReg1] / registerSpace[SReg2];

			break;
		}
		// and operator
		case 9: 
		{
			registerSpace[destReg] = registerSpace[SReg1] & registerSpace[SReg2];
			break;
		}
		// or operator
		case 10: 
		{
			registerSpace[destReg] = registerSpace[SReg1] ^ registerSpace[SReg2];

			break;
		}
		// move indirect
		case 11: 
		{
			registerSpace[destReg] = tempAddress;
			break;
		}
		// add indirect
		case 12: 
		{
			registerSpace[destReg] += tempAddress;
			break;
		}
		// multiply indirect
		case 13:
		{
			registerSpace[destReg] *= tempAddress;
			break;
		}
		// divide indirect
		case 14: 
		{
			registerSpace[destReg] /= tempAddress;
			break;
		}
		// load indirect
		case 15: 
		{
			registerSpace[destReg] = (tempAddress);
			break;
		}
		// SLT
		case 16: 
		{
			if (registerSpace[SReg1] < registerSpace[SReg2]) 
			{
				registerSpace[destReg] = 1;
			} else 
			{
				registerSpace[destReg] = 0;
			}
			break;
		}
		// SLTI
		case 17:
		{
			if (registerSpace[SReg1] < (tempAddress / 4))
			{
				registerSpace[destReg] = 1;
			} else 
			{
				registerSpace[destReg] = 0;
			}
			break;
		}
		// halt
		case 18: 
		{
			programCounter = jobInstructionCount;
			break;
		}
		// NOP
		case 19: 
		{
			// do nothing
			break;
		}
		// jump
		case 20: 
		{
			programCounter = tempAddress / 4;
			jump = true;
			break;
		}
		// BEQ (branch if equal)
		case 21: 
		{
			if (registerSpace[BReg] == registerSpace[destReg]) 
			{
				programCounter = tempAddress / 4;
				jump = true;
			} else 
			{
				// do not branch
			}
			break;
		}
		// BNE (branch if not equal)
		case 22: 
		{
			if (registerSpace[BReg] != registerSpace[destReg]) 
			{
				// branch
				programCounter = tempAddress / 4;
				jump = true;

			} else 
			{
				// do not branch
			}
			break;
		}
		// BEZ (branch if equal 0)
		case 23: 
		{
			if (registerSpace[BReg] == 0)
			{
				// branch
				programCounter = tempAddress / 4;
				jump = true;
			} else 
			{
				// do not branch
			}
			break;
		}
		// BNZ (branch if not equal 0)
		case 24: 
		{
			if (registerSpace[BReg] != 0) 
			{
				programCounter = tempAddress / 4;
				jump = true;
			} else 
			{
			}
			break;
		}
		// BGZ (branch if greater than 0)
		case 25: {
			if (registerSpace[BReg] > 0) 
			{
				// branch
				programCounter = tempAddress / 4;
				jump = true;
			} else 
			{
				// do not branch
			}
			break;
		}
		// BLZ (branch if less than 0)
		case 26: 
		{
			if (registerSpace[BReg] < 0) 
			{
				// branch
				programCounter = tempAddress / 4;
				jump = true;
			} else 
			{
				// do not branch
			}
			break;
		}
		// default case to prevent dropping through
		default: 
		{
			System.out.println("System error: invalid operation");
		}
		}
	}

	// method the calculates the effective address of an address given its offset
	public int effectiveAddress(int i, long a) 
	{
		return registerSpace[i] + (int) a;
	}

	// method the puts a process to sleep
	private void sleep() 
	{
		try {
			Thread.sleep(Driver.sleepTimeMs);
		} catch (InterruptedException ex) 
		{

		}
	}

	// method that puts a process to sleep for a certain amount of time
	private void sleepTimed(int multiplier) 
	{
		try {
			Thread.sleep(Driver.sleepTimeMs * multiplier);
		} catch (InterruptedException ex) 
		{

		}
	}

	/*
	 * method that will call the fetch(), decode(), and execute() methods print
	 * statements included to tell the user what the CPU is doing at that time if a
	 * jump in necessary this method will handle it after execution the CPU is put
	 * back to sleep and the flags are reset accordingly
	 */
	public void run() 
	{
		// Driver.jobMetricses[currentJobNumber()-1].setStartRunTime(System.currentTimeMillis());
		// Driver.updateJobMetrics(Driver.jobMetricses[currentJobNumber()-1]);
		printFlags("PRE RUNNING");
		setRunning(true);
		threadID = Thread.currentThread().getId();
		// Driver.jobsRan.add("\nRUNNING JOB: " + currentJobNumber() + "\tON THREAD: " +
		// threadID);
		System.out.println("\nRUNNING JOB: " + currentJobNumber() + "\tON THREAD: " + threadID);
		while (programCounter < jobInstructionCount) 
		{
			String instruction = fetch(programCounter);
			int opCode = decode(instruction);
			execute(opCode);

			if (!jump) 
			{
				programCounter++;
			} else 
			{
				jump = false;
			}

			sleep();
			// updateMetrics(CPUMetrics.CPU_STATE.RUNNING, instruction);
		}

		setIdleFlag(true);
		setTerminateFlag(true);
		setUnload(true);
		setRunning(false);
		// Driver.jobMetricses[currentJobNumber()-1].setEndRunTime(System.currentTimeMillis());
		// Driver.updateJobMetrics(Driver.jobMetricses[currentJobNumber()-1]);
		printFlags("POST RUNNING");
	}

	// mutator for idle flag
	private void setIdleFlag(boolean val) 
	{
		flags[0] = val;
	}

	// mutator for terminate flag
	private void setTerminateFlag(boolean val) 
	{
		flags[1] = val;
	}

	// mutator for unload flag
	private void setUnload(boolean val) 
	{
		flags[2] = val;
	}

	// accessor for unload flag
	public boolean shouldUnload() 
	{
		return flags[2];
	}

	// accessor for idle flag
	public boolean isIdle() 
	{
		return flags[0];
	}

	// accessor for load flag
	public boolean isJobLoaded() 
	{
		return flags[3];
	}

	// accessor for load flag
	public void setJobLoaded(boolean val) 
	{
		flags[3] = val;
	}

	// accessor for terminate flag
	public boolean shouldTerminate() 
	{
		return flags[1];
	}

	// accessor for the current job number
	public int currentJobNumber() 
	{
		return jobNumber;
	}

	// accessor for the running flag
	public boolean isRunning()
	{
		return flags[4];
	}

	// mutator for running flag
	public void setRunning(boolean val) 
	{
		flags[4] = val;
	}

	/*
	 * method that will print all the current states of the flags in the flag[]
	 * array helpful to use when manually debugging the CPU
	 */
	public void printFlags(String type)
	{
		System.out.println(String.format(
				"FLAGS: JOB: %s CPU_THREAD: %s TYPE: %s idle:%s terminate: %s unload: %s load: %s running: %s",
				currentJobNumber(), threadID, type, isIdle(), shouldTerminate(), shouldUnload(), isJobLoaded(),
				isRunning()));
	}

	/*
	 * method that interacts with the PCB class to obtain pertinent info about the
	 * process to be run
	 */
	public void load(PCB job) 
	{
		programCounter = job.getProgramCounter();
		jobNumber = job.getJobID();
		priorityNum = job.getJobPriority();
		inputBuffer = job.getInputBuffer();
		outputBuffer = job.getOutputBuffer();
		jobBaseAddress = job.getJobMemoryIndex();
		tempBuffer = job.getTempBuffer();
		jobInstructionCount = job.getJobSize();
		cacheSize = inputBuffer + outputBuffer + tempBuffer + jobInstructionCount;

		/*
		 * debug print cache values 
		 * cache = new String[cacheSize]; for (int i = 0; i < cacheSize; i++) 
		 * { 
		 * cache[i] = read(i); 
		 * System.out.println("cache[" + i + "]" + cache[i]); 
		 * }
		 */
	}

	// method that reads and returns the data at a given address
	public String read(int address) 
	{
		return MMU2.read(address);
	}
}
