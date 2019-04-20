import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;



public class Driver {

    public Driver() {

        LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();
    }

    public Driver(int numOfCPUs, SchedulingType schedulingType){
        LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();
        Driver.schedulingType = schedulingType;
        CPUs = new CPU[numOfCPUs];

        for (int i = 0; i < CPUs.length; i++)


        cpuFutures = new Future<?>[numOfCPUs];
        executorService = Executors.newFixedThreadPool(numOfCPUs);
        jobsRan = new ArrayList<String>();
        isOSComplete = false;
        commands = new String[31];
    }
    
    //Uses user input from panel to set our base values.
    public static void init(int numOfCPUs, SchedulingType schedType, int sleep)
    {
        schedulingType = schedType;
        CPUs = new CPU[numOfCPUs];
        CpuMetrics = new CPUMetrics[numOfCPUs];
        LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();


        for (int i = 0; i < CPUs.length; i++)
            CPUs[i] = new CPU(i+1);

        for (int i = 0; i < CpuMetrics.length; i++)
            CpuMetrics[i] = new CPUMetrics(i+1);




        cpuFutures = new Future<?>[numOfCPUs];
        executorService = Executors.newFixedThreadPool(numOfCPUs);
        jobsRan = new ArrayList<String>();
        isOSComplete = false;
        commands = new String[31];
        numberOfCPUs = numOfCPUs;
        sleepTimeMs = (long)sleep;
        completedJobs = 0;

    }
    public static LongTermScheduler LongTermScheduler;
    public static ShortTermScheduler ShortTermScheduler;
    public static CPU CPU;
    public static SchedulingType schedulingType;
    public static CPU[] CPUs;
    public static CPUMetrics[] CpuMetrics;
    public static JobMetrics[] jobMetricses;
    public static ExecutorService executorService;
    public static Future<?>[] cpuFutures;
    public static ArrayList<String> jobsRan;
    public static boolean isOSComplete = false;
    public static String[] commands;
    public static int numberOfCPUs = 4;
    private static int totalWaitTime = 0;
    private static int totalRunTime = 0;
    public static int completedJobs = 0;
    public static long sleepTimeMs = 0;
    public static long  osStartTime = 0;
    public static long osEndTIme = 0;

    /* Starts our RAM, Disk, and PCB manager, as well as tracking run time.
     * Sends our program file into our loader then starts loader
     * Sets job metrics for each job
     * Runs our OS until our PCB indicates completion
     * 
     */
    public static void run() throws IOException
    {
        RAM.init();
        Disk.init();
        PCBManager.init();
        osStartTime = System.currentTimeMillis();

        Loader loader = new Loader(System.getProperty("user.dir") + "/src/ProgramFile.txt");
        loader.Start();

        jobMetricses = new JobMetrics[PCBManager.getJobListSize()];
        for (int i = 0; i < jobMetricses.length; i++)
            jobMetricses[i] = new JobMetrics();


        String s = Disk.readDisk(2);
        System.out.println("DISK TEST: " + s);

        while (stillWorking())
        {
            ready();
            prep((schedulingType != null)? schedulingType : SchedulingType.FIFO);
            execute();
        }
        executorService.shutdown();

    }

    public static boolean stillWorking()
    {
        return  !PCBManager.allJobsDone() && !isOSComplete;
    }

    public static void ready()
    {
        LongTermScheduler.Schedule();
    }

    public static void prep(SchedulingType type)
    {
        ShortTermScheduler.Schedule(type);
    }

