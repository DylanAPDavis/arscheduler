package net.floodlightcontroller.arscheduler;

/**
 * Switch node, with a name and number of ports
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class Switch extends Node 
{

	/**
	 * Construct switch with given name and number of ports
	 * @param name
	 * @param portNum
	 */
	public Switch(String name, int portNum) 
	{
		super(name, portNum);
	}

	/**
	 * Indicate that this node is a switch
	 */
	@Override
	public void setNodeType() 
	{
		super.nodeType = "Switch";
	}

	/**
	 * Verify that this node is a swtich
	 */
	@Override
	public boolean nodeIsSwitch() 
	{
		return true;
	}
}

