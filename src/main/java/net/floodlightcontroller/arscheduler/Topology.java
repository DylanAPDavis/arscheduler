package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

/**
 * Wrapper for a list of Nodes and FlowLinks, together representing the network.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class Topology 
{
	/**
	 * The list of nodes
	 */
	private ArrayList<Node> topologyNodes = new ArrayList<Node>();
	/**
	 * The list of links
	 */
	private ArrayList<FlowLink> topologyLinks = new ArrayList<FlowLink>();
	
	/**
	 * Construct a topology given nodes and links
	 * @param topoNodes
	 * @param topoLinks
	 */
	public Topology(ArrayList<Node> topoNodes, ArrayList<FlowLink> topoLinks)
	{
		this.topologyNodes = topoNodes;
		this.topologyLinks = topoLinks;
	}
	
	/**
	 * Print out the topology
	 */
	public void dumpTopology()
	{
		System.out.println("NODES:");
		for(Node n : topologyNodes)
		{
			System.out.println(n);
		}
		
		System.out.println();
		
		System.out.println("LINKS:");
		for(FlowLink l : topologyLinks)
		{
			System.out.println(l);
		}
	}
	
	/**
	 * Output the topology as a list of strings
	 * @return ArrayList<String> representation of the topology
	 */
	public ArrayList<String> dumpTopologyString(){
		ArrayList<String> topoOutput = new ArrayList<String>();
		topoOutput.add("Nodes:");
		for(Node n : topologyNodes)
		{
			topoOutput.add(n.toString());
		}
		
		
		topoOutput.add("LINKS:");
		for(FlowLink l : topologyLinks)
		{
			topoOutput.add(l.toString());
		}
		return topoOutput;
	}
	
	/**
	 * Get the nodes
	 * @return ArrayList<Node>
	 */
	public ArrayList<Node> getNodes()
	{
		return this.topologyNodes;
	}
	
	/**
	 * Get the links
	 * @return ArrayList<FlowLink>
	 */
	public ArrayList<FlowLink> getLinks()
	{
		return this.topologyLinks;
	}
	
	/**
	 * Checks if the topology has any links
	 * @return True if the topology has no links
	 */
	public boolean isEmpty()
	{
		return topologyLinks.isEmpty();
	}

	/**
	 * Get the node with the given name
	 * @param nodeName
	 * @return the matching Node
	 */
	public Node getNodeByName(String nodeName){
		for(Node n : topologyNodes){
			if(n.getNodeName().equals(nodeName)){
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Return string presentation of the topology
	 */
	@Override
	public String toString() {
		return "Topology [topologyNodes=" + topologyNodes + ", topologyLinks="
				+ topologyLinks + "]";
	}
}

