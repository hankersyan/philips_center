package io.hankers.mdi.philips_center;

import org.json.JSONObject;

public class Models {

	public static class HL7Message {
		String _type;
		int _controlId;
		long _timestamp;
		
		String _bedno;

		public boolean isEmpty() {
			return false;
		}
	}

	public static class HL7VitalSign extends HL7Message {
		String pulse;
		String hr;
		String resp;
		String temp;
		String spo2;
		
		String nbps;
		String nbpd;
		String nbpm;
		
		String abps;
		String abpd;
		String abpm;
		
		String cvps;
		String cvpd;
		String cvpm;
		
		String paps;
		String papd;
		String papm;

		String yellow_alarm;
		String red_alarm;

		public HL7VitalSign() {
		}
		
		public void copyFrom(HL7VitalSign vs) {
			if(vs == null) return;
			
			if (isValidValue(vs.pulse)) pulse = vs.pulse;
			if (isValidValue(vs.hr)) hr = vs.hr;
			if (isValidValue(vs.resp)) resp = vs.resp;
			if (isValidValue(vs.temp)) temp = vs.temp;
			if (isValidValue(vs.spo2)) spo2 = vs.spo2;
			
			if (isValidValue(vs.nbps)) nbps = vs.nbps;
			if (isValidValue(vs.nbpd)) nbpd = vs.nbpd;
			if (isValidValue(vs.nbpm)) nbpm = vs.nbpm;
			
			if (isValidValue(vs.abps)) abps = vs.abps;
			if (isValidValue(vs.abpd)) abpd = vs.abpd;
			if (isValidValue(vs.abpm)) abpm = vs.abpm;
			
			if (isValidValue(vs.cvps)) cvps = vs.cvps;
			if (isValidValue(vs.cvpd)) cvpd = vs.cvpd;
			if (isValidValue(vs.cvpm)) cvpm = vs.cvpm;
			
			if (isValidValue(vs.paps)) paps = vs.paps;
			if (isValidValue(vs.papd)) papd = vs.papd;
			if (isValidValue(vs.papm)) papm = vs.papm;
			
			if (isValidValue(vs.yellow_alarm)) yellow_alarm = vs.yellow_alarm;
			if (isValidValue(vs.red_alarm)) red_alarm = vs.red_alarm;
		}

		public String toString() {
			JSONObject json = new JSONObject();
			if (isValidValue(pulse)) {
				json.put("PULSE", pulse);
			}
			if (isValidValue(hr)) {
				json.put("HR", hr);
			}
			if (isValidValue(resp)) {
				json.put("RESP", resp);
			}
			if (isValidValue(spo2)) {
				json.put("SPO2", spo2);
			}
			if (isValidValue(temp)) {
				json.put("TEMP", temp);
			}
			
			if (isValidValue(nbps)) {
				json.put("NBPS", nbps);
			}
			if (isValidValue(nbpd)) {
				json.put("NBPD", nbpd);
			}
			if (isValidValue(nbpm)) {
				json.put("NBPM", nbpm);
			}
			
			if (isValidValue(abps)) {
				json.put("ABPS", abps);
			}
			if (isValidValue(abpd)) {
				json.put("ABPD", abpd);
			}
			if (isValidValue(abpm)) {
				json.put("ABPM", abpm);
			}
			
			if (isValidValue(cvps)) {
				json.put("CVPS", cvps);
			}
			if (isValidValue(cvpd)) {
				json.put("CVPD", cvpd);
			}
			if (isValidValue(cvpm)) {
				json.put("CVPM", cvpm);
			}
			
			if (isValidValue(paps)) {
				json.put("PAPS", paps);
			}
			if (isValidValue(papd)) {
				json.put("PAPD", papd);
			}
			if (isValidValue(papm)) {
				json.put("PAPM", papm);
			}

			if (isValidValue(yellow_alarm)) {
				json.put("YELLOW_ALARM", yellow_alarm);
			}
			if (isValidValue(red_alarm)) {
				json.put("RED_ALARM", red_alarm);
			}

			if (!json.isEmpty()) {
				if (_timestamp > 0) {
					json.put("timestamp", _timestamp);
				}
				if (isValidValue(_bedno)) {
					json.put("bed", _bedno);
				}
				return json.toString();
			}
			return null;
		}

		@Override
		public boolean isEmpty() {
			String content = this.toString();
			return content == null || content.isEmpty();
		}
		
		private boolean isValidValue(String src) {
			return src != null && !src.isEmpty();
		}
	}

}
