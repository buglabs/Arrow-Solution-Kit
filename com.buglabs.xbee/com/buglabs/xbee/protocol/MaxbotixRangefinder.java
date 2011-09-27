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

public class MaxbotixRangefinder extends BaseXBeeProtocol {

	public MaxbotixRangefinder(int[] address, XBeeController con) {
		super(address, con);
		// TODO Auto-generated constructor stub
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
			ret.put("Range",data);
			last = ret;
		} 
		return ret;
	}

	@Override
	public String toString(Map<String, Object> data) {
		String ret = "range("+Integer.toHexString(((int[])data.get("address"))[1])
					+"): "+data.get("Range");
		return ret;
	}
	
	@Override
	public boolean parseable(XBeeResponse res){
		if (res.getApiId() != ApiId.RX_16_RESPONSE)
			return false;
		RxResponse pkt = (RxResponse) res;
		String data = ByteUtils.toString(pkt.getData());
		data = data.replaceAll("\n", "");
		data = data.replaceAll("\r", "");
		if ((data.charAt(0) == 'R')&&(data.length() == 4))
			return true;
		return false;
	}

}
