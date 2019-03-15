import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;



public class Driver {
    public static int NUM_CPUS=4;
    public static int SORT_ALGORITHM=1;
    public static int SLEEP_INTERVAL=15;
    public static ArrayList<JobMetrics> jobMets = new ArrayList();
    
    
    public Driver() {
        
        ShortTermScheduler = new ShortTermScheduler();
        LongTermScheduler = new LongTermScheduler();

    }

    public Driver(int numOfCPUs, SchedulingType schedulingType){
    	
        ShortTermScheduler = new ShortTermScheduler();
        LongTermScheduler = new LongTermScheduler();
        
        this.schedulingType = schedulingType;
        CPUs = new CPU[numOfCPUs];


        for (int i = 0; i < CPUs.length; i++)


        cpuFutures = new Future<?>[numOfCPUs];
        executorService = Executors.newFixedThreadPool(numOfCPUs);
        jobsRan = new ArrayList<String>();
        isOSComplete = false;
        commands = new String[31];
    }
    public static void init(int numOfCPUs, SchedulingType schedType, int sleep)
    {
    	schedulingType = schedType;
    	LongTermScheduler = new LongTermScheduler();
        ShortTermScheduler = new ShortTermScheduler();
        CpuMetrics = new CPUMetrics[numOfCPUs];
        CPUs = new CPU[numOfCPUs];
        

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
    public static ShortTermScheduler ShortTermScheduler;
    public static LongTermScheduler LongTermScheduler;
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
                System.out.println("XXX CANCELLED XXX" + i);
            try {

            if(cpuFutures[i] != null  && cpuFutures[i].isDone())
                cpuFutures[i].get();
            }
            catch (Exception ex)
            {
                System.out.println("<<FUTURE FINISHED BUT EXCEPTION WAS THROWN!!!!>>\n" + ex.toString());
            }
            if(((cpuFutures[i] == null || cpuFutures[i].isDone() || cpuFutures[i].isCancelled()) && CPUs[i].isJobLoaded() && !CPUs[i].shouldUnload()))
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


        System.out.println("starting OS...");
        MainFrame.buildUI();
        MainFrame.driverFunction = new Callable() {
            @Override
            public Object call() throws Exception {
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
    private static void buildUI()
    {
        JFrame frame = new JFrame("JequirityOS");

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(175, 100));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.out.println(JFrame.WIDTH);

        JPanel options =new JPanel(new FlowLayout());
        options.setBackground(Color.decode("#212121"));
        options.setPreferredSize(new Dimension(JFrame.WIDTH, 40));
        JLabel text1 = new JLabel("Number of CPUs");
        text1.setForeground(Color.decode("#8BC34A"));

        JLabel text2 = new JLabel("Type of Scheduling: ");
        JLabel text3 = new JLabel("Sleep Delay");
        String[] ops= {"Priority (Non Pre-emptive)","FIFO","SJF"};
        box=new JComboBox(ops);
        text2.setForeground(Color.decode("#8BC34A"));
        text3.setForeground(Color.decode("#8BC34A"));
        JButton startButton= new JButton("START");

        JLabel logo = new JLabel(new ImageIcon(System.getProperty("user.dir") + "/src/JOSlogoV3.png"));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setAlignmentX(frame.getContentPane().CENTER_ALIGNMENT);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    con.clear();
                    Driver.init(Integer.parseInt(input1.getText()), SchedulingType.fromValue(box.getSelectedIndex()+1),Integer.parseInt(input2.getText()) );
                    Driver.run();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        options.add(text1);
        options.add(input1);
        options.add(text2);
        options.add(box);
        options.add(text3);
        options.add(input2);
        options.add(startButton);

        MatteBorder border=new MatteBorder(0, 40, 40, 40, Color.decode("#212121"));

        con.setBorder(border);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.getContentPane().add(logo);
        frame.getContentPane().add(options);
        frame.getContentPane().add(con);
        frame.getContentPane().setBackground(Color.decode("#212121"));
        frame.pack();
        frame.setVisible(true);
    }



}
