package com.buglabs.xbee;

import java.util.Map;

public interface XBeeCallback {
	public void dataRecieved(Map<String, Object> data);
}