    public static void execute()
    {
        Dispatcher.dispatch();
        for (int i  = 0; i <CPUs.length && stillWorking(); i++)
        {
            if(cpuFutures[i] != null && cpuFutures[i].isCancelled())
                System.out.println("XXXXXXX CANCELLED XXXXXX" + i);
            try {

            if(cpuFutures[i] != null  && cpuFutures[i].isDone())
                cpuFutures[i].get();
            }
            catch (Exception ex)
            {
                System.out.println("<<FUTURE FINISHED BUT EXCEPTION WAS THROWN!!!!>>\n" + ex.toString());
            }
            if(((cpuFutures[i] == null || cpuFutures[i].isDone() || cpuFutures[i].isCancelled()) && CPUs[i].isJobLoaded() && !CPUs[i].shouldUnload()))
//                    || (!CPUs[i].isRunning() && !CPUs[i].isIdle() && !CPUs[i].shouldUnload()))
            {
                cpuFutures[i] = executorService.submit(CPUs[i]);
                commands[CPUs[i].currentJobNumber()] += "\nRUNNING JOB: " + CPUs[i].currentJobNumber() + "\tON CPU: " + i;
            }
        }
    }

    public static boolean areAllCPUsIdle()
    {
        for (int i = 0; i < CPUs.length; i++)
        {
            if(!CPUs[i].isIdle())
                return false;
        }
        return true;
    }

    public static void updateCpuMetric(final CPUMetrics metrics)
    {
        CpuMetrics[metrics.cpuNumber-1].update(metrics);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame.updateCPUMetrics(metrics);
            }
        });
    }

    public static void updateJobMetrics(final JobMetrics jobMetrics)
    {
        jobMetricses[jobMetrics.getJobNumber()-1].update(jobMetrics);
        if(jobMetricses[jobMetrics.getJobNumber()-1].getWaitTime() > 0)
            totalWaitTime += jobMetricses[jobMetrics.getJobNumber()-1].getWaitTime();
        if(jobMetricses[jobMetrics.getJobNumber()-1].getRunTime() > 0)
            totalRunTime += jobMetricses[jobMetrics.getJobNumber()-1].getRunTime();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame.updateJobMetrics(jobMetrics);
            }
        });

    }

    public static void updateOsMetric()
    {
        int jobsInProgress = numberOfBusyCpus();

        final OSMetrics osMetrics = new OSMetrics(PCBManager.getJobListSize(), numberOfBusyCpus(), completedJobs, getAverageWaitTime(), getAverageRunTime());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame.updateOsMetrics(osMetrics);
            }
        });

    }

    private static double getAverageWaitTime()
    {
        int jobs = 0;
        int waitTime = 0;
        for (JobMetrics job : jobMetricses)
        {
            if(job.getWaitTime() > 0)
            {
                jobs++;
                waitTime += job.getWaitTime();
            }
        }
        return (double)waitTime/jobs;
    }

    private static double getAverageRunTime()
    {
        int jobs = 0;
        int runTime = 0;
        for (JobMetrics job : jobMetricses)
        {
            if(job.getRunTime() > 0)
            {
                jobs++;
                runTime += job.getRunTime();
            }
        }
        return (double)runTime/jobs;
    }
    
    //
    private static int numberOfBusyCpus()
    {
        int count = 0;
        for (CPU cpu : CPUs)
        {
            if (!cpu.isIdle())
                count++;
        }
        return count;
    }

    public static double calcOSRunTime()
    {
        return (double)((osEndTIme - osStartTime)/1000.0);
    }

    
    //Starts OS 
    public static void main(String [] args) {


        System.out.println("starting OS...");
        MainFrame.buildUI();
        MainFrame.driverFunction = new Callable() {
            @Override
            public Object call() throws Exception {
            	//Sets OS's settings based off of user input
                Driver.init(Integer.parseInt(input1.getText()), SchedulingType.fromValue(box.getSelectedIndex()+1),Integer.parseInt(input2.getText()) );
                Driver.run();
                return null;
            }
        };




        int numberOfTimesDriverRuns = 0;


    }



    public static JTextField input1=new JTextField("4", 3);
    public static JTextField input2=new JTextField("0", 4);
    public static JComboBox box;
    public static ConsoleConstructor con=new ConsoleConstructor();
  



}
