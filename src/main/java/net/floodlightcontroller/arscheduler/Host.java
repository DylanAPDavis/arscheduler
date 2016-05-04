package net.floodlightcontroller.arscheduler;

/**
 * A host node.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class Host extends Node 
{
	/**
	 * Construct a node using a name and a number of ports
	 * @param name
	 * @param portNum
	 */
	public Host(String name, int portNum) 
	{
		super(name, portNum);
	}

	/**
	 * Set the node's type as Host
	 */
	@Override
	public void setNodeType() 
	{
		super.nodeType = "Host";		
	}

	/**
	 * Indicate that this host is not a switch
	 */
	@Override
	public boolean nodeIsSwitch() 
	{
		return false;
	}
}
