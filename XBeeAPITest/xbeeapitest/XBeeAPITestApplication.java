package xbeeapitest;

import java.util.Map;
import java.util.Properties;

import org.osgi.service.log.LogService;

import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxBaseResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.util.ByteUtils;
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
public class XBeeAPITestApplication implements ManagedRunnable {
	private LogService ls;
	private XBee xb;

	@Override
	public void run(Map<Object, Object> services) {			
		ls = (LogService) services.get(LogService.class.getName());
		ilog("Start, connecting");
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyUSB0");
		xb = new XBee();
		try {
			xb.open("/dev/ttyUSB0",9600);
			dlog("Opened, requesting data");
			AtCommandResponse res = (AtCommandResponse) xb.sendSynchronous(new AtCommand("MY"), 10000);
			if (res.isOk()){
				ilog("Addr of attached XBee: "+ByteUtils.toBase16(res.getValue()));
			}
		} catch (XBeeException e) {
			e.printStackTrace();
			shutdown();
			return;
		}
		xb.addPacketListener(new PacketParser());
	} 

	@Override
	public void shutdown() {
		ilog("Stop.");
		xb.close();
	}
	
	private class PacketParser implements PacketListener{

		@Override
		public void processResponse(XBeeResponse res) {
			//If the incoming data is a serial data packet
			if (res.getApiId() == ApiId.RX_16_RESPONSE){
				RxResponse pkt = (RxResponse) res;
				String data = ByteUtils.toString(pkt.getData());
				data = data.replaceAll("\n", "");
				data = data.replaceAll("\r", "");
				//dlog("Incoming data "+pkt.getSourceAddress().getAddress()[1]+": "+data);
				if (pkt.getSourceAddress().getAddress()[1] == 1){
					//Data from the Sonar mote
					if (data.charAt(0) == 'R'){
						//Remove out-of-range-samples
						if (!data.contains("765"))
							ilog("Range: "+data.substring(1)+" (cm)");
					} else {
						dlog("Sonar data: "+data);
					}
				} else if (pkt.getSourceAddress().getAddress()[1] == 2){
					//Serial data from the Motion mote (must be in serial mode)
					if (data.contains("M")){
						ilog("Motion detected!");
					} else {
						dlog("Motion data: "+data);
					}
				} else if (pkt.getSourceAddress().getAddress()[1] == 3){
					//Data from the weatherboard
					if (data.charAt(0) == '$'){
						data = data.substring(2,data.length()-2);
						String[] values = data.split(",");
						ilog("Temperature: "+values[0]+" (F)");
						ilog("Humidity: "+values[1]+" (%)");
						ilog("Dewpoint: "+values[2]+" (F)");
						ilog("Pressure: "+values[3]+" (in/hg)");
						ilog("Light: "+values[4]+" (%)");
						ilog("Wind Speed: "+values[5]+" (mph)");
						ilog("Wind Direction: "+values[6]+" (deg)");
						ilog("Rainfall: "+values[7]+" (in)");
						ilog("Battery: "+values[8]+" (V)");
					} else {
						dlog("Weatherboard info: "+data);
					}
				} else {
					dlog("Data from "+pkt.getSourceAddress().getAddress()[1]+": "+data);
				}
			} else if (res.getApiId() == ApiId.RX_16_IO_RESPONSE){
				//If incoming data is an IO packet
				//Assume this was from a Motion module
				RxResponseIoSample pkt = (RxResponseIoSample) res;
				//ilog("Motion detected!: "+ByteUtils.toBase16(pkt.getSamples()[0].getDioMsb())+","+ByteUtils.toBase16(pkt.getSamples()[0].getDioLsb()));
				ilog("Motion detected!");
			}
		}
		
	}
		
	//Wrappers for the log service (to standardize logged messages)
	void ilog(String message){  ls.log(ls.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}
	void dlog(String message){  ls.log(ls.LOG_DEBUG, "["+this.getClass().getSimpleName()+"] "+message);	}
	void elog(String message){  ls.log(ls.LOG_ERROR, "["+this.getClass().getSimpleName()+"] "+message);	}
	void wlog(String message){  ls.log(ls.LOG_WARNING, "["+this.getClass().getSimpleName()+"] "+message);	}
}