package remotemondemoapi;

import java.util.Arrays;
import java.util.Map;

import com.buglabs.xbee.XBeeCallback;
import com.buglabs.xbee.XBeeController;
import com.buglabs.xbee.protocol.MaxbotixRangefinder;
import com.buglabs.xbee.protocol.PIRMotion;
import com.buglabs.xbee.protocol.SerialDevice;
import com.buglabs.xbee.protocol.SparkfunWeatherboard;
import com.buglabs.xbee.protocol.XBeeProtocol;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
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
	final int[] WEATHER_ADDRESS = {0x00, 0x03};
	final int[] MOTION_ADDRESS = {0x00, 0x02};
	final int[] RANGE_ADDRESS = {0x00, 0x01};
	
	private XBeeController xbc;
	private LogService ls;
	private SparkfunWeatherboard weather;
	private MaxbotixRangefinder range;
	private PIRMotion motion;
	
	@Override
	public void dataRecieved(Map<String, Object> data) {
		/*if ((XBeeProtocol)data.get("protocol") == motion){
			ilog("Motion detected!");
			ilog((String)xbc.getResponse(RANGE_ADDRESS).get("Range"));
			ilog((String)xbc.getResponse(WEATHER_ADDRESS).get("Temperature"));
		}*/
		if ((XBeeProtocol)data.get("protocol") == weather){
			ilog("Weather data from "+Integer.toHexString(((int[])data.get("address"))[1]));
			ilog("Temperature: "+data.get("Temperature"));
			ilog("Humidity: "+data.get("Humidity"));
			ilog("Dewpoint: "+data.get("Dewpoint"));
			ilog("Pressure: "+data.get("Pressure"));
			ilog("Light: "+data.get("Light"));
		} else if ((XBeeProtocol)data.get("protocol") == range){
			String range = (String) data.get("Range");
			ilog("Range from "+Integer.toHexString(((int[])data.get("address"))[1])
					+": "+range);
		} else if ((XBeeProtocol)data.get("protocol") == motion){
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
		weather = new SparkfunWeatherboard(WEATHER_ADDRESS, xbc);
		range = new MaxbotixRangefinder(RANGE_ADDRESS, xbc);
		motion = new PIRMotion(MOTION_ADDRESS , xbc);
	}

	@Override
	public void shutdown() {
		xbc.removeListener(WEATHER_ADDRESS);
		xbc.removeListener(RANGE_ADDRESS);
		xbc.removeListener(MOTION_ADDRESS);
		ilog("Stop");
	}
	
	void ilog(String message){  ls.log(ls.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}

}