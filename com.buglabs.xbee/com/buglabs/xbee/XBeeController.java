package com.buglabs.xbee;

import java.util.Map;

import com.buglabs.xbee.protocol.XBeeProtocol;
import com.rapplogic.xbee.api.XBee;

public interface XBeeController {
	public void addListener(XBeeProtocol proto);
	public boolean removeListener(int[] addr);
	public XBee getXBee();
	public Map<String,Object> getResponse();
	public Map<String,Object> getResponse(int[] addr);
}
