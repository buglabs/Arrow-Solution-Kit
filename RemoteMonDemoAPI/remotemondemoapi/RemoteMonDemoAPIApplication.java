package remotemondemoapi;

import java.util.Arrays;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
import com.buglabs.xbee.XBeeCallback;
import com.buglabs.xbee.XBeeController;
import com.buglabs.xbee.protocol.MaxbotixRangefinder;
import com.buglabs.xbee.protocol.PIRMotion;
import com.buglabs.xbee.protocol.SparkfunWeatherboard;
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
public class RemoteMonDemoAPIApplication implements ManagedRunnable, XBeeCallback {
	private XBeeController xbc;
	private LogService ls;
	
	@Override
	public void dataRecieved(Map<String, Object> data) {
		if ((Class<?>)data.get("class") == SparkfunWeatherboard.class){
			ilog("Weather data from "+Integer.toHexString(((int[])data.get("address"))[1]));
			ilog("Temperature: "+data.get("Temperature"));
			ilog("Humidity: "+data.get("Humidity"));
			ilog("Dewpoint: "+data.get("Dewpoint"));
			ilog("Pressure: "+data.get("Pressure"));
			ilog("Light: "+data.get("Light"));
		} else if ((Class<?>)data.get("class") == MaxbotixRangefinder.class){
			String range = (String) data.get("Range");
			ilog("Range from "+Integer.toHexString(((int[])data.get("address"))[1])
					+": "+range);
		} else if ((Class<?>)data.get("class") == PIRMotion.class){
			ilog("Motion detected from "+Integer.toHexString(((int[])data.get("address"))[1]));
		} else {
			ilog("Unknown data: "+Arrays.toString((int[])data.get("raw")));
		}
	}
	
	@Override
	public void run(Map<Object, Object> services) {			
		xbc = (XBeeController) services.get(XBeeController.class.getName());			
		ls = (LogService) services.get(LogService.class.getName());
		ilog("start");
		xbc.addPredictive(SparkfunWeatherboard.class);
		xbc.addPredictive(MaxbotixRangefinder.class);
		xbc.addPredictive(PIRMotion.class);
	}

	@Override
	public void shutdown() {
		xbc.removeAll(SparkfunWeatherboard.class);
		xbc.removeAll(MaxbotixRangefinder.class);
		xbc.removeAll(PIRMotion.class);
		ilog("Stop");
	}
	
	void ilog(String message){  ls.log(LogService.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}

}