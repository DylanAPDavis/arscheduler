package net.floodlightcontroller.arscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.routing.Link;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;

/**
 * Represents a collection of Floodlight objects which make up a topology.
 * @author dylan
 *
 */
public class FloodlightTopology {
	protected ArrayList<IDevice> devices;
	protected ArrayList<Link> links;
	protected Map<DatapathId, IOFSwitch> switchMap;
	protected HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap;
	protected HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap;
	
	public FloodlightTopology(ArrayList<IDevice> devices2,
			ArrayList<Link> links2, Map<DatapathId, IOFSwitch> switchMap2,
			HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap2,
			HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap2) {
		this.devices = devices2;
		this.links = links2;
		this.switchMap = switchMap2;
		this.switchPortMap = switchPortMap2;
		this.switchQueueMap = switchQueueMap2;
	}

	public ArrayList<IDevice> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<IDevice> devices) {
		this.devices = devices;
	}

	public ArrayList<Link> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<Link> links) {
		this.links = links;
	}

	public Map<DatapathId, IOFSwitch> getSwitchMap() {
		return switchMap;
	}

	public void setSwitchMap(Map<DatapathId, IOFSwitch> switchMap) {
		this.switchMap = switchMap;
	}

	public HashMap<IOFSwitch, ArrayList<OFPortDesc>> getSwitchPortMap() {
		return switchPortMap;
	}

	public void setSwitchPortMap(
			HashMap<IOFSwitch, ArrayList<OFPortDesc>> switchPortMap) {
		this.switchPortMap = switchPortMap;
	}

	public HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> getSwitchQueueMap() {
		return switchQueueMap;
	}

	public void setSwitchQueueMap(
			HashMap<IOFSwitch, HashMap<Integer, ArrayList<FlowQueue>>> switchQueueMap) {
		this.switchQueueMap = switchQueueMap;
	}
}
