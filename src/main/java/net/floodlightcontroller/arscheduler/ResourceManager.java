package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

import org.slf4j.Logger;

/**
 * Maintains our view of the network state - i.e. used bandwidth on links and active or expired flows.
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class ResourceManager 
{
	/**
	 * Set of links and the bandwidth used per link
	 */
	private ArrayList<FlowLink> linkStatus = new ArrayList<FlowLink>();
	/**
	 * Set of flows and associated paths
	 */
	private FlowTable flowTable = new FlowTable();
	/**
	 * The nodes and links in the network
	 */
	protected Topology netTopology;
	
	/**
	 * Logger for output to console
	 */
	private Logger logger;
	
	
	/**
	 * Construct the Resource Manager with a logger
	 * @param log
	 */
	public ResourceManager(Logger log){
		logger = log;
	}
	
	/**
	 * Replace the Resource Manager's topology with a new topology, update the
	 * status of all links, and reset the Flow Table
	 * @param newTopology - the new topology
	 */
	public void intializeState(Topology newTopology){
		netTopology = newTopology;
		linkStatus = netTopology.getLinks();
		flowTable.removeAllFlowRouteTuples();
	}
		
	/**
	 * Reserve an amount of bandwidth on a link for a particular time period.
	 * Fails if link does not exist.
	 * @param link
	 * @param bwToReserve
	 * @param startTime
	 * @param endTime
	 */
	public void decreaseAvailableLinkBandwidth(FlowLink link, long bwToReserve, long startTime, long endTime)
	{
		assert !linkStatus.isEmpty();
		assert(link != null);
		assert(linkStatus.contains(link));
						
		linkStatus.get(linkStatus.indexOf(link)).decreaseBandwidthAvailable(bwToReserve, startTime, endTime);
	}
	
	/**
	 * Release an amount of bandwidth on a link for a particular time period.
	 * Fails if the link does not exist.
	 * @param link
	 * @param bwToFree
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public boolean increaseAvailableLinkBandwidth(FlowLink link, long bwToFree, long startTime, long endTime)
	{
		assert !linkStatus.isEmpty();
		assert(link != null);
		assert(linkStatus.contains(link));
		
		return linkStatus.get(linkStatus.indexOf(link)).increaseBandwidthAvailable(bwToFree, startTime, endTime);
	}
	
	/**
	 * Return the FlowTable (List of FlowRouteTuples)
	 * @return FlowTable
	 */
	public FlowTable getFlowTable()
	{
		return flowTable;
	}
	
	/**
	 * Add a flow and a route to the FlowTable.
	 * @param flow
	 * @param topo
	 */
	public void addFlowToRM(Flow flow, Topology topo)
	{
		FlowRouteTuple frTuple = new FlowRouteTuple(flow, topo);
		flowTable.addFlowRouteTupleToFlowTable(frTuple);
	}
	
	/**
	 * Set the status of a given FlowRouteTuple in the FlowTable to "Released"
	 * @param flowTupleToRelease
	 */
	public void releaseFlowFromRM(FlowRouteTuple flowTupleToRelease)
	{
		for(FlowRouteTuple frTuple : flowTable.getAllFlowRouteTuples())
		{			
			if(frTuple.equals(flowTupleToRelease) && !frTuple.isReleased())
			{
				frTuple.setReleased(true);
				logger.info("Flow {} Released", flowTupleToRelease.getFlow().getID());
				return;
			}
		}
	}
	
	/**
	 * Return a FlowRouteTuple given a Flow object
	 * @param flow
	 * @return the matching FlowRouteTuple
	 */
	public FlowRouteTuple getFlowFromRM(Flow flow)
	{
		for(FlowRouteTuple frTuple : flowTable.getAllFlowRouteTuples())
		{
			if(frTuple.getFlow().equals(flow))
			{
				return frTuple;
			}
		}
		
		return null;	
	}
	
	/**
	 * Return the Resource Manager's view of the topology	
	 * @return the Topology
	 */
	public Topology getTopology()
	{
		return netTopology;
	}
	
}
