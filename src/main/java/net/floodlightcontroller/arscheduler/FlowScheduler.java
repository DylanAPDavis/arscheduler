package net.floodlightcontroller.arscheduler;

import org.slf4j.Logger;

/**
 * Negotiates between the AR Scheduler and the Resource Manager to schedule flow requests
 * when they do not conflict with other scheduled requests.
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class FlowScheduler {
	/**
	 * The Resource Manager, Bandwidth Pruner, and Shortest Path Engine used by the 
	 * scheduler to determine how a request can be provisioned on the network
	 */
	ResourceManager theResourceManager;
	BandwidthPruner bwPruner;
	ShortestPathEngine spEngine;
	Logger logger;
	
	/**
	 * Construct a Flow Scheduler with a given Resource Manager
	 * @param rm - the Resource Manager which keeps track of used network resources
	 */
	public FlowScheduler(ResourceManager rm, Logger log)
	{
		theResourceManager = rm;
		logger = log;
	}
	
	/**
	 * Given a Flow parameter, determine what path (if any) can be provisioned to
	 * establish the flow. Updates the Resource Manager if a path can be provisioned.
	 * @param flow - the Flow request (and associated constraints)
	 * @return the Flow request with an updated success status.
	 */
	public Flow scheduleNewFlow(Flow flow)
	{
		Topology prunedTopology;
		Topology shortestPath;
		
		bwPruner = new BandwidthPruner(theResourceManager.getTopology());
		prunedTopology = bwPruner.pruneTopology(flow.getBandwidth(), flow.getStartTime(), flow.getEndTime());
		
		if(prunedTopology.isEmpty())
			return null;
						
		spEngine = new ShortestPathEngine(prunedTopology);
		shortestPath = spEngine.calculateSP(flow);
		
		//shortestPath.dumpTopology();
				
		if(shortestPath.isEmpty())
		{
			logger.warn("No Feasible Path for Flow {}", flow.getID());
			return null;
		}
		
				
		for(FlowLink l : shortestPath.getLinks())
		{
			theResourceManager.decreaseAvailableLinkBandwidth(l, flow.getBandwidth(), flow.getStartTime(), flow.getEndTime());
		}
				
		
		theResourceManager.addFlowToRM(flow, shortestPath);
				
		flow.schedulingSuccess(true);
		return flow;
	}
	
	/**
	 * Release an expired Flow by updating the Resource Manager.
	 * @param flowTuple - A combination of a Flow and its associated Route
	 * @return - True if the flow was successfully released and the RM was updated
	 */
	public boolean releaseExpiredFlow(FlowRouteTuple flowTuple)
	{
		
		if(flowTuple == null)
			return false;
		
		assert(flowTuple.getFlow().getSuccess() == true);
		if(!flowTuple.isReleased()){
			theResourceManager.releaseFlowFromRM(flowTuple);
			Topology flowRoute = flowTuple.getShortestPathtopology();
			Flow flow = flowTuple.getFlow();
			for(FlowLink l : flowRoute.getLinks())
			{
				theResourceManager.increaseAvailableLinkBandwidth(l, flow.getBandwidth(), flow.getStartTime(), flow.getEndTime());
			}			
		}
		
		return true;
	}
}
