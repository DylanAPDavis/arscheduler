package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

/**
 * Prunes out all links in a topology which do not have an available capacity at least equal to the requested bandwidth.
 * @author Dylan
 *
 */
public class BandwidthPruner 
{
	/**
	 * The provided topology
	 */
	Topology netTopology;
	
	/**
	 * Construct the pruner with the given topology.
	 * @param topo
	 */
	public BandwidthPruner(Topology topo)
	{
		netTopology = topo;
	}
	
	/**
	 * Prune out the links in the topology which do not have enough bandwidth available between (startTime, endTime)
	 * @param requiredBandwidth
	 * @param startTime
	 * @param endTime
	 * @return The topology with insufficient bandwidth links removed.
	 */
	public Topology pruneTopology(long requiredBandwidth, long startTime, long endTime)	//bps
	{
		Topology prunedTopo;
		ArrayList<FlowLink> prunedLinks = new ArrayList<FlowLink>();
				
		for(FlowLink l : netTopology.getLinks())
		{			
			if(l.getBandwidthAvailableBetweenTimes(startTime, endTime) >= requiredBandwidth)
				prunedLinks.add(l);
		}
				
		prunedTopo = new Topology(netTopology.getNodes(), prunedLinks);
		
		return prunedTopo;
	}
}

