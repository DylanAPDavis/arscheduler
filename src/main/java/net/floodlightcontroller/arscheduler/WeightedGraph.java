package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

/**
 * Representation of a topology as an adjacency matrix. Used for shortest path calculations.
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class WeightedGraph 
{
	/**
	 * The adjacency matrix (i.e. 1 if nodes are neighbors, 9999 otherwise)
	 */
	private int[][] adjacencyMatrix;
	/**
	 * Two-dimensional array list, each index representing a node pair
	 */
	private ArrayList<ArrayList<Node>> nodeMatrix = new ArrayList<ArrayList<Node>>(); 
	
	/**
	 * Construct the WeightedGraph using a Topology
	 * @param topo
	 */
	public WeightedGraph(Topology topo)
	{	
		adjacencyMatrix = new int[topo.getNodes().size()][topo.getNodes().size()];
		
		// Initialize all weights to "infinity"
		for(int x = 0; x < adjacencyMatrix.length; x++)
		{
			for(int y = 0; y < adjacencyMatrix[x].length; y++)
			{
				adjacencyMatrix[x][y] = 9999;
			}
		}
		
		for(Node m : topo.getNodes())
		{
			ArrayList<Node> oneDimension = new ArrayList<Node>();
			oneDimension.add(m);
			for(Node n : topo.getNodes())
			{
				oneDimension.add(n);
			}
			
			nodeMatrix.add(oneDimension);
		}
						
		for(FlowLink l : topo.getLinks())
		{
			Node srcNode = l.getSrcNode();
			Node dstNode = l.getDstNode();
			
			for(int n = 0; n < nodeMatrix.size(); n++)
			{
				if(srcNode.equals(nodeMatrix.get(n).get(0)))
				{
					int index = nodeMatrix.get(n).indexOf(dstNode) - 1;	// Must map from nodeMatrix to adjacencyMatrix
					
					adjacencyMatrix[n][index] = 1;
																				
					break;
				}
			}				
		}
	}
	
	/**
	 * Get the weight of an adjacencyMatrix index (i.e. a link)
	 * @param src
	 * @param dst
	 * @return
	 */
	public int getWeight(int src, int dst)
	{
		return adjacencyMatrix[src][dst];
	}
	
	/**
	 * Return the number of elements in the Adjacency Matrix
	 * @return the size
	 */
	public int size()
	{
		return adjacencyMatrix.length;
	}
	
	/**
	 * Return a list of index values, each mapped to a node that is neighboring the given node
	 * (there is a link between that neighbor and the given node).
	 * @param node
	 * @return the array of neighbor indices
	 */
	public int[] neighbors(int node)
	{
		int[] adjacentIndeces = new int[adjacencyMatrix.length];
		int[] neighbors;
		int i = 0;
		
		for(int n = 0; n < adjacencyMatrix[node].length; n++)
		{
			if(node == n)
				continue;
				
			if(adjacencyMatrix[node][n] > 0)
			{
				adjacentIndeces[i] = n;
				i++;
			}
		}
		
		neighbors = new int[i];
		
		for(int x = 0; x < i; x++)
		{
			neighbors[x] = adjacentIndeces[x];
		}
				
		return neighbors;
	}
	
	/**
	 * Find the index matching the given Node from the nodeMatrix
	 * @param node
	 * @return the index of the given node
	 */
	public int indexOf(Node node)
	{
		int i = 0;
		for(ArrayList<Node> oneDimension : nodeMatrix)
		{
			if(oneDimension.get(0).getNodeName().equals(node.getNodeName()))
			{
				return i;
			}
			
			i++;
		}
		
		return -1;
	}
	
	/**
	 * Get the node at the given index
	 * @param index
	 * @return the located node object
	 */
	public Node getNodeValueAtIndex(int index)
	{
		return nodeMatrix.get(index).get(0);
	}

	/**
	 * Output the adjacency matrix as a string
	 */
	@Override
	public String toString() {
		ArrayList<ArrayList<String>> formattedAdjacencyMatrix = new ArrayList<ArrayList<String>>();
		for(int x = 0; x < adjacencyMatrix.length; x++)
		{
			ArrayList<String> formattedAdjacencyRow = new ArrayList<String>();
			for(int y = 0; y < adjacencyMatrix[x].length; y++)
			{
				formattedAdjacencyRow.add(String.valueOf(adjacencyMatrix[x][y]));
			}
			formattedAdjacencyMatrix.add(formattedAdjacencyRow);
		}
		return "WeightedGraph [adjacencyMatrix="
				+ formattedAdjacencyMatrix.toString() + ", nodeMatrix="
				+ nodeMatrix + "]";
	}
}

