package com.buglabs.xbee.protocol;

import java.util.HashMap;
import java.util.Map;

import com.buglabs.xbee.XBeeController;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.util.ByteUtils;

public class MaxbotixRangefinder implements XBeeProtocol {
	private Map<String, Object> last = null;
	private XBeeAddress addr;

	public MaxbotixRangefinder(int[] address){
		if (address.length > 2)
			addr = new XBeeAddress64(address);
		else
			addr = new XBeeAddress16(address);
	}
	
	public MaxbotixRangefinder(int[] address, XBeeController con){
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
		if (res.getApiId() != ApiId.RX_16_RESPONSE)
			return ret;
		RxResponse pkt = (RxResponse) res;
		String data = ByteUtils.toString(pkt.getData());
		data = data.replaceAll("\n", "");
		data = data.replaceAll("\r", "");
		if (data.charAt(0) == 'R'){
			ret = new HashMap<String, Object>();
			data = data.substring(1,4);
			ret.put("range",data);
			last = ret;
		} 
		return ret;
	}

	@Override
	public String toString(Map<String, Object> data) {
		String ret = "range("+Integer.toHexString(((int[])data.get("address"))[1])
					+"): "+data.get("range");
		return ret;
	}

}
