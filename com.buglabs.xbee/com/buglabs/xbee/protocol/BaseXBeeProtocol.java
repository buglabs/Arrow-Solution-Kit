package com.buglabs.xbee.protocol;

import java.util.Map;

import com.buglabs.xbee.XBeeController;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;

public class BaseXBeeProtocol implements XBeeProtocol {
	protected Map<String, Object> last = null;
	protected XBeeAddress addr;
	protected XBeeController con;
	
	public BaseXBeeProtocol(){
	}

	public BaseXBeeProtocol(int[] address, XBeeController controller){
		if (address.length > 2)
			addr = new XBeeAddress64(address);
		else
			addr = new XBeeAddress16(address);
		con = controller;
		con.addListener(this);
	}
	@Override
	public final XBeeAddress getAddr() {
		return addr;
	}

	@Override
	public final Map<String, Object> lastSample() {
		return last;
	}

	@Override
	public Map<String, Object> parse(XBeeResponse res) {
		return null;
	}

	@Override
	public String toString(Map<String, Object> data) {
		return this.getClass().toString();
	}
	
	@Override
	public String toString(){
		return toString(last);
	}
	@Override
	public boolean parseable(XBeeResponse res) {
		return false;
	}

	@Override
	public void setAddr(XBeeAddress address) {
		addr = address;
	}

}
