package net.floodlightcontroller.arscheduler;

/**
 * Wrapper for a Flow and shortest path Topology combination
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class FlowRouteTuple {
	/**
	 * The Flow
	 */
	private Flow theFlow;
	/**
	 * Topology representing the shortest path
	 */
	private Topology theShortestPathTopology;
	/**
	 * Indicates that this flow/route combination has been released, and should no longer take up network resources.
	 */
	private boolean released;
	
	/**
	 * Construct a FlowRouteTuple using a Flow and a shortest path Topology
	 * @param f
	 * @param t
	 */
	public FlowRouteTuple(Flow f, Topology t)
	{
		theFlow = f;
		theShortestPathTopology = t;
		setReleased(false);
	}
	
	/**
	 * Get the Flow
	 * @return Flow for this tuple.
	 */
	public Flow getFlow()
	{
		return theFlow;
	}
	
	/**
	 * Get the shortest path
	 * @return Topology for this tuple.
	 */
	public Topology getShortestPathtopology()
	{
		return theShortestPathTopology;
	}

	/**
	 * Check if this flow/route tuple has been released.
	 * @return
	 */
	public boolean isReleased() {
		return released;
	}

	/**
	 * Change the released status of this FlowRouteTuple
	 * @param released
	 */
	public void setReleased(boolean released) {
		this.released = released;
	}
	
	
}
