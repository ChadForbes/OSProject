import java.util.ArrayList;

public class ShortTermScheduler {

    public ArrayList<PCB> readyQueue;
    public PCB block;

    public ShortTermScheduler() {
        readyQueue = new ArrayList<PCB>();

    }

    public void ScheduleFIFO(){
        for (int i = 0; i <= PCBManager.getJobCount();i++){
            readyQueue.add(PCBManager.getPCB(i));
        }
    }

    public void addToRQueue(PCB pcb){
        readyQueue.add(pcb);
    }

}
