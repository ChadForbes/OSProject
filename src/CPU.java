
public class CPU implements Runnable
{
	public int opCode;
	public int instructionType;
	
	public int SReg1;
	public int SReg2;
	public int destReg;
	public int BReg;
	public int address;
	public int tempAddress;
	public int reg1;
	public int reg2;
	
	public int inputBuffer;
	public int outputBuffer;
	public int jobBaseAddress;
	public int priorityNum;
	public int tempBuffer;
	public int jobInstructionCount;
	public int cacheSize;
	
	public boolean jump = false;
	public int jobCounter;
	
	public int CPUNum = 1;
	private long threadID;
	
    /**
     *flag[0] = idle
     *flag[1] = terminate queue
     *flag[2] = unload
     *flag[3] = loaded; a job has been loaded
     *flag[4] = running
     */
	
	public boolean[] flags = {true, false, false, false, false};

	private int jobNumber;
	
	public int programCounter;
	public String[] cache;
	
	public int[] registerSpace = new int[16];
	
	public String fetch(int progCounter)
	{
		String instruction = cache[progCounter];
		return instruction;
	}
	
	public int decode(String instruction) 
	{
		String binInString = Conversions.hexStringToBinaryString(instruction);
		String tempInString = binInString;
		
		instructionType = Conversions.binaryStringToLiteralInteger(tempInString.substring(0, 2));
		opCode = Conversions.binaryStringToLiteralInteger(tempInString.substring(2, 8));
		
		switch(instructionType) 
		{
		case 00:
		{
			SReg1 = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			SReg2 = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			destReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(16, 20));
			break;
		}
		case 01:
		{
			BReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			destReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			address = Conversions.binaryStringToLiteralInteger(tempInString.substring(16));
			break;
		}
		case 10:
		{
			address = Conversions.binaryStringToLiteralInteger(tempInString.substring(8));
			break;
		}
		case 11:
		{
			reg1 = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			reg2 = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			address = Conversions.binaryStringToLiteralInteger(tempInString.substring(16));
			break;
		}
		default:
		{
			System.out.println("EXCEPTION: Invalid instruction type");
		}
		}
		return opCode;
	}
	
	public void execute(int instruction) 
	{
		int opCode = instruction;
		
		switch(opCode) 
		{
		// read
		case 0:
		{
			if (reg2 > 0) 
			{
				registerSpace[reg1] = Conversions.hexToDecimal(cache[registerSpace[reg2]/4]);
			}
			else 
			{
				registerSpace[reg1] = Conversions.hexToDecimal(cache[tempAddress/4].substring(2));
			}
			break;
		}
		// write
		case 1:
		{
			if(reg2 > 0)
            {
                registerSpace[reg2] = registerSpace[reg1];
            }
            else
            {
                cache[tempAddress/4] = "0x" + Conversions.decimalToHex(registerSpace[reg1]);
            }

            //Driver.jobMetricses[currentJobNumber()-1].ios++;

			break;
		}
		// store
		case 2:
		{
			cache[registerSpace[destReg]/4] = "0x" + Conversions.decimalToHex(registerSpace[BReg]);		
			break;
		}
		// load
		case 3: 
        {
            registerSpace[destReg] = Conversions.hexToDecimal(cache[(registerSpace[BReg]/4) + tempAddress].substring(2));

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
        	registerSpace[destReg] = registerSpace[destReg]-registerSpace[SReg2];

            break;
        }
        // multiply
        case 7: 
        {
        	registerSpace[destReg] = registerSpace[SReg1]*registerSpace[SReg2];

            break;
        }
        // divide
        case 8: 
        {
        	registerSpace[destReg] = registerSpace[SReg1]/registerSpace[SReg2];

            break;
        }
        // and operator
        case 9: 
        {
        	registerSpace[destReg] = registerSpace[SReg1]&registerSpace[SReg2];
            break;
        }
        // or operator
        case 10:    
        {
        	registerSpace[destReg] = registerSpace[SReg1]^registerSpace[SReg2];

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
            if(registerSpace[SReg1] < registerSpace[SReg2])
            {
            	registerSpace[destReg] = 1;
            }
            else
            {
            	registerSpace[destReg] = 0;
            }
            break;
        }
        // SLTI
        case 17:   
        {
            if(registerSpace[SReg1] < (tempAddress/4))
            {
            	registerSpace[destReg] = 1;
            }
            else
            {
            	registerSpace[destReg] = 0;
            }
            break;
        }
        // halt
        case 18:    
        {
            programCounter = jobCounter;
            break;
        }
        // NOP
        case 19:    
        {
            //do nothing
            break;
        }
        // jump
        case 20:    
        {
            programCounter = tempAddress/4;
            jump = true;
            break;
        }
        // BEQ
        case 21:    
        {
            if(registerSpace[BReg] == registerSpace[destReg])
            {
                programCounter = tempAddress/4;
                jump = true;
            }
            else
            {
            	// do not branch
            }
            break;
        }
        // BNE
        case 22: 
        {
            if(registerSpace[BReg] != registerSpace[destReg])
            {
                //branch
                programCounter = tempAddress/4;
                jump = true;

            }
            else
            {
            	// do not branch
            }
            break;
        }
        // BEZ
        case 23: 
        {
            if(registerSpace[BReg] == 0)
            {
                //branch
                programCounter = tempAddress/4;
                jump = true;
            }
            else
            {
            	// do not branch
            }
            break;
        }
        // BNZ
        case 24: 
        {
            if(registerSpace[BReg] != 0)
            {
                programCounter = tempAddress/4;
                jump = true;
            }
            else
            {
            }
            break;
        }
        // BGZ
        case 25:    
        {
            if(registerSpace[BReg] > 0)
            {
                //branch
                programCounter = tempAddress/4;
                jump = true;
            }
            else
            {
            	// do not branch
            }
            break;
        }
        // BLZ
        case 26:   
        {
            if (registerSpace[BReg] < 0) 
            {
                //branch
                programCounter = tempAddress / 4;
                jump = true;
            }
            else 
            {
            	// do not branch
            }
            break;
        }
        default:
        {
        	 System.out.println("System error: invalid operation");
        }
		}		
	}
	private void sleep()
	{	    
        try
        {
            Thread.sleep(Driver.sleepTimeMs);
        }
        catch (InterruptedException ex)
        {

        }
	}
	private void sleepTimed(int multiplier)
	{	    
        try
        {
            Thread.sleep(Driver.sleepTimeMs * multiplier);
        }
        catch (InterruptedException ex)
        {

        }
	}
	
    public void run()
    {
        Driver.jobMetricses[currentJobNumber()-1].setStartRunTime(System.currentTimeMillis());
        Driver.updateJobMetrics(Driver.jobMetricses[currentJobNumber()-1]);
        printFlags("PRE RUNNING");
        setRunning(true);
        threadID = Thread.currentThread().getId();
        Driver.jobsRan.add("\nRUNNING JOB: " + currentJobNumber() + "\tON THREAD: " + threadID);
        System.out.println("\nRUNNING JOB: " + currentJobNumber() + "\tON THREAD: " + threadID);
        while(programCounter < jobCounter)
        {
            String instruction = fetch(programCounter);
            int opCode = decode(instruction);
            execute(opCode);

            if(!jump) {
                programCounter++;
            }
            else {
                jump = false;
            }

            sleep();
            updateMetrics(CPUMetrics.CPU_STATE.RUNNING, instruction);
        }

        setIdleFlag(true);
        setTerminateFlag(true);
        setUnload(true);
        setRunning(false);
        Driver.jobMetricses[currentJobNumber()-1].setEndRunTime(System.currentTimeMillis());
        Driver.updateJobMetrics(Driver.jobMetricses[currentJobNumber()-1]);
        printFlags("POST RUNNING");
    }
	private void setIdleFlag(boolean val)
    {
        flags[0] = val;
    }

    private void setTerminateFlag(boolean val)
    {
        flags[1] = val;
    }

    private void setUnload(boolean val)
    {
        flags[2] = val;
    }

    public boolean shouldUnload()
    {
        return flags[2];
    }
    public boolean isIdle()
    {
        return flags[0];
    }

    public boolean isJobLoaded()
    {
        return flags[3];
    }

    public void setJobLoaded(boolean val)
    {
        flags[3] = val;
    }

    public boolean shouldTerminate()
    {
        return flags[1];
    }

    public int currentJobNumber()
    {
        return jobNumber;
    }
    public boolean isRunning()
    {
        return flags[4];
    }

    public void setRunning(boolean val)
    {
        flags[4] = val;
    }

    /**
     * great function to use when manually debugging the CPU through logs.
     *
     * @param type
     */
    public void printFlags(String type)
    {
        System.out.println(String.format("FLAGS: JOB: %s CPU_THREAD: %s TYPE: %s idle:%s terminate: %s unload: %s load: %s running: %s",
                currentJobNumber(),
                threadID,
                type,
                isIdle(),
                shouldTerminate(),
                shouldUnload(),
                isJobLoaded(),
                isRunning()));
    }

    public void load(PCB job) 
    {
    	jobNumber = job.getJobNumber();
    	priorityNum = job.getJobPriority();
    	inputBuffer = job.getInputBuffer();
    	outputBuffer = job.getOutputBuffer();
    	jobBaseAddress = job.getJobMemoryAddress();
    	tempBuffer = job.getTemporaryBuffer();
    	jobInstructionCount = job.getJobInstructionCount();
    	cacheSize = inputBuffer + outputBuffer + tempBuffer + jobInstructionCount;
    	    	
    	int[] info = {jobNumber, priorityNum, jobBaseAddress, jobInstructionCount, inputBuffer, outputBuffer, tempBuffer, cacheSize};
    	//MMU.writeToPhysical(read(address), MMU.getOffset(read(address)), info);
    	
    	cache = new String[cacheSize];
    	for (int i = 0; i < info.length-1; i++) 
    	{
    		cache[i] = Integer.toString(info[i]);
    	}

    }

    public String read(int address) 
    {
    	return MMU.read(jobBaseAddress);
    }

    //Helpers

    /**
     * logging function, prepends the log message with the Job and CPU num.
     * @param val the message to be logged.
     */
    private void log(String val)
    {
        System.out.println(String.format("JOB: %s CPU #: %s MSG: %s",
                currentJobNumber(),
                CPUNum,
                val));
    }

    /**
     * Function makes call that updates the gui with the most recent metric data.
     *
     * @param state The current state of the CPU, RUNNING, LOADING,... see enum type.
     * @param instruction
     * @see Driver#updateCpuMetric(CPUMetrics)
     */
    private void updateMetrics(CPUMetrics.CPU_STATE state, String instruction)
    {
        CPUMetrics metrics = new CPUMetrics(CPUNum);
        metrics.setTotalInstructionsNumber(jobCounter);
        metrics.setProgramCounter(programCounter);
        metrics.setCurrentJobNumber(currentJobNumber());
        metrics.setCurrentState(state);
        metrics.setCurrentInstruction(instruction);
        Driver.updateCpuMetric(metrics);
    }


    /**
     * Function calls  Driver.updateOsMetric(); that updates the gui with the current OS metrics.
     * @see Driver#updateOsMetric()
     */
    private void updateOsMetrics()
    {
        Driver.updateOsMetric();
}
}
