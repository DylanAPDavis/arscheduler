package net.floodlightcontroller.arscheduler;

/**
 * Parse an OpenFlow "FLOW_REMOVED" message to retrieve the flow rule's cookie (i.e. Flow ID)
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class OFMessageParser 
{
	/**
	 * The Flow ID associated with the OF Message
	 */
	long flowID;  
	
	/**
	 * Parse the OF Message to retrieve a cookie	
	 * @param theMessage
	 */
	public void parseMessage(String theMessage)
	{
		String[] parsedMsg = theMessage.split(", ");
		
		for(String oneElement : parsedMsg)
		{
			if(oneElement.contains("cookie"))
			{
				String temp[] = oneElement.split("0x");
				flowID = Long.parseLong(temp[1]);
			}
		}
	}	
}

