package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.topology.ITopologyService;




/**********************************************************
 * NOTE:
 * ADDED IF STATEMENT TO FORWARDING MODULE SO IT ONLY 
 * ANDLES PROVISIONING FLOW RULES FOR ARP PACKETS NOW.
 * REMOVE THIS IF STATEMENT IF NOT USING ARSCHEDULER.
 * LOOK IN net.floodlightcontroller.forwarding.Forwarding
 * Function: processPacketInMessage
 **********************************************************/


/**
 * Coordinator for provisioning Advance Reservation Scheduling requests. Acts as the main class for the ARScheduler module.
 *  @author Dylan Davis and Jeremy Plante
 *
 */
public class ARScheduler implements IFloodlightModule, IARSchedulerService, IOFMessageListener {
	
	/*
	 * Floodlight Services
	 */
	protected IFloodlightProviderService floodlightProvider;
	protected ITopologyService topologyService;
	protected IDeviceService deviceManagerService;
	protected IOFSwitchService switchService;
	protected IRestApiService restApiService;
	protected static Logger logger;
	protected OFFactory of13Factory;
	
	/*
	 * Floodlight Devices/Links/Switchs/Ports/Queues in the topology
	 */
	protected FloodlightTopology floodlightTopology;
	
	/*
	 * The Resource Manager - tracks bandwidth/time usage across the topology
	 */
	protected ResourceManager theRM;
	/*
	 * The Flow Scheduler - handles determining what network elements can be used to meet
	 * a request's bandwidth/time demand
	 */
	protected FlowScheduler scheduler;
	
	/*
	 * Floodlight Topology Builder - makes the calls to Floodlight services to construct
	 * the structures used to represent Floodlight's view of the topology
	 */
	protected FloodlightTopologyBuilder floodlightTopoBuilder;
	/*
	 * Flow Provisioner - pushes flow rules out to each switch in a given path
	 */
	protected FlowProvisioner flowProvisioner;
	
	/*
	 * Topology Builder - constructs our view of the topology (i.e. FlowLinks versus Links,
	 * which have additional information necessary for calculating the shortest available
	 * path for a request)
	 */
	protected TopologyBuilder topoBuilder;
	
	
	 /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *   Reservation Handlers
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	
	@Override
	/**
	 * Handles a new flow request by calling the Flow Scheduler and spawning a SchedulingThread if the request can be
	 * scheduled successfully.
	 */
	public String handleNewFlow(Flow flow)
	{
		long flowID = flow.getID();
		flow = scheduler.scheduleNewFlow(flow);
		
		if(flow == null)
			return "Flow " + flowID + " reservation FAILED";
		else
		{
			int startHour = (int)(flow.getStartTime() / 60 / 60);
			int startMinute = (int)(flow.getStartTime() / 60 % 60);
			int startSecond = 0;	
			
			// Create and start Thread to delay scheduling in the flowProvisioner. The call commented out below is now done in ScheduleThread class.
			SchedulingThread schedulingThread = new SchedulingThread(this, flow, startHour, startMinute, startSecond, logger);
			/*this.flowProvisioner.provisionFlowPath(this.theRM.getFlowFromRM(flow), this.switchMap, this.switchQueueMap); */
			
			return "Flow " + flow.getID() + " reservation SUCCESS";
		}
	}
	
	/**
	 * Releases a flow, and the associated network resources, through use of the
	 * Flow Scheduler.
	 * @param flowTuple - the combination of a Flow and the path associated with
	 * that flow
	 * @return Success or Failure of the release
	 */
	public String releaseExpiredFlow(FlowRouteTuple flowTuple)
	{
		boolean success = scheduler.releaseExpiredFlow(flowTuple);
				
		if(success == false)
			return "Flow " + flowTuple.getFlow().getID() + " release FAILED";
		else
			return "Flow " + flowTuple.getFlow().getID() + " release SUCCESS";
	}
	

	/**
	 * Initializes the state of the network, polling Floodlight for the most up-to-date view of
	 * the topology (devices, links, switches, ports, queues).
	 */
	@Override 
	public ArrayList<Node> initializeState(){
		floodlightTopology = floodlightTopoBuilder.createFloodlightTopology();
		
		Topology newTopology = topoBuilder.convertTopology(floodlightTopology);
		this.theRM.intializeState(newTopology);
		return this.getTopology().getNodes();
	}
	
	/**
	 * Returns the Resource Manager's view of the topology
	 */
	@Override
	public Topology getTopology(){
		return theRM.getTopology();
	}
	
	
	/**
	 * Return the name of this module
	 */
	@Override
	public String getName() {
	    return ARScheduler.class.getSimpleName();
	}
	
