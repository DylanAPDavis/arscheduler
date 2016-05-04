package net.floodlightcontroller.arscheduler;

/**
 * Represents a port on a node. A node can have a number of ports
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class Port 
{
	/**
	 * The port's ID number
	 */
	private int portID;
	/**
	 * The port's name
	 */
	private String portName;
	
	/**
	 * Construct port using the number and name
	 * @param num
	 * @param name
	 */
	public Port(int num, String name)
	{
		portID = num;
		portName = name;
	}
	
	/**
	 * Get the ID
	 * @return ID integer
	 */
	public int getID()
	{
		return portID;
	}
	
	/**
	 * Get the name
	 * @return name string
	 */
	public String getName()
	{
		return portName;
	}
	
}
