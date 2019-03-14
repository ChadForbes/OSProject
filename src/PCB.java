import java.util.ArrayList;
import java.util.Arrays;

//Essentially stores a job's info and the corresponding data's info
public class PCB {

    public PCB(int jobID, int jobSize, int jobDiskIndex)
    {
        this.jobID = jobID;
        this.jobDiskIndex = jobDiskIndex;
        this.jobSize = jobSize;
        jobInMemory = false;
        registers=new int[16];
        Arrays.fill(registers,0);
    }

    //Data information
    private int dataDiskIndex;
    private int dataMemoryAddress;

    //Job instructions
    private int jobID;
    private int jobPriority;
    private int jobDiskIndex;
    private int jobMemoryIndex;
    private int jobSize;


    //Buffer information
    private int inputBuffer;
    private int outputBuffer;
    private int tempBuffer;

    private boolean IObound;
    private String instruction;
    private boolean jobInMemory;
    private boolean hasJobRan;
    private int[] registers;
    private int programCounter;
    private String[] cache;
    private int cacheSize;

    public int getCacheSize() {
        return cache.length;
    }

    public void setCacheSize(int cachesize) {
        this.cacheSize = cachesize;
    }

    public String[] getCache() {
        return cache;
    }

    public void setCache(String[] cache) {
        this.cache = cache;
    }

    public int getProgramCounter()
    {
        return programCounter;
    }
    public void setProgramCounter(int num)
    {
        programCounter =num;
    }
    public void setRegisters(int[] reg)
    {
        registers=reg;
    }
    public int[] getRegisters()
    {
        return registers;
    }

    public boolean isHasJobRan() {
        return hasJobRan;
    }

    public void setHasJobRan(boolean hasJobRan) {
        this.hasJobRan = hasJobRan;
    }

    public boolean isJobInMemory() {
        return jobInMemory;
    }

    public void setJobInMemory(boolean jobInMemory) {
        this.jobInMemory = jobInMemory;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public boolean isIObound() {
        return IObound;
    }

    public void setIObound(boolean IObound) {
        this.IObound = IObound;
    }

    public int getTempBuffer() {
        return tempBuffer;
    }

    public void setTempBuffer(int tempBuffer) {
        this.tempBuffer = tempBuffer;
    }

    public int getOutputBuffer() {
        return outputBuffer;
    }

    public void setOutputBuffer(int outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public int getInputBuffer() {
        return inputBuffer;
    }

    public void setInputBuffer(int inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public int getDataMemoryAddress() {
        return dataMemoryAddress;
    }

    public void setDataMemoryAddress(int dataMemoryAddress) {
        this.dataMemoryAddress = dataMemoryAddress;
    }

    public int getDataDiskIndex() {
        return dataDiskIndex;
    }

    public void setDataDiskIndex(int dataDiskIndex) {
        this.dataDiskIndex = dataDiskIndex;
    }

    public int getJobSize() {
        return jobSize;
    }

    public void setJobSize(int jobSize) {
        this.jobSize = jobSize;
    }

    public int getJobMemoryIndex() {
        return jobMemoryIndex;
    }

    public void setJobMemoryIndex(int jobMemoryIndex) {
        this.jobMemoryIndex = jobMemoryIndex;
    }

    public int getjobDiskIndex() {
        return jobDiskIndex;
    }

    public void setjobDiskIndex(int jobDiskIndex) {
        this.jobDiskIndex = jobDiskIndex;
    }

    public int getJobPriority() {
        return jobPriority;
    }

    public void setJobPriority(int jobPriority) {
        this.jobPriority = jobPriority;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }
}