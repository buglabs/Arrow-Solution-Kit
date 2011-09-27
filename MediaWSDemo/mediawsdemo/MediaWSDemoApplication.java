package mediawsdemo;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import mediawsdemo.JMPlayer;

import com.buglabs.application.IServiceProvider;
import com.buglabs.application.MainApplicationThread;
import com.buglabs.device.ButtonEvent;
import com.buglabs.device.IButtonEventListener;
import com.buglabs.device.IButtonEventProvider;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.PublicWSProviderWithParams;
import com.buglabs.services.ws.WSResponse;
import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
import com.buglabs.bug.module.video.pub.IVideoModuleControl;
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
public class MediaWSDemoApplication implements IButtonEventListener, ManagedRunnable, PublicWSProviderWithParams {

	public IButtonEventProvider bep ;
	public JMPlayer jmplayer = null;
	private DefaultListModel listModel;	
	private File[] foundMedia;
	private ServiceRegistration cameraControlWSReg;
	private BundleContext theContext ;
	private static final String mediaRoot = "/media/sda1" ;
//	private static final String mediaRoot = "/home/root/Media");
	
	public MediaWSDemoApplication(BundleContext context) {
		theContext = context ;
	}
	
	public void run(Map<Object, Object> services) {			
		IButtonEventProvider ibuttoneventprovider = (IButtonEventProvider) services.get(IButtonEventProvider.class.getName());
		ibuttoneventprovider.addListener(this);
		IVideoModuleControl ivideomodulecontrol = (IVideoModuleControl) services.get(IVideoModuleControl.class.getName());
		ivideomodulecontrol.setDVI() ;
		
		cameraControlWSReg = theContext.registerService(PublicWSProvider2.class.getName(), this, null);
		
		try {
			// initialize mplayer
			Runtime.getRuntime()
					.exec(new String[] { "/bin/sh", "-c",
							"export DISPLAY=:0.0 &" });
			this.jmplayer = new JMPlayer();
			
			findMedia();
			System.out.println("Found the following media files: ");			
			for (int i=0; i<foundMedia.length; i++) {
				System.out.println(foundMedia[i]);
			}
		} catch (final IOException e) {
			System.err.println("Failed to initiate mplayer: " + e);
		}
	}
	
	public void findMedia() {
		File root = new File(mediaRoot);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".avi")) {
					return true;
				} 
				else if (lowercaseName.endsWith(".mp4")) {
					return true;
				} else if (lowercaseName.endsWith(".m4v")) {
					return true;
				}
				else {
					return false;
				}
			}
		};

		foundMedia = root.listFiles(filter);	
	}

	public void shutdown() {
		if(jmplayer.isPlaying()) jmplayer.togglePlay();
	}


	public void buttonEvent(ButtonEvent event) {
		if ((event.getButton() == ButtonEvent.BUTTON_BUG20_USER) &&
				(event.getAction() == ButtonEvent.KEY_UP)) {
			System.out.println("Button pressed");
			if (jmplayer.isPlaying()) {
				jmplayer.togglePlay();
			}
		}		
	}
	
	private String serviceName = "MediaPlayer";
	public void setPublicName(String name) {
		serviceName = name;
	}

	public PublicWSDefinition discover(int operation) {
		if (operation == PublicWSProvider2.GET) {
			return new PublicWSDefinition() {

				public List getParameters() {
					return null;
				}

				public String getReturnType() {
					return "text/plain";
				}
			};
		}

		return null;
	}
	
	public IWSResponse execute(int operation, String input, Map get, Map post) {
		if (operation == PublicWSProvider2.GET) {
			Iterator entries = get.entrySet().iterator();
			while (entries.hasNext()) {
			  Entry thisEntry = (Entry) entries.next();
			  Object key = thisEntry.getKey();
			  Object value = thisEntry.getValue();
			  System.out.println("key: "+key.toString()+" value: "+value.toString());
			}
			String v = (String) get.get("vid");
			if (v != null) {
				final int i = Integer.parseInt(v);
				System.out.println("playing " + foundMedia[i].getName());
				try {
					jmplayer.open(foundMedia[i]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return new WSResponse("Success! Loading "+ foundMedia[Integer.parseInt(v)].getName() + ". Media will play immediately", "text/plain") ;
		}
		
		return null ;
		
	}


	public String getPublicName() {
		return serviceName;
	}


	public String getDescription() {
		
//		String defaultDesc = "To select a video, change the URL in your browser to " +
//		"http://bugIP/service/MediaPlayer?vid=# <br>" +
//		"and replace '#' with the numbers below:<br><br>" ;
//		
//		StringBuffer bfr = new StringBuffer(defaultDesc);
		
//		for (int i=0; i<foundMedia.length; i++) {
//			bfr.append((i) + ": " + foundMedia[i].getName() + "<br>");
//		}
		findMedia();
		
		String defaultDesc = "To play media on a display attached to BUGvideo, choose from the links below." +
				"Media files are placed in:<br>" + mediaRoot + 
				"<br><br>User Button = Play/Pause<br>" ;
			
		
		StringBuffer bfr = new StringBuffer(defaultDesc);
		
		for (int i=0; i<foundMedia.length; i++) {
			
			bfr.append("<a href=\"/service/MediaPlayer?vid=" + i + "\">" + (i) + ": " + foundMedia[i].getName() + "</a><br>");
		}
		
		bfr.append("<br>*Note: Playback will begin immediately.  Video that is too large to render properly" +
				" will result in choppy playback.<br>") ;

		
		return bfr.toString() ;
				
	}


	public IWSResponse execute(int operation, String input) {
		return null;
	}


}
