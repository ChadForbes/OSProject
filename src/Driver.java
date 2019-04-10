import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created by Stephen on 2/8/16.
 */


public class Driver {
    public static int CPU_COUNT =1;
    public static int SORT_ALGORITHM=1;
    public static int SLEEP_INTERVAL=15;
    public Driver() {

        LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();
        // CPU = new CPU();
    }

    public Driver(int numOfCPUs){
        LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();
        // CPU = new CPU();
        CPUs = new CPU[numOfCPUs];
        // Arrays.fill(CPUs, new CPU());

        for (int i = 0; i < CPUs.length; i++)
            // CPUs[i] = new CPU();


        cpuFutures = new Future<?>[numOfCPUs];
        executorService = Executors.newFixedThreadPool(numOfCPUs);
        jobsRan = new ArrayList<String>();
        isOSComplete = false;
        commands = new String[31];
    }
    public static void init(int numOfCPUs, int sleep)
    {
        LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();
        // CPU = new CPU();
        CPUs = new CPU[numOfCPUs];
        //  jobMetricses = new JobMetrics[50];
        // Arrays.fill(CPUs, new CPU());

        for (int i = 0; i < CPUs.length; i++)
            CPUs[i] = new CPU(i+1);

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
    public static CPU[] CPUs;
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

        while (custardStands())
        {
            ready();
            aim((schedulingType != null)? schedulingType : SchedulingType.FIFO);
            fire();
        }
        executorService.shutdown();

    }

    public static boolean custardStands()
    {
        return  !PCBManager.allJobsDone() && !isOSComplete;
    }

    public static void ready()
    {
        LongTermScheduler.Schedule();
    }

    public static void aim(SchedulingType type)
    {
        ShortTermScheduler.Schedule(type);
    }

    public static void fire()
    {
        Dispatcher.dispatch();
        for (int i  = 0; i <CPUs.length && custardStands(); i++)
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
//        System.out.println("\nfire()");
//        for (int  i = 0; i < jobsRan.size(); i++)
//            System.out.println(jobsRan.get(i));
    }

    public static boolean isCPUIdle(){

        if (CPU.isIdle())
            return false;
        return true;
    }

    public static boolean areAllCPUsIdle()
    {
        //update when multi cored.
        for (int i = 0; i < CPUs.length; i++)
        {
            if(!CPUs[i].isIdle())
                return false;
        }
        return true;
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


    public static void main(String [] args) {

                Driver.init(Integer.parseInt(input1.getText()), SchedulingType.fromValue(box.getSelectedIndex()+1),Integer.parseInt(input2.getText()) );
                Driver.run();
        };



        int numberOfTimesDriverRuns = 0;


}