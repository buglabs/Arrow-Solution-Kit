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

public class PIRMotion extends BaseXBeeProtocol {
	public PIRMotion(int[] address, XBeeController con) {
		super(address, con);
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
	
	@Override
	public boolean parseable(XBeeResponse res){
		if (res.getApiId() == ApiId.RX_16_IO_RESPONSE)
			return true;
		return false;
	}

}
