package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.floodlightcontroller.core.IOFSwitch;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetQueue;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;

/**
 * Given a Floodlight topology and a FlowRouteTuple, provision the matching flow rules on each switch in the path.
 * NOTE: ARP is handled by the Forwarding module.
 * @author Dylan Davis
 * @author Jeremy Plante
 */
public class FlowProvisioner {
	/**
	 * Factory for building Floodlight classes for provisioning flow rules
	 */
	protected OFFactory of13Factory;
	/**
	 * Logger for creating output
	 */
	protected Logger logger;
	
	/**
	 * Construct the FlowProvisioner using the ARScheduler
	 * @param theScheduler
	 */
	public FlowProvisioner(ARScheduler theScheduler){
		this.of13Factory = theScheduler.of13Factory;
		this.logger = theScheduler.logger;
	}
	
	/**
	 * Provision the appropriate flow rules on each switch along a path
	 * @param flowRouteTuple
	 * @param switchMap
	 * @param switchQueueMap
	 */
	public void provisionFlowPath(FlowRouteTuple flowRouteTuple, Map<DatapathId, IOFSwitch> switchMap, 
			HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap){
		//Source Node and Destination Node Information
		Flow flow = flowRouteTuple.getFlow();
		Node srcNode = flow.getSource();
		Node dstNode = flow.getDest();
		
		
		//Path Information
		Topology topology = flowRouteTuple.getShortestPathtopology();
		ArrayList<FlowLink> pathLinks = topology.getLinks();
		
		HashMap<Node, ArrayList<Port>> usedPortsMap = buildUsedPortsMap(new HashMap<Node, ArrayList<Port>>(), pathLinks);

		provisionFlowRules(flow, srcNode, dstNode, usedPortsMap, switchMap, switchQueueMap);
	}
	
	/**
	 * Build a map showing which ports are used along the shortest path
	 * @param usedPortsMap
	 * @param pathLinks
	 * @return HashMap Showing which ports are being used per switch along the provisioned path
	 */
	public HashMap<Node, ArrayList<Port>> buildUsedPortsMap(HashMap<Node, ArrayList<Port>> usedPortsMap, ArrayList<FlowLink> pathLinks){
		//Build a map of switches to used ports for constructing flow rules
		for(FlowLink link : pathLinks){
			Node linkSrcNode = link.getSrcNode();
			Port linkSrcPort = link.getSrcPort();
			if(linkSrcNode.nodeIsSwitch()){
				updateUsedPortsMap(usedPortsMap, linkSrcNode, linkSrcPort);
			}
			Node linkDstNode = link.getDstNode();
			Port linkDstPort = link.getDstPort();
			if(linkDstNode.nodeIsSwitch()){
				updateUsedPortsMap(usedPortsMap, linkDstNode, linkDstPort);
			}
		}
		return usedPortsMap;
	}
	
	/**
	 * Update the used ports map
	 * @param usedPortsMap
	 * @param node
	 * @param port
	 */
	public void updateUsedPortsMap(HashMap<Node, ArrayList<Port>> usedPortsMap, Node node, Port port){
		if(!usedPortsMap.containsKey(node)){
			ArrayList<Port> usedPorts = new ArrayList<>();
			usedPorts.add(port);
			usedPortsMap.put(node, usedPorts);
		}
		else{
			if(!usedPortsMap.get(node).contains(port)){
				usedPortsMap.get(node).add(port);
			}
		}
	}
	
	/**
	 * Provision two flow rules per switch, one for each direction in the scheduler bi-directional flow
	 * @param flow
	 * @param srcNode
	 * @param dstNode
	 * @param usedPortsMap
	 * @param switchMap
	 * @param switchQueueMap
	 */
	public void provisionFlowRules(Flow flow, Node srcNode, Node dstNode, HashMap<Node, ArrayList<Port>> usedPortsMap, 
			Map<DatapathId, IOFSwitch> switchMap, HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap){
		
		Set<Node> switches = usedPortsMap.keySet();
		for(Node thisSwitch : switches){
			ArrayList<Port> usedPorts = usedPortsMap.get(thisSwitch);
			String srcIP = flow.getSrcIP();
			String dstIP = flow.getDstIP();
			//Provision bidirectional flows
			provisionFlowRule(srcIP, srcNode, dstIP, dstNode, thisSwitch, usedPorts.get(0), usedPorts.get(1), flow, switchMap, switchQueueMap);
			provisionFlowRule(dstIP, dstNode, srcIP, srcNode, thisSwitch, usedPorts.get(1), usedPorts.get(0), flow, switchMap, switchQueueMap);
		}
	}
	
