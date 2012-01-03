package remotemondemoapi;

import gui.SensordataView;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext; 
import org.osgi.service.log.LogService;

import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
import com.buglabs.bug.swarm.client.ISwarmJsonMessageListener;
import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.xbee.XBeeCallback;
import com.buglabs.xbee.XBeeController;
import com.buglabs.xbee.protocol.MaxbotixRangefinder;
import com.buglabs.xbee.protocol.PIRMotion;
import com.buglabs.xbee.protocol.SparkfunWeatherboard;
import com.buglabs.xbee.protocol.XBeeProtocol;
/**
 * This class represents the running application when all service dependencies are fulfilled.
 * 
 * The run() method will be called with a map containing all the services specified in ServiceTrackerHelper.openServiceTracker().
 * The application will run in a separate thread than the caller of start() in the Activator.  See 
 * ManagedInlineRunnable for a thread-less application.
 * 
 * By default, the application will only be started when all service dependencies are fulfilled.  For 
 * finer grained service binding logic, see ServiceTrackerHelper.openServiceTracker(BundleContext context, String[] services, Filter filter, ServiceTrackerCustomizer customizer)
 */
public class RemoteMonDemoAPIApplication implements ManagedRunnable, XBeeCallback, ISwarmJsonMessageListener {
	private final static String SERVER_URL = "api.bugswarm.net";
	private final static String API_KEY = "dd370eabf1fde6beeab83ec9c288e0abb4639654";
	private final static String RESOURCE_ID = "7e424080b35fefd505c440a8689ab6da909ada64";
	private final static String SWARM_ID = "82bf3639e5d52500bd3384efe6e9892b42ff6c0c";
	
	private final static int RANGE_CACHE_SIZE = 5;
	 
	private XBeeController xbc;
	private LogService ls;
	private SensordataView SensorView;
	private BundleContext context;
	private ISwarmSession swarm;
	boolean swarming = false;
	boolean skip = false;
	int rangeSamples = 0;
	
	@Override
	public void dataRecieved(Map<String, Object> data) {
		XBeeProtocol proto = (XBeeProtocol)data.get("protocol");
		if (proto.getClass() == MaxbotixRangefinder.class){
			if (rangeSamples < RANGE_CACHE_SIZE){
				rangeSamples++;
				return;
			} else {
				rangeSamples = 0;
			}
		}
		if (swarming && swarm.isConnected()){
			Map<String, Object> out = new HashMap<String,Object>(data);
			out.put("protocol",proto.getClass().getName()); 
			String addr = Integer.toHexString(((int[])out.get("address"))[1]);
			out.put("address",addr);
			out.remove("class");
			
			ObjectMapper mapper = new ObjectMapper();
			if (!skip){
				SensorView.update(data);
			}
			if (swarming && !skip){
				try {
					ilog("data sent to swarm");
					swarm.send(out);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			skip = false;
		} 
			
//		if ((Class<?>)data.get("class") == SparkfunWeatherboard.class){
//			ilog("Weather data from "+Integer.toHexString(((int[])data.get("address"))[1]));
//			ilog("Temperature: "+data.get("Temperature"));
//			ilog("Humidity: "+data.get("Humidity"));
//			ilog("Dewpoint: "+data.get("Dewpoint"));
//			ilog("Pressure: "+data.get("Pressure"));
//			ilog("Light: "+data.get("Light"));
//			
//		} else if ((Class<?>)data.get("class") == MaxbotixRangefinder.class){
//			String range = (String) data.get("Range");
//			ilog("Range from "+Integer.toHexString(((int[])data.get("address"))[1])
//					+": "+range);
//		} else if ((Class<?>)data.get("class") == PIRMotion.class){
//			ilog("Motion detected from "+Integer.toHexString(((int[])data.get("address"))[1]));
//		} else {
//			ilog("Unknown data: "+Arrays.toString((int[])data.get("raw")));
//		}
	}
	  
	@Override
	public void run(Map<Object, Object> services) {			
		xbc = (XBeeController) services.get(XBeeController.class.getName());			
		ls = (LogService) services.get(LogService.class.getName());
		ilog("start");
		swarming = true;
		try {
			swarm = SwarmClientFactory.createSwarmSession(SERVER_URL, API_KEY, RESOURCE_ID, SWARM_ID);
			swarm.addListener(this);
		} catch (UnknownHostException e) {
			elog("UnknownHostException, make sure bug can ping "+SERVER_URL+".  Continuing without swarm.");
			swarming = false;
		} catch (IOException e) {
			elog("IOException trying to connect.  Trying a few times...");
			int errorcount = 0;
			while (errorcount < 10){ 
				try {
					ilog("Attempt "+errorcount+" to reconnect...");
					swarm = SwarmClientFactory.createSwarmSession(SERVER_URL, API_KEY, RESOURCE_ID, SWARM_ID);
					swarm.addListener(this);
				} catch (UnknownHostException e1) { 
				} catch (IOException e1) {
					swarming = false;
					errorcount++;
					try {
						Thread.sleep(2000);
						continue;
					} catch (InterruptedException e2) { break; }
				}
				swarming = true;
				break;
			}
		}
		if (swarming)
			ilog("Connected to swarm!");
		xbc.addPredictive(SparkfunWeatherboard.class);
		xbc.addPredictive(MaxbotixRangefinder.class);
		xbc.addPredictive(PIRMotion.class);
		SensorView = new SensordataView("Remote Sensor Monitor",this);
	}

	@Override
	public void shutdown() {
		xbc.removeAll(SparkfunWeatherboard.class);
		xbc.removeAll(MaxbotixRangefinder.class);
		xbc.removeAll(PIRMotion.class);
		if (swarming){
			swarm.removeListener(this);
			swarm.close();
		}
		ilog("Stop");
	}
	
	void ilog(String message){  ls.log(LogService.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}
	void elog(String message){  ls.log(LogService.LOG_ERROR, "["+this.getClass().getSimpleName()+"] "+message);	}
	void wlog(String message){  ls.log(LogService.LOG_WARNING, "["+this.getClass().getSimpleName()+"] "+message);	}
	void dlog(String message){  ls.log(LogService.LOG_DEBUG, "["+this.getClass().getSimpleName()+"] "+message);	}
	
	public void setBundlecontext(BundleContext context) {
		this.context = context;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	@Override
	public void presenceEvent(String fromSwarm, String fromResource,
			boolean isAvailable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exceptionOccurred(ExceptionType type, String message) {
		elog(type+": "+message);
		
	}

	@Override
	public void messageRecieved(Map<String, ?> payload, String fromSwarm,
			String fromResource, boolean isPublic) {
		// TODO Auto-generated method stub
		
	}

}