import java.util.ArrayList;

//This will be very important to expand in phase 2
public final class PCBManager{
    private static ArrayList<PCB> pcbList = new ArrayList<PCB>();

    public static int getJobCount(){
        return pcbList.size();
    }

    public void addPCB(PCB pcb){
        pcbList.add(pcb);
    }

    public static PCB getPCB(int index) {
        return pcbList.get(index);
    }
}