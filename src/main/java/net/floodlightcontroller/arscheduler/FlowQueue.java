package net.floodlightcontroller.arscheduler;

/**
 * Represents a queue to which a flow is assigned. There can be a number of queues per port on a switch.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class FlowQueue{
	/** 
	 * Which port this queue is assigned to.
	 */
	protected int portNum;
	/**
	 * The queue's ID. A portNum & queue ID combination are unique on a switch.
	 */
	protected long queueID;
	/**
	 * The max/min bandwidth supported by this queue.
	 */
	protected long bandwidth;
	/**
	 * Indicates that this queue is currently in use
	 */
	protected boolean used;
	/**
	 * The flow currently assigned to this queue
	 */
	protected long flowID;
	
	/**
	 * Construct a FlowQueue using a port number, queue id, and bandwidth. 
	 * @param pNum
	 * @param qId
	 * @param bw
	 */
	public FlowQueue(int pNum, long qId, long bw){
		portNum = pNum;
		queueID = qId;
		bandwidth = bw;
		used = false;
		flowID = -1;
	}
	
	/**
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 * GETTERS AND SETTERS
	 * portNum, queueId, bandwidth, used, flowID
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 */
	public int getPortNum() {
		return portNum;
	}

	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	public long getQueueID() {
		return queueID;
	}

	public void setQueueID(long queueId) {
		this.queueID = queueId;
	}

	public long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public long getFlowID() {
		return flowID;
	}

	public void setFlowID(long flowID) {
		this.flowID = flowID;
	}

}
