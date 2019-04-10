import java.util.ArrayList;

public class LongTermScheduler {

    public LongTermScheduler(){

    }

    public void Schedule(){
        if(!RAM.isRAMFull()){
            int totJobs = PCBManager.getJobCount();
            for (int i = 0; i <= totJobs; i++){
                if (!PCBManager.getPCB(i).isJobInMemory() && !RAM.isRAMFull()){
                    loadJobToRam(PCBManager.getPCB(i));
                }
            }
        }
    }

    private void loadJobToRam(PCB pcb) {

        int jobNum = pcb.getJobID();
        int i = 0;
        int m = PCBManager.getPCB(jobNum).getjobDiskIndex();
        int dataSize = pcb.getInputBuffer() + pcb.getOutputBuffer() + pcb.getTempBuffer();
        int mem = PCBManager.getPCB(jobNum).getjobDiskIndex() + dataSize;
        int numPages = (int)Math.ceil((double)mem/(double) RAM.getPageSize());
        int startAddr = pcb.getjobDiskIndex();
        int curDiskAddr = startAddr;
        int physPageNum;
        int virPageNum =
        String[] chunk;
        ArrayList<Integer> virAllocatedPages = RAM.allocate(numPages);
        if(virAllocatedPages.size() != 0){
            System.out.println(jobNum);
            PCBManager.getPCB(jobNum).setJobInMemory(true);
            PCBManager.getPCB(jobNum).setJobMemoryIndex(virPageNum * RAM.getPageSize());


        }


        try{
            RAM.fillPage();
        }catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        Driver.ShortTermScheduler.addToRQueue(PCBManager.getPCB(jobNum));
    }
    private int calulateChunkSize(PCB pcb, int curDiskAddr){
        int dataSize = pcb.getInputBuffer() + pcb.getOutputBuffer() + pcb.getTempBuffer();
        int mem = pcb.getjobDiskIndex() + dataSize;
        if (curDiskAddr + RAM.getPageSize() > pcb.getjobDiskIndex()+mem){
            return (pcb.getjobDiskIndex() + mem)-curDiskAddr;
        }else {
            return RAM.getPageSize();
        }
    }
}
