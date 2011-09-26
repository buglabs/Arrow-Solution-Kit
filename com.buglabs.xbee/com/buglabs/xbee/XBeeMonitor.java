package com.buglabs.xbee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
import com.buglabs.xbee.protocol.XBeeProtocol;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.wpan.RxBaseResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.util.ByteUtils;

public class XBeeMonitor implements ManagedRunnable, Runnable, PacketListener, XBeeController {
	final int RECONNECT_DELAY = 5000;
	final int REQUEST_TIMEOUT = 5000;
	final int LOCAL_POLL_DELAY = 10000;
	final ApiId[] VALID_APIID = {ApiId.RX_16_IO_RESPONSE,ApiId.RX_16_RESPONSE,
			ApiId.RX_64_IO_RESPONSE,ApiId.RX_64_RESPONSE,ApiId.REMOTE_AT_RESPONSE};
	
	private Thread t;
	private LogService ls;
	private boolean running = true;
	private String devnode = "/dev/ttyUSB0";
	private int baud = 9600;
	private Map<XBeeAddress, XBeeProtocol> protocols;
	private BundleContext _context;
	private ServiceTracker tracker;
	
	public XBee xb;
	public boolean connected = false;
	
	@Override
	public void processResponse(XBeeResponse res) {
		//Shortcut - AT_Responses should only be synchronous
		//Also, ATMY is used as a ping, so we need t
		if (res.getApiId() == ApiId.AT_RESPONSE)
			return;
		//First, check this is a packet that contains a remote address
		boolean found = false;
		for (int i=0;i<VALID_APIID.length;i++)
			if (res.getApiId() == VALID_APIID[i])
				found = true;
		//We can't process packets with no known sender... Sorry!
		//TODO - can we support other packet types?
		if (!found){
			dlog("Unsupported pkt type recieved: "+res.getApiId().getValue());
			return;
		}
		RxBaseResponse pkt = (RxBaseResponse)res;
		if (protocols.containsKey(pkt.getSourceAddress())){
			Map<String,Object> ret;
			ret = protocols.get(pkt.getSourceAddress()).parse(res);
			ret.put("protocol", protocols.get(pkt.getSourceAddress()));
			ret.put("address", pkt.getSourceAddress().getAddress());
			ret.put("raw", pkt.getProcessedPacketBytes());
			//TODO - implement a whiteboard callback
			dlog("Got a packet belonging to "+protocols.get(pkt.getSourceAddress()).toString());
			whiteboardNotify(ret);
			//For now, just printing keyz
//			for (Map.Entry<String, Object> entry : ret.entrySet())
//			{
//			    ilog(entry.getKey()+": "+entry.getValue());
//			}
		//If we don't know where the packet came from, we can't parse it!
		} else {
			dlog("Unknown sender ("+ByteUtils.toBase16(pkt.getSourceAddress().getAddress())
					+") "+ByteUtils.toString(pkt.getProcessedPacketBytes()));
		}
	}
	
	private void whiteboardNotify(Map<String,Object> data){
		Object[] services = tracker.getServices();
		if (services == null)
			return;
		for (Object s:services){
			((XBeeCallback) s).dataRecieved(data);
		}
	}
	
	@Override
	public void addListener(XBeeProtocol proto) {
		dlog("adding protocol "+ByteUtils.toBase16(proto.getAddr().getAddress())+":"+proto.getClass().getName());
		protocols.put(proto.getAddr(), proto);
	}
	
	@Override
	public boolean removeListener(int[] address) {
		XBeeAddress addr;
		if (address.length > 2)
			addr = new XBeeAddress64(address);
		else
			addr = new XBeeAddress16(address);
		if (!protocols.containsKey(addr))
			return false;
		dlog("removing protocol at "+ByteUtils.toBase16(addr.getAddress()));
		protocols.remove(addr);
		return true;
	}
		
	public XBeeMonitor(BundleContext context){
		_context = context;
	}
	
	@Override
	public void run(Map<Object, Object> services) {
		// TODO Auto-generated method stub
		ls = (LogService) services.get(LogService.class.getName());
		protocols = new HashMap<XBeeAddress, XBeeProtocol>();
		t = new Thread(this, "XBee Monitor");
		t.start();
		_context.registerService(XBeeController.class.getName(), this, null);
		tracker = new ServiceTracker(_context, XBeeCallback.class.getName(),null);
		tracker.open();
	}

	@Override
	public void run() {
		dlog("Monitor thread start");
		xb = new XBee();
		running: while(running){
			try {
				//Specify system property to force RXTX to take the devnode
				System.setProperty("gnu.io.rxtx.SerialPorts", devnode);
				xb.open(devnode, baud);
			} catch (XBeeException e) {
				//If we cannot open the port, retry after RECONNECT_DELAY
				dlog("Cannot open port");
				try { t.sleep(RECONNECT_DELAY); }
				//If interrupted, break out and allow thread to close.
				catch (InterruptedException e1) { break running;}
				continue running;
			}
			connected = true;
			//allows processResponse to receive responses
			xb.addPacketListener(this);
			connected: while (connected){
				AtCommandResponse res = null;
				try {
					//Send a local ATMY command - being used as a local "ping"
					res = (AtCommandResponse) xb.sendSynchronous(new AtCommand("MY"), REQUEST_TIMEOUT);
					//If local XBee is gone, reconnect
				} catch (XBeeTimeoutException e) {
					dlog("Timeout with local XBee");
					break connected;
				} catch (XBeeException e) {
					dlog("Error with local XBee, reconnecting");
					break connected;
				}
				if (!res.isOk()){
					elog("Cannot determine local address, retrying");
					break connected;
				}
				try { t.sleep(LOCAL_POLL_DELAY); } 
				catch (InterruptedException e1) { break running;}
			}
			//Close port in preparation for reconnection
			connected = false;
			xb.close();
			
		}
		//Close port before closing thread
		dlog("Monitor thread stop");
		xb.close();
	}
	
	@Override
	public void shutdown() {
		running = false;
		t.interrupt();
		tracker.close();
	}
		
	//Wrappers for the log service (to standardize logged messages)
	void ilog(String message){  ls.log(ls.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}
	void dlog(String message){  ls.log(ls.LOG_DEBUG, "["+this.getClass().getSimpleName()+"] "+message);	}
	void elog(String message){  ls.log(ls.LOG_ERROR, "["+this.getClass().getSimpleName()+"] "+message);	}
	void wlog(String message){  ls.log(ls.LOG_WARNING, "["+this.getClass().getSimpleName()+"] "+message);	}

	@Override
	public XBee getXBee() {
		return xb;
	}

}