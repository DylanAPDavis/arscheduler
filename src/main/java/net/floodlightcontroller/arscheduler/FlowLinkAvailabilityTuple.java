package net.floodlightcontroller.arscheduler;

/**
 * Wrapper for matching bandwidth consumed during a time period.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class FlowLinkAvailabilityTuple 
{
	/**
	 * The amount of bandwidth consumed.
	 */
	private long consumedBandwidth;
	/**
	 * The start time for a flow.
	 */
	private long startTime;
	/**
	 * The end time for a flow.
	 */
	private long endTime;
	
	/**
	 * Construct a FlowLinkAvailabilityTuple using bandwidth, start time, and end time.
	 * @param bwUsed
	 * @param start
	 * @param end
	 */
	public FlowLinkAvailabilityTuple(long bwUsed, long start, long end)
	{
		consumedBandwidth = bwUsed;
		startTime = start;
		endTime = end;
	}

	/**
	 * Get the amount of bandwidth consumed.
	 * @return The bandwidth long.
	 */
	public long getConsumedBandwidth() 
	{
		return consumedBandwidth;
	}
	
	/**
	 * Set the amount of bandwidth consumed.
	 * @param newBW
	 */
	public void setConsumedBandwidth(long newBW) 
	{
		consumedBandwidth = newBW;
	}

	/**
	 * Get the start time in seconds.
	 * @return The start time long.
	 */
	public long getStartTime() 
	{
		return startTime;
	}

	/**
	 * Get the end time in seconds.
	 * @return The end time long.
	 */
	public long getEndTime() {
		return endTime;
	}
}
