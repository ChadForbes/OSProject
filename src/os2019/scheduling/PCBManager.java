package os2019.scheduling;
import java.util.ArrayList;

import os2019.processing.PCB;

//This will be very important to expand in phase 2
public final class PCBManager{
    private static ArrayList<PCB> pcbList = new ArrayList<PCB>();

    public static int getJobCount(){
        return pcbList.size();
    }

    public static void addPCB(PCB pcb){
        pcbList.add(pcb);
    }

    public static PCB getPCB(int index) {
        return pcbList.get(index);
    }
}
