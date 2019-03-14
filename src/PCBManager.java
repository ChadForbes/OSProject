import java.util.ArrayList;

//This will be very important to expand in phase 2
public class PCBManager{
    ArrayList<PCB> pcbList = new ArrayList<PCB>();

    public int getJobCount(){
        return pcbList.size();
    }

    public void addPCB(PCB pcb){
        pcbList.add(pcb)
    }

    public static PCB getPCB(int index) {
        return pcbList.get(index);
    }
}