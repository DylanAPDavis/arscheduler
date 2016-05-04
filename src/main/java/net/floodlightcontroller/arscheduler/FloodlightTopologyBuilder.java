package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.routing.Link;
import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFQueueGetConfigReply;
import org.projectfloodlight.openflow.protocol.OFQueueGetConfigRequest;
import org.projectfloodlight.openflow.types.DatapathId;
import com.google.common.util.concurrent.ListenableFuture;
/**
 * Construct a FloodlightTopology wrapper to contain information about Floodlight's view of the network.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class FloodlightTopologyBuilder {
	
	/**
	 * The coordinating ARScheduler
	 */
	protected ARScheduler arscheduler;
	/*
	 * A hardcoded mapping from queue number to bandwidth rate
	 * Can be replaced when the max/min rate of each queue can be
	 * determined correctly through Floodlight calls
	 */
	static protected long bitsPerGb = 1000000000;

	/**
	 * Construct the builder using a reference to the ARScheduler
	 * @param arscheduler
	 */
	public FloodlightTopologyBuilder(ARScheduler arscheduler){
		this.arscheduler = arscheduler;
	}
	
	/**
	 * Create a FloodlightTopology by querying Floodlight for information about the network.
	 * @return A FloodlightTopology representation of the network
	 */
	public FloodlightTopology createFloodlightTopology(){
		ArrayList<IDevice> devices = createDevices();
		ArrayList<Link> links = createLinks();
		Map<DatapathId, IOFSwitch> switchMap = createSwitchMap();
		HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap = createPortMap(switchMap);
		HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap = createQueueMap(createQueueBandwidthMap(), switchMap, switchPortMap);
		return new FloodlightTopology(devices, links, switchMap, switchPortMap, switchQueueMap);
	}
	
	/**
	 * Create a list of devices(hosts) in the network.
	 * @return The hosts in the network
	 */
	public ArrayList<IDevice> createDevices(){
		ArrayList<IDevice> updatedDevices = new ArrayList<IDevice>();
		Collection<? extends IDevice> deviceList = arscheduler.deviceManagerService.getAllDevices();
		for (IDevice device : deviceList){
			SwitchPort[] attachmentPoints = device.getAttachmentPoints();
			for (SwitchPort ap : attachmentPoints){
				if(ap.getPort().getPortNumber() > 0){
					updatedDevices.add(device);
				}
			}
		}
		return updatedDevices;
	}
	
	/**
	 * Get a list of links in the network.
	 * @return The links in the network
	 */
	public ArrayList<Link> createLinks(){
		Map<DatapathId, Set<Link>> linkMap = arscheduler.topologyService.getAllLinks();
		ArrayList<Link> updatedLinks = new ArrayList<Link>();
		for(Set<Link> linkSet : linkMap.values()){
			for(Link l : linkSet){
				if(!updatedLinks.contains(l)){
					updatedLinks.add(l);
				}
			}
		}
		return updatedLinks;
	}
	
	/**
	 * Get a Map connecting DatapathId to IOFSwitch objects.
	 * @return Mapping of switch ID to IOFSwitch objects.
	 */
	public Map<DatapathId, IOFSwitch> createSwitchMap(){
		return arscheduler.switchService.getAllSwitchMap();
	}
	
	/**
	 * Create a map of switches to their set of ports.
	 * @param switchMap
	 * @return Mapping of IOFSwitch to ArrayList<OFPortDesc>, which contain details about each port
	 */
	public HashMap<IOFSwitch, ArrayList<OFPortDesc>> createPortMap(Map<DatapathId, IOFSwitch> switchMap){
		HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap = new HashMap<IOFSwitch, ArrayList<OFPortDesc>>();
		for(DatapathId id : switchMap.keySet()){
			IOFSwitch switchObject = switchMap.get(id);
			Collection<OFPortDesc> ports = switchObject.getPorts();
			ArrayList<OFPortDesc> usablePorts = new ArrayList<OFPortDesc>();
			for(OFPortDesc port : ports){
				//Integer portNum = port.getPortNo().getPortNumber();
				//Long portBandwidth = port.getCurrSpeed();
				if(port.getCurrSpeed() > 0){
					//getCurrSpeed returns bandwidth in Megabits
					usablePorts.add(port);
				}
			}
			switchPortMap.put(switchObject, usablePorts);
		}
		return switchPortMap;
	}
	
	/**
	 * Create a map of queues to switches, with each queue grouped by port number.
	 * @param queueBandwidthMap
	 * @param switchMap
	 * @param switchPortMap
	 * @return HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>>, ArrayList<FlowQueue> per port number per switch
	 */
	public HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> createQueueMap(HashMap<Long, Long> queueBandwidthMap, 
			Map<DatapathId, IOFSwitch> switchMap, HashMap<IOFSwitch, 
			ArrayList<OFPortDesc>> switchPortMap){
		
		HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> queueMap = new HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>>();		
		
		for(IOFSwitch thisSwitch : switchMap.values()){
			
			HashMap<Integer, ArrayList<FlowQueue>> portQueueMap = new HashMap<Integer, ArrayList<FlowQueue>>();
			for(OFPortDesc portDesc : switchPortMap.get(thisSwitch)){
				ArrayList<FlowQueue> queuesThisPort = new ArrayList<FlowQueue>();
				OFQueueGetConfigRequest cr = arscheduler.of13Factory.buildQueueGetConfigRequest().setPort(portDesc.getPortNo()).build(); // Get all queues on all ports 
				
				ListenableFuture<OFQueueGetConfigReply> future = thisSwitch.writeRequest(cr); // Send request to switch 1
				try { 
				    // Wait up to 10s for a reply; return when received; else exception thrown 
				    OFQueueGetConfigReply reply = future.get(10, TimeUnit.SECONDS);
				    // Iterate over all queues 
				    for (OFPacketQueue q : reply.getQueues()) {
				    	///queues.add(q);
				    	if(q.getQueueId() == 0)
				    		continue;
				    	FlowQueue newQueue = new FlowQueue(portDesc.getPortNo().getPortNumber(), q.getQueueId(), 
				    			queueBandwidthMap.get(Long.valueOf(q.getQueueId())));
				    	queuesThisPort.add(newQueue);
				    }
				    int portNum = portDesc.getPortNo().getPortNumber();
				    portQueueMap.put(Integer.valueOf(portNum), queuesThisPort);
				} catch (InterruptedException | ExecutionException | TimeoutException e) { 
				    e.printStackTrace();
				}
			}
			queueMap.put(thisSwitch, portQueueMap);	
	    	
		}
		
		return queueMap;
	}
	
	/**
	 * Creates the *hardcoded* mapping of queue number of bandwidth
	 * Change if queue min/max rate properties become available programmatically
	 */
	public HashMap<Long, Long> createQueueBandwidthMap(){
		/*
		queue_bandwidth_dict = {1: 1*bpGb, 2: 1*bpGb, 3: 1*bpGb, 4: 1*bpGb, 5: 1*bpGb,
	            6: 1*bpGb, 7: 1*bpGb, 8: 1*bpGb, 9: 1*bpGb, 10: 1*bpGb,
	            11: 2*bpGb, 12: 2*bpGb, 13: 2*bpGb, 14: 2*bpGb, 15: 2*bpGb,
	            16: 3*bpGb, 17: 3*bpGb, 18: 3*bpGb,
	            19: 4*bpGb, 20: 4*bpGb,
	            21: 5*bpGb, 22: 5*bpGb,
	            23: 6*bpGb,
	            24: 7*bpGb,
	            25: 8*bpGb,
	            26: 9*bpGb,
	            27: 10*bpGb}
		*/
		HashMap<Long, Long> queueBandwidthMap = new HashMap<Long, Long>();
		for(long queueNum = 1; queueNum < 28; queueNum++){
			long bandwidth = this.bitsPerGb;
			if(queueNum < 11)
				bandwidth *=  1;
			else if(queueNum < 16)
				bandwidth *= 2;
			else if(queueNum < 19)
				bandwidth *= 3;
			else if(queueNum < 21)
				bandwidth *= 4;
			else if(queueNum < 23)
				bandwidth *= 5;
			else if(queueNum == 23)
				bandwidth *= 6;
			else if(queueNum == 24)
				bandwidth *= 7;
			else if(queueNum == 25)
				bandwidth *= 8;
			else if(queueNum == 26)
				bandwidth *= 9;
			else if(queueNum == 27)
				bandwidth *= 10;
			queueBandwidthMap.put(new Long(queueNum), new Long(bandwidth));
		}
		return queueBandwidthMap;
	}
}
