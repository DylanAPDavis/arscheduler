package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.routing.Link;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Constructs a Topology object given a collection of Floodlight objects that represent that network.
 * This Topology object is used for tracking resource usage related to scheduled flows.
 * @author Dylan Davis and Jeremy Plante
 *
 */
public class TopologyBuilder {
	
	/**
	 * Convert Floodlight objects into a Topology. 
	 * @param floodlightTopology
	 * @return Topology object
	 */
	public Topology convertTopology(FloodlightTopology floodlightTopology)
	{
		ArrayList<IDevice> devices = floodlightTopology.getDevices();
		ArrayList<Link> links = floodlightTopology.getLinks();
		Map<DatapathId, IOFSwitch> switchMap = floodlightTopology.getSwitchMap();
		HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap = floodlightTopology.getSwitchPortMap();

		ArrayList<Node> topoNodes = new ArrayList<Node>();
		ArrayList<FlowLink> topoLinks = new ArrayList<FlowLink>();
		//Create Switch Nodes
		for(DatapathId dpID : switchMap.keySet()){
			String name = dpID.toString();
			int numPorts = switchPortMap.get(switchMap.get(dpID)).size();
			topoNodes.add(new Switch(name, numPorts));
		}
		//Create Host Nodes
		//Create (Host, Switch) Links
		for(IDevice device : devices){
			String name = device.getMACAddressString();
			SwitchPort[] attachmentPoints = device.getAttachmentPoints();
			for (SwitchPort ap : attachmentPoints){
				if(ap.getPort().getPortNumber() > 0){
					Host newHost = new Host(name, 1);
					topoNodes.add(newHost);
					ArrayList<FlowLink> hostSwitchLinks = makeHostSwitchLinkPair(topoNodes, device, ap, switchMap, switchPortMap);
					topoLinks.add(hostSwitchLinks.get(0));
					topoLinks.add(hostSwitchLinks.get(1));
				}
			}
		}
		//Create (Switch, Switch) Links
		for(Link link : links){
			FlowLink switchSwitchLink = makeSwitchSwitchLink(topoNodes, link, switchMap, switchPortMap);
			topoLinks.add(switchSwitchLink);
		}
		
		return new Topology(topoNodes, topoLinks);
	}
	
	/**
	 * Get a node with the given name from a list of nodes 
	 * @param nodes
	 * @param name
	 * @return the given node (null if node found)
	 */
	public Node getNodeWithName(ArrayList<Node> nodes, String name){
		for(Node n : nodes){
			if(n.getNodeName().equals(name)){
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Construct a link between two switches.
	 * @param topoNodes
	 * @param link
	 * @param switchMap
	 * @param switchPortMap
	 * @return the link connecting the two given switches
	 */
	public FlowLink makeSwitchSwitchLink(ArrayList<Node> topoNodes,Link link, Map<DatapathId, IOFSwitch> switchMap, HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap){
		DatapathId srcId = link.getSrc();
		DatapathId dstId = link.getDst();
		
		IOFSwitch srcSwitch = switchMap.get(srcId);
		IOFSwitch dstSwitch = switchMap.get(dstId);
		
		OFPort srcPort = link.getSrcPort();
		OFPort dstPort = link.getDstPort();
		
		int srcPortNum = srcPort.getPortNumber();
		int dstPortNum = dstPort.getPortNumber();
		ArrayList<OFPortDesc> srcPorts = switchPortMap.get(srcSwitch);
		ArrayList<OFPortDesc> dstPorts = switchPortMap.get(dstSwitch);
		
		String linkName = "(" + srcId.toString() + ", " + dstId.toString() + ")";
		Node src = getNodeWithName(topoNodes, srcId.toString());
		Node dst = getNodeWithName(topoNodes, dstId.toString());
		Port srcP = src.getPortByID(srcPortNum);
		Port dstP = dst.getPortByID(dstPortNum);
		
		long bandWidth = 0;
		for(OFPortDesc portDesc : srcPorts){
			if(portDesc.getPortNo().equals(srcPort)){
				bandWidth = convertBandwidth(portDesc.getCurrSpeed());
			}
		}
		return new FlowLink(linkName, src, dst, srcP, dstP, bandWidth);
	}
	
	/**
	 * Convert given reportedBandwidth from Megabits to bits
	 * @param reportedBandwidth
	 * @return the given bandwidth in terms of bits
	 */
	public long convertBandwidth(long reportedBandwidth){
		//CONVERT FROM MEGABITS TO BITS
		long newBandwidth = reportedBandwidth * 1000;
		return newBandwidth;
	}
	
	/**
	 * Construct a link between a host and a switch
	 * @param topoNodes
	 * @param device
	 * @param ap
	 * @param switchMap
	 * @param switchPortMap
	 * @return the link which connects given device and access point
	 */
	public ArrayList<FlowLink> makeHostSwitchLinkPair(ArrayList<Node> topoNodes, IDevice device, SwitchPort ap, Map<DatapathId, IOFSwitch> switchMap, 
			HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap){
		
		String deviceName = device.getMACAddressString();
				
		DatapathId switchId = ap.getSwitchDPID();
		String switchName = switchId.toString();
		
		IOFSwitch connectingSwitch = switchMap.get(switchId);
		ArrayList<OFPortDesc> ports = switchPortMap.get(connectingSwitch);
		
		OFPort portOnSwitch = ap.getPort();
		int switchPortNum = portOnSwitch.getPortNumber();
		
		String hostSwitchLinkName = "(" + deviceName + ", " + switchName + ")";
		String switchHostLinkName = "(" + switchName + ", " + deviceName + ")";
		

		Node src = getNodeWithName(topoNodes, deviceName);
		Node dst = getNodeWithName(topoNodes, switchName);
		
		Port srcP = src.getPortByID(1);
		Port dstP = dst.getPortByID(switchPortNum);
		
		long bandWidth = 0;
		
		for(OFPortDesc portDesc : ports){
			if(portDesc.getPortNo().equals(portOnSwitch)){
				bandWidth = convertBandwidth(portDesc.getCurrSpeed());
			}
		}
		
		ArrayList<FlowLink> hostSwitchLinks = new ArrayList<FlowLink>();
		hostSwitchLinks.add(new FlowLink(hostSwitchLinkName, src, dst, srcP, dstP, bandWidth));
		hostSwitchLinks.add(new FlowLink(switchHostLinkName, dst, src, dstP, srcP, bandWidth));
		
		return hostSwitchLinks;
	}
}
