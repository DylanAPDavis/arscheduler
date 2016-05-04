package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

/**
 * A node in the topology. A node can be a switch or a host.
 * @author Dylan Davis
 * @author Jeremy Plante
 *
 */
public abstract class Node 
{
	/**
	 * The node's type (host/switch)
	 */
	protected String nodeType;
	/**
	 * The node's name (unique)
	 */
	protected String nodeName;
	/**
	 * List of ports on this node
	 */
	private ArrayList<Port> portList;
	/**
	 * Used to generate node ID
	 */
	private static int classNodeID = 0;
	/**
	 * The node's ID
	 */
	private int nodeID;
	
	/**
	 * Construct a node with a name and a number of ports
	 * @param name
	 * @param numPorts
	 */
	public Node(String name, int numPorts)
	{
		nodeName = name;
		portList = new ArrayList<Port>();
		
		for(int p = 1; p <= numPorts; p++)
		{
			Port onePort = new Port(p, "port-" + p);
			portList.add(onePort);
		}
		
		nodeID = ++classNodeID;
		
		setNodeType();
	}
	
	/**
	 * Set the node's type
	 */
	abstract public void setNodeType();
	
	/**
	 * Determine if the node is a switch
	 * @return True if it is a switch
	 */
	abstract public boolean nodeIsSwitch();
	
	/**
	 * Get the node's name
	 * @return string representing the node's name
	 */
	public String getNodeName()
	{
		return nodeName;
	}
	
	/**
	 * Get the node's type
	 * @return String representing the node's type (host/switch)
	 */
	public String getNodeType()
	{
		return nodeType;
	}
	
	/**
	 * Get list of the node's ports
	 * @return ArrayList<Port>
	 */
	public ArrayList<Port> getPorts()
	{
		return this.portList;
	}
	
	/**
	 * Get the port with the given ID
	 * @param ID
	 * @return The matching port object
	 */
	public Port getPortByID(int ID){
		for(Port p : this.portList){
			if(p.getID() == ID){
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Get this node's ID
	 * @return int ID
	 */
	public int getID()
	{
		return this.nodeID;
	}
	
	/**
	 * Represent this node as a string
	 */
	@Override
	public String toString()
	{
		return (nodeType + " " + nodeName);  
	}
	
	/**
	 * Test equality between this node and another node
	 * @param anotherNode
	 * @return True if the node's are equal
	 */
	public boolean equals(Node anotherNode)
	{
		if(this.nodeType.equals(anotherNode.nodeType))
		{
			if(this.nodeName.equals(anotherNode.nodeName))
			{
				if(this.nodeID == anotherNode.getID())
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
