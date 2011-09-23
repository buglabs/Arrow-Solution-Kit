/**
 * Generated by Dragonfly SDK.
 */
package xbeeapitest;


import org.osgi.service.log.LogService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.application.ServiceTrackerHelper;


/**
 * BundleActivator for XBeeAPITest.  The OSGi entry point to the application.
 *
 */
public class Activator implements BundleActivator {
    /**
	 * OSGi services the application depends on.
	 */
	private static final String [] services = {		
		LogService.class.getName(),
	};	
	private ServiceTracker serviceTracker;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		//Begin tracking services, and when all services are available, create thread and call ManagedRunnable.run().
		serviceTracker = ServiceTrackerHelper.openServiceTracker(context, services, new XBeeAPITestApplication());

	}

    /*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	
		//Will cause the ManagedRunnable.shutdown() to be called.
		serviceTracker.close();
	}
	
	
}