    /**
     * Return the list of interfaces that this module implements.
     * All interfaces must inherit IFloodlightService
     * @return
     */
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> our_services = new ArrayList<Class<? extends IFloodlightService>>();
		our_services.add(IARSchedulerService.class);
		return our_services;
	}

	/**
     * Instantiate (as needed) and return objects that implement each
     * of the services exported by this module.  The map returned maps
     * the implemented service to the object.  The object could be the
     * same object or different objects for different exported services.
     * @return The map from service interface class to service implementation
     */
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> our_service_map = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		our_service_map.put(IARSchedulerService.class, this);
		return our_service_map;
	}

    /**
     * Get a list of Modules that this module depends on.  The module system
     * will ensure that each these dependencies is resolved before the
     * subsequent calls to init().
     * @return The Collection of IFloodlightServices that this module depnds
     *         on.
     */
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> services = new ArrayList<Class<? extends IFloodlightService>>();
		services.add(IFloodlightProviderService.class);
		services.add(ITopologyService.class);
		services.add(IDeviceService.class);
		services.add(IRestApiService.class);
		services.add(ILinkDiscoveryService.class);
		return services;
	}

    /**
     * This is a hook for each module to do its <em>internal</em> initialization,
     * e.g., call setService(context.getService("Service"))
     *
     * All module dependencies are resolved when this is called, but not every module
     * is initialized.
     *
     * @param context
     * @throws FloodlightModuleException
     */
	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		this.floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		this.deviceManagerService = context.getServiceImpl(IDeviceService.class);
		this.topologyService = context.getServiceImpl(ITopologyService.class);
		this.switchService = context.getServiceImpl(IOFSwitchService.class);
		this.restApiService = context.getServiceImpl(IRestApiService.class);
		logger = LoggerFactory.getLogger(ARScheduler.class);

		this.of13Factory =  OFFactories.getFactory(OFVersion.OF_13);
		
		this.theRM = new ResourceManager(logger);
		this.scheduler = new FlowScheduler(theRM, logger);
		this.floodlightTopoBuilder = new FloodlightTopologyBuilder(this);
		this.flowProvisioner = new FlowProvisioner(this);
		this.topoBuilder = new TopologyBuilder();
		
	}

    /**
     * This is a hook for each module to do its <em>external</em> initializations,
     * e.g., register for callbacks or query for state in other modules
     *
     * It is expected that this function will not block and that modules that want
     * non-event driven CPU will spawn their own threads.
     *
     * @param context
     */
	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		logger.info("Starting {}", this.getName());
		restApiService.addRestletRoutable(new ARSchedulerWebRoutable());
	    floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
	    //switchService.addOFSwitchListener(this);
	}

	
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Receives messages before the Forwarding module
	 * Forwarding handles only ARP packets if the ARScheduler is being used
	 */
	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
        return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
	}

	/**
	 * Listen for FLOW_REMOVED messages, update the RM's view of the topology
	 * by releasing the resources associated with that flow.
	 */
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		logger.info(msg.toString());
		logger.info(sw.toString());
		
		OFMessageParser parser = new OFMessageParser();
		parser.parseMessage(msg.toString());
		
		FlowRouteTuple flowTupleToRelease = theRM.getFlowTable().matchFlow(parser.flowID);
		assert(flowTupleToRelease != null);
		
		releaseExpiredFlow(flowTupleToRelease);
		releaseQueuesOnSwitch(sw, flowTupleToRelease.getFlow());
		
		return Command.CONTINUE;
	}
	
	/**
	 * Marks all queues associated with a Flow on a switch as unused
	 * @param sw - The switch that has removed a flow rule
	 * @param flowToRelease - The Flow associated with that flow rule
	 */
	protected void releaseQueuesOnSwitch(IOFSwitch sw, Flow flowToRelease) {
		// TODO Auto-generated method stub
		Collection<OFPortDesc> ports = sw.getPorts();
		for(OFPortDesc portDesc : ports){
			
			int portNum = portDesc.getPortNo().getPortNumber();
			HashMap<Integer, ArrayList<FlowQueue>> portNumQueueMap = floodlightTopology.getSwitchQueueMap().get(sw);
			
			for(Integer portNumInMap : portNumQueueMap.keySet()){
				
				if(portNumInMap.intValue() == portNum){
					
					ArrayList<FlowQueue> queues = portNumQueueMap.get(portNumInMap);
					
					for(int queueIndex = 0; queueIndex < queues.size(); queueIndex++){
						
						FlowQueue queue = queues.get(queueIndex);
						if(queue.getFlowID() == flowToRelease.getID()){
							floodlightTopology.getSwitchQueueMap().get(sw).get(portNumInMap).get(queueIndex).setUsed(false);
							floodlightTopology.getSwitchQueueMap().get(sw).get(portNumInMap).get(queueIndex).setFlowID(-1);
						}
					}
				}
			}
		}
	}
	
	public FloodlightTopology getFloodlightTopology(){
		return this.floodlightTopology;
	}
}