	/**
	 * Provision a single flow rule on a switch to route traffic from the source host to the destination host.
	 * @param srcIPString
	 * @param srcNode
	 * @param dstIPString
	 * @param dstNode
	 * @param thisSwitch
	 * @param inPort
	 * @param outPort
	 * @param flow
	 * @param switchMap
	 * @param switchQueueMap
	 */
	public void provisionFlowRule(String srcIPString, Node srcNode, String dstIPString, Node dstNode, Node thisSwitch, Port inPort, 
			Port outPort, Flow flow, Map<DatapathId, IOFSwitch> switchMap, HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap){
		

		IOFSwitch thisIOFSwitch = switchMap.get(DatapathId.of(thisSwitch.getNodeName()));
		
		long queueId = getMatchingQueueId(flow, switchQueueMap, thisIOFSwitch, outPort);
		if(queueId == -1){
			logger.warn("NO QUEUE FOUND TO MATCH FLOW");
			return;
		}
		
		Long timeoutSeconds = getTimeoutSeconds(flow.getStartTime(), flow.getEndTime());
		
		//Create Address Object for Source and Destination
		IPv4Address srcIP = IPv4Address.of(srcIPString);
		IPv4Address dstIP = IPv4Address.of(dstIPString);
		
		// Create a match for incoming port and src/dst MAC Addresses
		Match ipMatch = of13Factory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(inPort.getID()))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_SRC, srcIP)
				.setExact(MatchField.IPV4_DST, dstIP)
				.build();

		
		// Create instructions that will be passed to switch for the above match
		OFInstructions instructions = of13Factory.instructions();
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = of13Factory.actions();
		
		//Output Packets on the specified QUEUE
		// For OpenFlow 1.0 
		
		if (of13Factory.getVersion().compareTo(OFVersion.OF_10) == 0) {
		    OFActionEnqueue enqueue = actions.buildEnqueue()
		        .setPort(OFPort.of(outPort.getID())) // Must specify port number 
		        .setQueueId(queueId)
		        .build();
		    actionList.add(enqueue);
		} else { // For OpenFlow 1.1+ 
		    OFActionSetQueue setQueue = actions.buildSetQueue()
		        .setQueueId(queueId)
		        .build();
		    actionList.add(setQueue);
		}
		
		// Output packets on the specified outgoing port
		OFActionOutput output = actions.buildOutput()
				.setPort(OFPort.of(outPort.getID()))
				.setMaxLen(Integer.MAX_VALUE)
				.build();
		actionList.add(output);
		
		
		// Create list of instructions for the flowAdd
		OFInstructionApplyActions applyActions = instructions.buildApplyActions()
				.setActions(actionList)
				.build();
		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);
		
		
		// Create flag so that switch notifies controller when flow removed
		HashSet<OFFlowModFlags> flags = new HashSet<OFFlowModFlags>();
		flags.add(OFFlowModFlags.SEND_FLOW_REM);
		
		// Create the flowAdd
		OFFlowAdd flowAddIP = of13Factory.buildFlowAdd()
				.setMatch(ipMatch)
				.setInstructions(instructionList)
				.setPriority(1)
				.setHardTimeout(timeoutSeconds.intValue())
				.setFlags(flags)
				.setCookie(U64.of(Long.valueOf(flow.getID())))
				.build();
	

		logger.info(flowAddIP.toString());
		// Write the flowAdd to the switch
		thisIOFSwitch.write(flowAddIP);
		
	}
	

	/**
	 * Calculate the duration of the flow rule based on the provided start and end time.
	 * @param startTime
	 * @param endTime
	 * @return The number of seconds that the flow rule should last before expiring.
	 */
	private Long getTimeoutSeconds(long startTime, long endTime) {
		// TODO Auto-generated method stub
		return new Long(Math.abs(endTime - startTime));
	}

	/**
	 * Get the ID for a queue that matches the provided flow, switch, and port.
	 * @param flow
	 * @param switchQueueMap
	 * @param thisSwitch
	 * @param outPort
	 * @return The queue's ID.
	 */
	private long getMatchingQueueId(Flow flow,
			HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap,
			IOFSwitch thisSwitch, Port outPort) {
		
		ArrayList<FlowQueue> queues = switchQueueMap.get(thisSwitch).get(outPort.getID());
		for(int queueIndex = 0; queueIndex < queues.size(); queueIndex++){
			FlowQueue queue = queues.get(queueIndex);
			if(queue.getBandwidth() == flow.getBandwidth() && !queue.isUsed()){
				long queueId = queue.getQueueID();
				switchQueueMap.get(thisSwitch).get(outPort.getID()).get(queueIndex).setUsed(true);
				switchQueueMap.get(thisSwitch).get(outPort.getID()).get(queueIndex).setFlowID(flow.getID());
				return queueId;
			}
		}
		return -1;
	}

}
