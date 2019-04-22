package os2019.memory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import os2019.Driver;
import os2019.processing.PCB;

public class Disk {
	//
	// static data
	//
	
	public static final int DISK_SIZE = 4096;
	
	//
	// instance data
	//
	
	private static String[] disk;
	private int nextFreeWord;
	
	//
	// constructor
	//
	
	public Disk() {
		disk = new String[DISK_SIZE];
		nextFreeWord = 0;
	}
	
	//
	// instance methods
	//
	
	/**
	 * Loads all jobs from the given file into the system as new processes and
	 * stores the file's contents into this disk, minus command cards.
	 * @param fileName The path to the file to load processes from.
	 */

	
	public String read(int word) {
		return disk[word];
	}
	
	public static void write(int word, String instruction) {
		disk[word] = instruction;
	}
}