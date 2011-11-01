package arrowtelematicssolution;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import arrowtelematicssolution.view.TelematicsFrame;

import com.buglabs.bug.module.gps.pub.IPositionProvider;
import com.buglabs.bug.module.gps.pub.INMEASentenceProvider;
import com.buglabs.bug.module.gps.pub.INMEARawFeed;
import com.buglabs.bug.module.lcd.pub.IML8953Accelerometer;

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
public class ArrowTelematicsSolutionApplication implements ManagedRunnable {

	private IPositionProvider ipositionprovider;
	private INMEASentenceProvider inmeasentenceprovider;
	private INMEARawFeed inmearawfeed;
	private IML8953Accelerometer iml8953accelerometer;

	@Override
	public void run(Map<Object, Object> services) {			
		ipositionprovider = (IPositionProvider) services.get(IPositionProvider.class.getName());			
		inmeasentenceprovider = (INMEASentenceProvider) services.get(INMEASentenceProvider.class.getName());			
		inmearawfeed = (INMEARawFeed) services.get(INMEARawFeed.class.getName());			
		iml8953accelerometer = (IML8953Accelerometer) services.get(IML8953Accelerometer.class.getName());
		System.out.println("DEBUG (" + this.getClass().getName() + "): Application Run");
		final TelematicsFrame frame = new TelematicsFrame("Arrow Telematics Demo", this);
		frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	frame.dispose();
            }
		});
		frame.schedule(0, 1000);
		
	}

	public IML8953Accelerometer getIml8953accelerometer() {
		return iml8953accelerometer;
	}

	public INMEARawFeed getInmearawfeed() {
		return inmearawfeed;
	}

	public INMEASentenceProvider getInmeasentenceprovider() {
		return inmeasentenceprovider;
	}

	public IPositionProvider getIpositionprovider() {
		return ipositionprovider;
	}

	@Override
	public void shutdown() {
		// TODO Add shutdown code here if necessary.
		System.out.println("DEBUG (" + this.getClass().getName() + "): Application Shutdown");
	}
	
	
}