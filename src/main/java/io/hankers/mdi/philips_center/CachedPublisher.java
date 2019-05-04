package io.hankers.mdi.philips_center;

import io.hankers.mdi.mdi_utils.MDIConfig;
import io.hankers.mdi.mdi_utils.MqttPublisher;
import io.hankers.mdi.philips_center.Models.HL7VitalSign;

public class CachedPublisher {

	static HL7VitalSign _cachedMsg = null;
	static long _interval = 60;
	
	static {
		long _interval = MDIConfig.getPublishInterval() * 1000;
		if (_interval < 1) {
			_interval = 60 * 1000;
		}
	}

	public static void put(HL7VitalSign newMsg) {
		if (newMsg == null || newMsg.isEmpty()) {
			// do nothing
		} else if (_cachedMsg == null) {
			_cachedMsg = newMsg;
		} else if (isSameInterval(_cachedMsg._timestamp, newMsg._timestamp)) {
			_cachedMsg.copyFrom(newMsg);
		} else {
			// MDILog.d("publishing {}, {}", _cachedMsg, newMsg);
			String content = _cachedMsg.toString();
			if (content != null && !content.isEmpty()) {
				MqttPublisher.addMessage(content);
			}
			_cachedMsg = newMsg;
		}
	}
	
	private static boolean isSameInterval(long ts1, long ts2) {
		return ((ts1 / _interval) * _interval) == ((ts2 / _interval) * _interval);
	}
	
}
