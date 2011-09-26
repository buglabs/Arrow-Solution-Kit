package com.buglabs.xbee.protocol;

import java.util.HashMap;
import java.util.Map;

import com.buglabs.xbee.XBeeController;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxRequest64;
import com.rapplogic.xbee.api.wpan.TxRequestBase;
import com.rapplogic.xbee.util.ByteUtils;

public class SerialDevice extends BaseXBeeProtocol {

	public SerialDevice(int[] address, XBeeController con) {
		super(address, con);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Map<String, Object> parse(XBeeResponse res) {
		Map<String, Object> ret = null;
		if (!((res.getApiId() == ApiId.RX_16_RESPONSE)||(res.getApiId() == ApiId.RX_64_RESPONSE)))
			return ret;
		ret = new HashMap<String,Object>();
		RxResponse pkt = (RxResponse) res;
		String data = ByteUtils.toString(pkt.getData());
		ret.put("data", data);
		last = ret;
		return ret;
	}

	@Override
	public String toString(Map<String, Object> data) {
		String ret = "range("+Integer.toHexString(((int[])data.get("address"))[1])
					+"): "+data.get("range");
		return ret;
	}
	
	public void write(String data){
		TxRequestBase request;
		if (addr.getAddress().length > 2)
			request = new TxRequest64((XBeeAddress64)addr, 0, TxRequestBase.Option.DISABLE_ACK, ByteUtils.stringToIntArray(data));
		else
			request = new TxRequest16((XBeeAddress16)addr, 0, TxRequestBase.Option.DISABLE_ACK, ByteUtils.stringToIntArray(data));
		try {
			con.getXBee().sendAsynchronous(request);
		} catch (XBeeException e) {}
	}

}
