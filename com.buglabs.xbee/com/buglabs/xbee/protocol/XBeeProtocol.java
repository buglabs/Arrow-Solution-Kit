package com.buglabs.xbee.protocol;

import java.util.Map;

import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeResponse;

public interface XBeeProtocol {
	public Map<String, Object> parse(XBeeResponse res);
	public Map<String, Object> lastSample();
	public XBeeAddress getAddr();
	public String toString(Map<String, Object> data);
};
