package com.buglabs.xbee.protocol;

import java.util.HashMap;
import java.util.Map;

import com.buglabs.xbee.XBeeController;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;

public class PIRMotion implements XBeeProtocol {
	private Map<String, Object> last = null;
	private XBeeAddress addr;
	
	public PIRMotion(int[] address){
		if (address.length > 2)
			addr = new XBeeAddress64(address);
		else
			addr = new XBeeAddress16(address);
	}
	
	public PIRMotion(int[] address, XBeeController con){
		this(address);
		con.addListener(this);
	}
	
	@Override
	public XBeeAddress getAddr() {
		return addr;
	}

	@Override
	public Map<String, Object> lastSample() {
		return last;
	}

	@Override
	public Map<String, Object> parse(XBeeResponse res) {
		Map<String, Object> ret = null;
		if (res.getApiId() == ApiId.RX_16_IO_RESPONSE){
			ret = new HashMap<String,Object>();
			ret.put("Motion", true);
			last = ret;
		}
		return ret;
	}

	@Override
	public String toString(Map<String, Object> data) {
		return "Motion detected from "+Integer.toHexString(((int[])data.get("address"))[1]);
	}

}
