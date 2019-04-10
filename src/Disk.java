//Disk class for OS Project
//Declare RAM separately in a diff class
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Disk {
    String disk[] = new String[4096];

    public void Loader(String fileName) {
        int currentIndex = 0;
        PCB pcb;
        int jobID = 0;
        int jobSize;
        String line;
        String[] removedWhitespace;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            line = reader.readLine();
            while(line != null){
                if(line.contains("//")){
                    if(line.contains("JOB")){
                        //Splits the line into tokens separated by whitespaces
                        removedWhitespace = line.split("\\s+");
                        jobID = Integer.parseInt(removedWhitespace[2], 16);
                        jobSize = Integer.parseInt(removedWhitespace[3], 16);
                        //jobPriority = Integer.parseInt(removedWhitespace[4], 16); <-- we dont use this yet
                        pcb = new PCB(jobID, jobSize, currentIndex);
                        PCBManager.addPCB(pcb);
                    }
                    else if (line.contains("DATA")) {
                        removedWhitespace = line.split("\\s+");
                        PCB currentPCB = PCBManager.getPCB(jobID);
                        currentPCB.setDataDiskIndex(currentIndex);
                        currentPCB.setInputBuffer(Integer.parseInt(removedWhitespace[2]));
                        currentPCB.setOutputBuffer(Integer.parseInt(removedWhitespace[3]));
                        currentPCB.setTempBuffer(Integer.parseInt(removedWhitespace[4]));
                    }
                    else if (line.contains("END")){
                        //nothing
                    }
                }
                else {
                    write(currentIndex, line);
                    currentIndex++;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String read(int index) {
        return disk[index];
    }

    public static void write(int index, String instruction){disk[index] = instruction;}
}