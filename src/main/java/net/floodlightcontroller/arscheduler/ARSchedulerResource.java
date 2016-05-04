package net.floodlightcontroller.arscheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * Intermediary between the ARScheduler and the REST Interface. Hooks up supported ARScheduler services and parses user input.
 * @author dylan
 *
 */
public class ARSchedulerResource extends ServerResource{
	/**
	 * Logger for output
	 */
	protected static Logger log = LoggerFactory.getLogger(ARSchedulerResource.class);
	/**
	 * Default string for unspecified user input fields.
	 */
	protected static String notSpecified = "NOT_SPECIFIED";
	
	/**
	 * Call the ARSCheduler to initialize the network state.
	 * @return The list of nodes in the network.
	 */
	@Get("json")
	public ArrayList<Node> initializeState(){
		IARSchedulerService flowSchedService = (IARSchedulerService)getContext().getAttributes().get(IARSchedulerService.class.getCanonicalName());
		return flowSchedService.initializeState();
	}

	/**
	 * Call the ARScheduler to schedule a new flow after parsing and validating the input.
	 * @param fmJson
	 * @return String containing status of the request.
	 * @throws IOException
	 */
	@Post
	public String scheduleFlow(String fmJson) throws IOException {

		IARSchedulerService flowSchedService = (IARSchedulerService)getContext().getAttributes().get(IARSchedulerService.class.getCanonicalName());
		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp;
		
		String srcMac = notSpecified;
		String dstMac = notSpecified;
		String bandwidth = notSpecified;
		String srcIP = notSpecified;
		String dstIP = notSpecified;
		String startTime = notSpecified;
		String endTime = notSpecified;

		try {
			jp = f.createParser(fmJson);
		} catch (JsonParseException e) {
			throw new IOException(e);
		}
		jp.nextToken();
		if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected START_OBJECT");
		}

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
				throw new IOException("Expected FIELD_NAME");
			}

			String n = jp.getCurrentName();
			jp.nextToken();
			if (jp.getText().equals("")) 
				continue;

			switch(n.toLowerCase()){
			case "srcip":
				srcIP = jp.getText();
				break;
			case "srcmac":
				srcMac = jp.getText();
				break;
			case "dstip":
				dstIP = jp.getText();
				break;
			case "dstmac":
				dstMac = jp.getText();
				break;
			case "bandwidth":
				bandwidth = jp.getText();
				break;
			case "starttime":
				startTime = jp.getText();
				break;
			case "endtime":
				endTime = jp.getText();
				break;
			}
		}
		if(srcMac.equals(notSpecified) || dstMac.equals(notSpecified) || bandwidth.equals(notSpecified) 
				|| srcIP.equals(notSpecified) || dstIP.equals(notSpecified) || startTime.equals(notSpecified) || endTime.equals(notSpecified)){
			return ("{\"status\" : \"" + "ERROR: srcIP, srcMac, dstIP, dstMac, bandwidth, startTime or endTime not specified" + "\"}"); 
		}
		String status = "";
		
		//Assumes that user passes in required bandwidth in terms of Gbps
		//Convert to bits per second for using queues
		long bitsPerGb = 1000000000;
		long bandwidthBPS = Long.parseLong(bandwidth) * bitsPerGb;
		
		//Convert start/end time to seconds since 00:00
		long sTimeSeconds = getTimeSeconds(startTime);
		long eTimeSeconds = getTimeSeconds(endTime);

		
		log.info("Source Host IP: {}", srcIP);
		log.info("Source Host MAC: {}", srcMac);
		log.info("Destination Host IP: {}", dstIP);
		log.info("Destination Host MAC: {}", dstMac);
		log.info("Bandwidth: {} bps", bandwidthBPS);
		log.info("Start Date-Time: {}", startTime);
		log.info("End Date-Time: {}", endTime);
		
		Topology topology = flowSchedService.getTopology();
		Node srcHostNode = topology.getNodeByName(srcMac);
		Node dstHostNode = topology.getNodeByName(dstMac);
		if(srcHostNode == null || srcHostNode.nodeIsSwitch() || dstHostNode == null || dstHostNode.nodeIsSwitch()){
			status = "Specified Source or Destination not a known host";
		}
		else if(sTimeSeconds == -1 || eTimeSeconds == -1){
			status = "Specified start or end time not in HH:mm format (e.g. HH can be 00-23, mm can be 00-59)";
		}
		else{
			Flow newFlow = constructFlow(srcHostNode, dstHostNode, bandwidthBPS, sTimeSeconds, eTimeSeconds, srcIP, dstIP);
			status = flowSchedService.handleNewFlow(newFlow);
		}
		return ("{\"status\" : \"" + status + "\"}"); 
	}
	
	/**
	 * Convert a string dateTime to seconds.
	 * @param dateTime - the time in HH:mm format
	 * @return dateTimeSeconds - the number of seconds since 00:00 as represented by HH:mm
	 */
	private long getTimeSeconds(String dateTime) {
		
		String[] parts = dateTime.split(":");
		if(parts.length != 2){
			return -1;
		}
		String hour = parts[0];
		String minute = parts[1];
		long hourLong = new Long(hour).longValue();
		long minuteLong =new Long(minute).longValue();
		if(hourLong < 0 || hourLong > 23 || minuteLong < 0 || minuteLong > 59){
			return -1;
		}
		//Hour and Minute are valid
		long dateTimeSeconds = 0;
		long secondsInMinute = 60;
		long minutesInHour = 60;
		dateTimeSeconds += minutesInHour * secondsInMinute * hourLong;
		dateTimeSeconds += secondsInMinute * minuteLong;
		return dateTimeSeconds;
	}

	/**
	 * Construct a new flow based on user parameters.
	 * @param srcHostNode
	 * @param dstHostNode
	 * @param bandwidth
	 * @param startTime
	 * @param endTime
	 * @param srcIP
	 * @param dstIP
	 * @return The Flow.
	 */
	public Flow constructFlow(Node srcHostNode, Node dstHostNode, long bandwidth, long startTime, long endTime, String srcIP, String dstIP){
		return new Flow(srcHostNode, dstHostNode, bandwidth, startTime, endTime, srcIP, dstIP);
	}
	
}
