//Author Chad Forbes
public class Dispatcher {

    public static void dispatch(){
        EndJobs();
        loadJobs();

        if (Driver.ShortTermScheduler.readyQueue.size() == 0 && Driver.isCPUIdle()){
            EndJobs();
            System.err.println("No ERROR found");
            System.out.println("Successful");
            Driver.isOSComplete = true;
            Driver.osEndTIme = System.currentTimeMillis();
        }
    }

    public static void EndJobs(){
        int curJobCount = Driver.CPU.currentJobNumber();
        try {
            if (Driver.CPU.isIdle() && Driver.CPU.shouldTerminate()){
                Driver.CPU.unload(PCBManager.getPCB(curJobCount));
                MMU.syncCache(PCBManager.getPCB(curJobCount));
                MMU.deallocate(PCBManager.getPCB(curJobCount));
                /*if (PCBManager.getPCB(curJobCount).getProcessStatus())== {
                }*/
            }
        }catch (ArrayIndexOutOfBoundsException ex){
            throw new ArrayIndexOutOfBoundsException("Error: Terminating Job");
        }
    }

    public static void loadJobs(){
        if (Driver.CPU.isIdle() && Driver.ShortTermScheduler.readyQueue.size() > 0){
            PCB pcb = Driver.ShortTermScheduler.readyQueue.remove(Driver.ShortTermScheduler.readyQueue.size());
            if (Driver.CPU.shouldUnload()){
                System.out.println("Job: " + Driver.CPU.currentJobNumber());
                EndJobs();



                Driver.CPU.load(pcb);
            }
        }
    }
}
