package os2019;
import os2019.memory.Disk;
import os2019.processing.PCB;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Loader {

    public static boolean loadingComplete = false;
    public static int nextFreeWord = 0;

	/**
	 * Loads all jobs from the given file into the system as new processes and
	 * stores the file's contents into this disk, minus command cards.
	 * @param fileName The path to the file to load processes from.
	 */
	public static void loadFromFile(String fileName) { //TODO: spiffy up
		// create a reader for the file:
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			line = reader.readLine(); // grab first line of file
		} catch(FileNotFoundException e) { // if file does not exist
			e.printStackTrace();
		} catch (IOException e) { // if file is closed
			e.printStackTrace();
		}
		
		// read each line of the file and parse it:
		int jobID = 0, jobSize = 0, jobPriority = 0, startOnDisk = 0,
				inBuffer = 0, outBuffer = 0, tempBuffer = 0;
		while(line != null) { // while not end-of-file
			if(line.isEmpty())
				continue;
			
			if(line.startsWith("//")) { // if line is a command-card
				// Splits the line into tokens separated by whitespaces
				String[] removedWhitespace = line.split("\\s+");
				
				// if beginning of new job:
				if(removedWhitespace[1].equalsIgnoreCase("JOB")) {
					jobID = Integer.parseInt(removedWhitespace[2], 16);
					jobSize = Integer.parseInt(removedWhitespace[3], 16);
					jobPriority = Integer.parseInt(removedWhitespace[4], 16);
					startOnDisk = nextFreeWord;
				// if beginning of data for a job:
				} else if(removedWhitespace[1].equalsIgnoreCase("DATA")) {
					inBuffer = Integer.parseInt(removedWhitespace[2], 16);
					outBuffer = Integer.parseInt(removedWhitespace[3], 16);
					tempBuffer = Integer.parseInt(removedWhitespace[4], 16);
				// if end of job:
				} else if(removedWhitespace[1].equalsIgnoreCase("END")) {
					PCB pcb = new PCB(jobID, startOnDisk, jobSize, inBuffer,
							outBuffer, tempBuffer, jobPriority);
					//PCBManager.addPCB(pcb);
					Driver.getInstance().longTermScheduler
							.registerNewProcess(pcb);
					
					// jump next line pointer to end of buffers:
					nextFreeWord = startOnDisk + pcb.calcJobSize();
				}
			} else // if line contains instruction or data
				Disk.write(nextFreeWord++, line);
			
			// read-in next line:
			try {
				line = reader.readLine();
			} catch (IOException e) { // if file is closed
				e.printStackTrace();
			}

			}
		loadingComplete = true;
		}
}

