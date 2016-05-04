package net.floodlightcontroller.arscheduler;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;

/**
 * Runs in the background, waiting to provision a successfully scheduled flow until the starting time.
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class SchedulingThread implements Runnable 
{
	/**
	 * The thread's ID
	 */
	static int tID = 0;
	int threadID;
	
	/**
	 * Calendar used to wait for the start time
	 */
	Calendar scheduledCal;
	/**
	 * The thread itself
	 */
	Thread theSchedulingThread;
	/**
	 * The coordinating ARScheduler
	 */
	ARScheduler schedulingCoordinator;
	/**
	 * The flow to be provisioned
	 */
	Flow flowToSchedule;
	/**
	 * The logger
	 */
	private Logger logger;

	/**
	 * Construct a SchedulingThread with references to an ARScheduler, a Flow, and the starting time
	 * Also takes in a logger for output.
	 * @param coordinator
	 * @param flow
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public SchedulingThread(ARScheduler coordinator, Flow flow, int hour, int minute, int second, Logger log)
	{
		threadID = ++tID;
		
		schedulingCoordinator = coordinator;
		flowToSchedule = flow;
		logger = log;
		
		theSchedulingThread = new Thread(this);
		
		Calendar initialCal = Calendar.getInstance();
		
		Date scheduledDate = initialCal.getTime();
		scheduledDate.setHours(hour);
		scheduledDate.setMinutes(minute);
		scheduledDate.setSeconds(second);
		
		scheduledCal = Calendar.getInstance();
		scheduledCal.setTime(scheduledDate);
	
		theSchedulingThread.start();	// Start background thread
	}
	
	/**
	 * Run the scheduling thread, wait until the start time, then provision the flow
	 */
	@Override
	public void run() 
	{
		logger.info("Initializing new Scheduling Thread for Flow {}.", flowToSchedule.getID());
		logger.info("Flow will be established at time {}",  
				String.valueOf(scheduledCal.get(Calendar.HOUR_OF_DAY)) + ":" + 
				String.valueOf(scheduledCal.get(Calendar.MINUTE)) + ":" + 
				String.valueOf(scheduledCal.get(Calendar.SECOND)));
		Calendar currentCal = Calendar.getInstance();
								
		while(currentCal.before(scheduledCal))
		{
			currentCal = Calendar.getInstance();
		}
		
		logger.info("Current time = {}",  
				String.valueOf(currentCal.get(Calendar.HOUR_OF_DAY)) + ":" + 
				String.valueOf(currentCal.get(Calendar.MINUTE)) + ":" + 
				String.valueOf(currentCal.get(Calendar.SECOND)));
		logger.info("Scheduling Flow {}!", flowToSchedule.getID());
		
		schedulingCoordinator.flowProvisioner.provisionFlowPath(schedulingCoordinator.theRM.getFlowFromRM(flowToSchedule), 
				schedulingCoordinator.getFloodlightTopology().getSwitchMap(), 
				schedulingCoordinator.getFloodlightTopology().getSwitchQueueMap());
		logger.info("Flow {} Active!", flowToSchedule.getID());
	}
}
