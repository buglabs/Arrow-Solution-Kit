/**
 * Generated by Dragonfly SDK.
 */
package remotemondemoapi;


import com.buglabs.xbee.XBeeCallback;
import com.buglabs.xbee.XBeeController;
import org.osgi.service.log.LogService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.application.ServiceTrackerHelper;


/**
 * BundleActivator for RemoteMonDemoAPI.  The OSGi entry point to the application.
 *
 */
public class Activator implements BundleActivator {
    /**
	 * OSGi services the application depends on.
	 */
	private static final String [] services = {		
		XBeeController.class.getName(),		
		LogService.class.getName(),
	};	
	private ServiceTracker serviceTracker;
	private ServiceRegistration callbackService;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		//Begin tracking services, and when all services are available, create thread and call ManagedRunnable.run().
		RemoteMonDemoAPIApplication mon = new RemoteMonDemoAPIApplication();
		serviceTracker = ServiceTrackerHelper.openServiceTracker(context, services, mon);
		callbackService = context.registerService(XBeeCallback.class.getName(), mon, null);
	}

    /*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	
		//Will cause the ManagedRunnable.shutdown() to be called.
		callbackService.unregister();
		serviceTracker.close();
	}
	
	
}