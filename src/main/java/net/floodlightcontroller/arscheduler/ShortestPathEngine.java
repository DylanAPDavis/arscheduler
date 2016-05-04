package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

/**
 * Finds the short path topology given a network topology.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class ShortestPathEngine 
{
	/**
	 * The initial topology
	 */
	Topology workingTopology;
	
	/**
	 * Construct the ShortestPathEngine given an initial topology
	 * @param topo
	 */
	public ShortestPathEngine(Topology topo)
	{
		workingTopology = topo;
	}
	
	/**
	 * Calculates the shortest path available between the source and destination specified by a flow
	 * @param flow
	 * @return the topology representing the shortest path (set of nodes and links)
	 */
	public Topology calculateSP(Flow flow)
	{
		Topology spTopology;
		WeightedGraph weightedGraph = new WeightedGraph(workingTopology); 		// Convert Topology to adjacency matrix.
		int[] prevNodes = dijkstrasAlgorithm(weightedGraph, flow.getSource());	// Dijkstra.
		ArrayList<Node> shortestPath = reportSP(weightedGraph, prevNodes, flow.getSource(), flow.getDest());
		ArrayList<FlowLink> linksOnPath;
				
		linksOnPath = this.getLinksOnSP(shortestPath);
		
		spTopology = new Topology(shortestPath, linksOnPath);
				
		return spTopology;		
	}

	/**
	 * Perform Dijkstra's algorithm to find the shortest weighted path between the source node and all other nodes
	 * @param G
	 * @param srcNode
	 * @return integer array of node indices
	 */
	public int[] dijkstrasAlgorithm(WeightedGraph G, Node srcNode) 
	{
		int s = G.indexOf(srcNode);
		int[] dist = new int[G.size()];  // shortest known distance from "s"
		int[] pred = new int[G.size()];  // preceeding node in path
		boolean[] visited = new boolean[G.size()]; // all false initially

		for(int i = 0; i < dist.length; i++) 
		{
			dist[i] = Integer.MAX_VALUE;
		}
		dist[s] = 0;

		for(int i = 0; i < dist.length; i++) 
		{
			final int next = minVertex(dist, visited);
			
			visited[next] = true;
	  
			// The shortest path to next is dist[next] and via pred[next].

			final int[] n = G.neighbors(next);

			for(int j = 0; j < n.length; j++) 
			{
				final int v = n[j];
				final int d = dist[next] + G.getWeight(next,v);
				if(dist[v] > d) 
				{
					dist[v] = d;
					pred[v] = next;
				}
			}
		}
		pred[s] = s;
		
		return pred;  // (ignore pred[s]==0!)
	}
	  
	/**
	 * Returns an unvisited vertex
	 * @param dist
	 * @param v
	 * @return
	 */
	private int minVertex(int[] dist, boolean[] v) 
	{
		int x = Integer.MAX_VALUE;
		int y = -1;   // graph not connected, or no unvisited vertices
		for(int i = 0; i < dist.length; i++) 
		{
			if (!v[i] && (dist[i] < x)) 
			{
				y=i; 
				x=dist[i];
			}
		}

		return y;
	}

	/**
	 * Output the shortest path as a list of nodes given output from Dijkstra's algorithm
	 * @param G
	 * @param pred
	 * @param src
	 * @param dst
	 * @return the list of nodes in the path
	 */
	public ArrayList<Node> reportSP(WeightedGraph G, int[] pred, Node src, Node dst) 
	{
		int s = G.indexOf(src);
		int d = G.indexOf(dst);
		
		ArrayList<Node> path = new ArrayList<Node>();
		int x = d;
		
		while(x != s) 
		{
			path.add(0, G.getNodeValueAtIndex(x));
			x = pred[x];
		}
		
		path.add(0, G.getNodeValueAtIndex(s));
		
		return path;
	}
	
	/**
	 * Get the links that make up the shortest path
	 * Note: as flows are bidirectional, this shortest path contains both links going towards the destination, and towards the source.
	 * @param spNodes
	 * @return the list of links in the shortest (bidirectional) path
	 */
	private ArrayList<FlowLink> getLinksOnSP(ArrayList<Node> spNodes)
	{
		ArrayList<FlowLink> linksToInclude = new ArrayList<FlowLink>();
		ArrayList<FlowLink> unprunedLinks = workingTopology.getLinks(); 
		for(int i = 0; i < spNodes.size() - 1; i++)
		{
			int j = i + 1;
			
			Node nodeI = spNodes.get(i);
			Node nodeJ = spNodes.get(j);
			
			for(FlowLink link : unprunedLinks)
			{
				if(link.getSrcNode().equals(nodeI) && link.getDstNode().equals(nodeJ))
				{
					linksToInclude.add(link);
				}
				if(link.getDstNode().equals(nodeI) && link.getSrcNode().equals(nodeJ))
				{
					linksToInclude.add(link);
				}
			}
		}
		
		return linksToInclude;
	}
	
}
