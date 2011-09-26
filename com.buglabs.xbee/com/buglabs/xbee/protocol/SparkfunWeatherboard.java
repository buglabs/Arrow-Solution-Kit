package com.buglabs.xbee.protocol;

import java.util.HashMap;
import java.util.Map;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.util.ByteUtils;

public class SparkfunWeatherboard implements XBeeProtocol {
	private Map<String, Object> last = null;
	private XBeeAddress addr;

	public SparkfunWeatherboard(int[] address){
		if (address.length > 2)
			addr = new XBeeAddress64(address);
		else
			addr = new XBeeAddress16(address);
	}
	
	@Override
	public Map<String, Object> lastSample() {
		return last;
	}

	@Override
	public Map<String, Object> parse(XBeeResponse res) {
		Map<String, Object> ret = new HashMap<String, Object>();
		if (res.getApiId() != ApiId.RX_16_RESPONSE)
			return ret;
		RxResponse pkt = (RxResponse) res;
		String data = ByteUtils.toString(pkt.getData());
		data = data.replaceAll("\n", "");
		data = data.replaceAll("\r", "");
		if (data.charAt(0) == '$'){
			data = data.substring(2,data.length()-2);
			String[] values = data.split(",");
			ret.put("Temperature", values[0]);
			ret.put("Humidity",values[1]);
			ret.put("Dewpoint",values[2]);
			ret.put("Pressure",values[3]);
			ret.put("Light",values[4]);
			ret.put("Wind Speed",values[5]);
			ret.put("Wind Direction",values[6]);
			ret.put("Rainfall",values[7]);
			ret.put("Battery",values[8]);
			last = ret;
		} 
		return ret;
	}

	@Override
	public XBeeAddress getAddr() {
		return addr;
	}

	@Override
	public String toString(Map<String, Object> data) {
		String ret = "";
		ret += "Weather data from "+Integer.toHexString(((int[])data.get("address"))[1])+"\r\n";
		ret += "Temperature: "+data.get("Temperature")+"\r\n";
		ret += "Humidity: "+data.get("Humidity")+"\r\n";
		ret += "Dewpoint: "+data.get("Dewpoint")+"\r\n";
		ret += "Pressure: "+data.get("Pressure")+"\r\n";
		ret += "Light: "+data.get("Light")+"\r\n";
		return ret;
	}

}
