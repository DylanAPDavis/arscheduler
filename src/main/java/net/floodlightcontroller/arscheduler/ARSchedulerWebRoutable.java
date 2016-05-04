package net.floodlightcontroller.arscheduler;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

/**
 * REST Interface for the ARScheduler. Provides endpoints for a REST application to schedule new flows.
 * @author dylan
 *
 */
public class ARSchedulerWebRoutable implements RestletRoutable {

	/**
	 * Construct the endpoints.
	 */
	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("/state/json", ARSchedulerResource.class);
		router.attach("/schedule/json", ARSchedulerResource.class);
		//router.attach("/topo/json", FlowSchedulerResource.class);
		return router;
	}

	/**
	 * Return the base path for the endpoints.
	 */
	@Override
	public String basePath() {
		return "/wm/arscheduler";
	}

}
