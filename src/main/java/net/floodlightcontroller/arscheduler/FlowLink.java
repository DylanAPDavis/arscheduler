package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;
import java.util.Vector;

/**
 * A Link, made up of a name, source node, destination node, source port, destination port, and bandwidth capacity.
 * Keeps track of bandwidth usage across time.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class FlowLink {
	/**
	 * The link's name
	 */
	private String linkName;
	/**
	 * The source node for the link
	 */
	private Node srcNode;
	/**
	 * The destination node for the link
	 */
	private Node dstNode;
	/**
	 * The source port for the link
	 */
	private Port srcPort;
	/**
	 * The destination port for the link
	 */
	private Port dstPort;
	/**
	 * The link's maximum capacity
	 */
	private long bandwidthCapacity;
	/**
	 * A list of the time period & bandwidth consumed for each flow using this link
	 */
	private ArrayList<FlowLinkAvailabilityTuple> allUses;
	
	/**
	 * Construct the FlowLink. The list of uses across time is initialized as an empty list.
	 * @param lnkName
	 * @param src
	 * @param dst
	 * @param srcP
	 * @param dstP
	 * @param bw
	 */
	public FlowLink(String lnkName, Node src, Node dst, Port srcP, Port dstP, long bw)
	{
		linkName = lnkName;
		srcNode = src;
		dstNode = dst;
		srcPort = srcP;
		dstPort = dstP;
		bandwidthCapacity = bw;
		allUses = new ArrayList<FlowLinkAvailabilityTuple>();
	}
	
	/**
	 * Get the maximum amount of bandwidth available.
	 * @return long value of the capacity
	 */
	public long getBandwidthCapacity()
	{
		return this.bandwidthCapacity;
	}
	
	/**
	 * Get the bandwidth consumption across time.
	 * @return ArrayList<FlowLinkAvailabilityTuple>
	 */
	public ArrayList<FlowLinkAvailabilityTuple> getBandwidthConsumptions()
	{
		return allUses;
	}
	
	/**
	 * Add a new bandwidth usage event for this link.
	 * @param lessBandwidth
	 * @param startTime
	 * @param endTime
	 */
	public void decreaseBandwidthAvailable(long lessBandwidth, long startTime, long endTime)
	{
		allUses.add(new FlowLinkAvailabilityTuple(lessBandwidth, startTime, endTime));
	}

	/**
	 * Remove a flow usage event from this link, releasing bandwidth to be available for other flows.
	 * @param moreBandwidth
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public boolean increaseBandwidthAvailable(long moreBandwidth, long startTime, long endTime)
	{
		for(FlowLinkAvailabilityTuple oneBWAvail : allUses)
		{
			if(oneBWAvail.getConsumedBandwidth() == moreBandwidth)
			{
				if(oneBWAvail.getStartTime() == startTime)
				{
					if(oneBWAvail.getEndTime() == endTime)
					{
						allUses.remove(oneBWAvail);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Get the link's source node.
	 * @return Source Node
	 */
	public Node getSrcNode()
	{
		return this.srcNode;
	}
	
	/**
	 * Get the link's destination node.
	 * @return Destination node
	 */
	public Node getDstNode()
	{
		return this.dstNode;
	}
	
	/**
	 * Get the link's source port.
	 * @return Source port
	 */
	public Port getSrcPort()
	{
		return this.srcPort;
	}
	
	/**
	 * Get the link's destination port.
	 * @return Destination port
	 */
	public Port getDstPort()
	{
		return this.dstPort;
	}
	
	/**
	 * Return string representation of this link
	 */
	@Override
	public String toString()
	{		
		String output = "Link " + linkName + ": (" + srcNode + ", " + dstNode + ")\n\t{";
		
		for(FlowLinkAvailabilityTuple t : allUses)
		{
			int startHour = (int)(t.getStartTime() / 60 / 60);
			int startMinute = (int)(t.getStartTime() / 60 % 60);	
			int endHour = (int)(t.getEndTime() / 60 / 60);
			int endMinute = (int)(t.getEndTime() / 60 % 60);
			
			output += "BW: " + t.getConsumedBandwidth() + "Gbps , " + "(" + startHour + ":" + startMinute + " - " + endHour + ":" + endMinute + ")\n";
		}
		return output;
	}
	
	// Added by Jeremy for AR Bandwidth availability check
	/**
	 * Get the amount of bandwidth available during a time period
	 * @param startX
	 * @param endX
	 * @return long Bandwidth available
	 */
	public long getBandwidthAvailableBetweenTimes(long startX, long endX)
	{		
		ArrayList< ArrayList<Integer> > overlaps = new ArrayList< ArrayList<Integer> >();
		ArrayList<Integer> overlapsWithX = new ArrayList<Integer>();
		Vector<Long> maxBandwidth = new Vector<Long>();
		long maxBWUsed = 0;
		boolean[] checkedIndex = new boolean[allUses.size()];
							
		for(int i = 0; i < allUses.size()-1; i++)
		{
			long startI = allUses.get(i).getStartTime();
			long endI = allUses.get(i).getEndTime();
			
			ArrayList<Integer> overlapsWithI = new ArrayList<Integer>();
		
			for(int j = i+1; j < allUses.size(); j++)
			{					
				long startJ = allUses.get(j).getStartTime();
				long endJ = allUses.get(j).getEndTime();
				
				// Reservations I and J overlap in time
				if(((startI >= startJ) && (endI >= endJ) && (startI <= endJ)) || ((startI <= startJ) && (endI <= endJ) && (endI >= startJ)) || ((startI <= startJ) && (endI >= endJ)) || ((startI >= startJ) && (endI <= endJ)))
				{
					overlapsWithI.add(j);
				}
			}
			
			overlaps.add(overlapsWithI);
		}
							
		for(int y = 0; y < allUses.size(); y++)
		{
			long startY = allUses.get(y).getStartTime();
			long endY = allUses.get(y).getEndTime();
			
			// Reservations X and Y overlap in time
			if(((startX >= startY) && (endX >= endY) && (startX <= endY)) || ((startX <= startY) && (endX <= endY) && (endX >= startY)) || ((startX <= startY) && (endX >= endY)) || ((startX >= startY) && (endX <= endY)))
			{
				overlapsWithX.add(y);
			}				
		}
								
		for(Integer oneIndex : overlapsWithX)
		{
			if(!checkedIndex[oneIndex])
			{
				Vector<Long> oneBW = exploreOverlappingReservations(overlaps, allUses, oneIndex, overlapsWithX, checkedIndex);
				
				for(Long oneSum : oneBW)
				{
					maxBandwidth.addElement(new Long(oneSum));
				}
			}					
		}
		
		for(Long mostBW : maxBandwidth)
		{
			if(mostBW >= maxBWUsed)
				maxBWUsed = mostBW;
		}
		
		return (bandwidthCapacity - maxBWUsed);
	}
	
	// Added by Jeremy for AR Bandwidth availability check. This recursive function identifies compounded overlaps and correctly calculates total bandwidth consumption among a set of overlapping (and partially overlapping) reservations.
	/**
	 * Calculate the total bandwidth consumption among a set of bandwidth uses.
	 * @param allOverlaps
	 * @param allRes
	 * @param index
	 * @param overlapsWithX
	 * @param checkedAlready
	 * @return The total bandwidth consumed across the set of bandwidth reservations
	 */
	private Vector<Long> exploreOverlappingReservations(ArrayList< ArrayList<Integer>> allOverlaps, ArrayList<FlowLinkAvailabilityTuple> allRes, int index, ArrayList<Integer> overlapsWithX, boolean[] checkedAlready)
	{
		Vector<Long> allSums = new Vector<Long>();
		
		long bandwidthOverlap = allRes.get(index).getConsumedBandwidth();
		checkedAlready[index] = true;
		
		if(allOverlaps.isEmpty() || index >= allOverlaps.size())
		{
			allSums.add(bandwidthOverlap);
			return allSums;
		}
		
		if(allOverlaps.get(index).isEmpty())
		{
			allSums.add(bandwidthOverlap);
		}
		else
		{			
			for(int i = 0; i < allOverlaps.get(index).size(); i++)
			{
				if(!checkedAlready[allOverlaps.get(index).get(i)] && overlapsWithX.contains(allOverlaps.get(index).get(i)))
				{
					Vector<Long> subSums = exploreOverlappingReservations(allOverlaps, allRes, allOverlaps.get(index).get(i), overlapsWithX, checkedAlready);
				
					long bwTotal = bandwidthOverlap;
					
					for(Long oneVal : subSums)
					{
						bwTotal += oneVal;
					}
					
					allSums.add(bwTotal);
				}
			}
		}
		
		if(allSums.isEmpty())
		{
			allSums.add(bandwidthOverlap);
		}
		
		return allSums;
	}
	
}
