package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;

/**
 * Wrapper for an ArrayList of Flow Route Tuples, keeping track of each Flow and it associated route.
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class FlowTable 
{
	/**
	 * The list of FlowRouteTuples
	 */
	private ArrayList<FlowRouteTuple> allFlowRoutes;
	
	/**
	 * Construct the FlowTable with a new list of FlowRouteTuples
	 */
	public FlowTable()
	{
		allFlowRoutes = new ArrayList<FlowRouteTuple>();
	}
	
	/**
	 * Add a FlowRouteTuple to the list.
	 * @param theTuple
	 */
	public void addFlowRouteTupleToFlowTable(FlowRouteTuple theTuple)
	{
		allFlowRoutes.add(theTuple);
	}
	
	/**
	 * Remove every FlowRouteTuple from the list
	 */
	public void removeAllFlowRouteTuples()
	{
		allFlowRoutes.clear();
	}
	
	/**
	 * Find a matching FlowRouteTuple from this FlowTable given a Flow
	 * @param theFlow
	 * @return the matching FlowRouteTuple
	 */
	public FlowRouteTuple findApplicableFlowRouteTuple(Flow theFlow)
	{
		
		
		for(FlowRouteTuple oneFlowRouteTuple : allFlowRoutes)
		{
			if(oneFlowRouteTuple.getFlow().equals(theFlow))
			{
				return oneFlowRouteTuple;
			}
		}
		
		return null;
	}
	
	/**
	 * Find a matching FlowRouteTuple given just the Flow ID. 
	 * Note: Flow IDs are unique.
	 * @param flowID
	 * @return the matching FlowRouteTuple
	 */
	public FlowRouteTuple matchFlow(long flowID)
	{
		for(FlowRouteTuple oneRouteTuple : allFlowRoutes)
		{
			Flow oneFlow = oneRouteTuple.getFlow();
			
			if((oneFlow.getID() == flowID))
			{
				return oneRouteTuple;
			}
		}
		
		return null;
	}
	
	/**
	 * Status of Flow Route Tuple list
	 * @return True if empty, False if not
	 */
	public boolean isEmpty()
	{
		return allFlowRoutes.isEmpty();
	}
	
	/**
	 * Gets the list of FLowRouteTuples
	 * @return ArrayList of tuples
	 */
	public ArrayList<FlowRouteTuple> getAllFlowRouteTuples()
	{
		return allFlowRoutes;
	}
	
}
