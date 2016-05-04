package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

import net.floodlightcontroller.core.module.IFloodlightService;

/**
 * Interface for the ARScheduler to provide services
 * @author dylan
 *
 */
public interface IARSchedulerService extends IFloodlightService {
	/**
	 * Initialize the ARScheduler's network state
	 * @return The nodes known to the ARScheduler
	 */
	public ArrayList<Node> initializeState();
	/**
	 * Get the ARScheduler's topology object
	 * @return The topology
	 */
	public Topology getTopology();
	/**
	 * Handle a new flow scheduling request
	 * @param flow
	 * @return status String
	 */
	public String handleNewFlow(Flow flow);
}
