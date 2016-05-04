package net.floodlightcontroller.arscheduler;

/**
 * A Flow request. A request is established from a source host to a destination host for a certain time period, reserving a certain amount of bandwidth.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class Flow 
{
	/**
	 * The source node
	 */
	private Node srcNode;
	/**
	 * The destination node
	 */
	private Node dstNode;
	/**
	 * The source IP address
	 */
	private String srcIP;
	/**
	 * The destination IP address
	 */
	private String dstIP;
	/**
	 * The Flow's unique ID
	 */
	private static long classFlowID = 0;
	private long flowID;
	/**
	 * The request bandwidth in bits per second
	 */
	private long bandwidth;  // bps
	
	//startTime & endTime - number of seconds since 00:00
	private long startTime;
	private long endTime;
	/**
	 * Indicate if the flow was successfully provisioned
	 */
	private boolean successfullyScheduled;
	
	/**
	 * Construct a flow given the passed in parameters from a user.
	 * @param srcAddr
	 * @param dstAddr
	 * @param bw
	 * @param start
	 * @param end
	 * @param srcIP
	 * @param dstIP
	 */
	public Flow(Node srcAddr, Node dstAddr, long bw, long start, long end, String srcIP, String dstIP)
	{
		srcNode = srcAddr;
		dstNode = dstAddr;
		flowID = ++classFlowID;
		bandwidth = bw;
		startTime = start;
		endTime = end;
		this.srcIP = srcIP;
		this.dstIP = dstIP;
		
		successfullyScheduled = false;
	}
	
	public void schedulingSuccess(boolean success)
	{
		successfullyScheduled = success;
	}
	
	public boolean getSuccess()
	{
		return successfullyScheduled;
	}
	
	public Node getSource()
	{
		return srcNode;
	}
	
	public Node getDest()
	{
		return dstNode;
	}
	
	public long getID()
	{
		return flowID;
	}
	
	public long getBandwidth()
	{
		return bandwidth;
	}
	
	public String getSrcIP(){
		return srcIP;
	}
	
	public String getDstIP(){
		return dstIP;
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public long getEndTime(){
		return endTime;
	}
	
	/**
	 * Test for flow equality with another flow.
	 * @param anotherFlow
	 * @return True if the two flows are equal, False otherwise
	 */
	public boolean equals(Flow anotherFlow)
	{
		if(this.flowID == anotherFlow.getID())
		{
			if(this.srcNode.equals(anotherFlow.getSource()))
			{
				if(this.dstNode.equals(anotherFlow.getDest()))
				{
					if(this.bandwidth == anotherFlow.getBandwidth())
					{
						return true;
					}
				}
			}
		}
			
		return false;
	}

	/**
	 * Convert the Flow to a string object.
	 * @return The String representation of the flow.
	 */
	@Override
	public String toString() {
		return "Flow [srcNode=" + srcNode + ", dstNode=" + dstNode
				+ ", flowID=" + flowID + ", bandwidth=" + bandwidth
				+ ", startTime=" + startTime + ", endTime=" + endTime
				+ ", successfullyScheduled=" + successfullyScheduled + "]";
	}
	

}