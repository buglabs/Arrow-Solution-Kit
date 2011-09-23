package remotemondemoapi;

import java.util.Map;

import com.buglabs.xbee.XBeeController;
import com.buglabs.xbee.protocol.SparkfunWeatherboard;
import com.buglabs.xbee.protocol.XBeeProtocol;

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
public class RemoteMonDemoAPIApplication implements ManagedRunnable {
	final int[] address = {0x00, 0x03};
	
	private XBeeController xbc;
	private LogService ls;

	@Override
	public void run(Map<Object, Object> services) {			
		xbc = (XBeeController) services.get(XBeeController.class.getName());			
		ls = (LogService) services.get(LogService.class.getName());
		ilog("start");
		xbc.addListener(new SparkfunWeatherboard(address));
	}

	@Override
	public void shutdown() {
		xbc.removeListener(address);
		ilog("Stop");
	}
	
	void ilog(String message){  ls.log(ls.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}
}