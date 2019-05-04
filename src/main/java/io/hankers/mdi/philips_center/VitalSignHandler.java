package io.hankers.mdi.philips_center;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.v22.datatype.PN;
import ca.uhn.hl7v2.model.v22.datatype.TSComponentOne;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v22.message.ORU_R01;
import ca.uhn.hl7v2.model.v22.segment.DSC;
import ca.uhn.hl7v2.model.v22.segment.OBX;
import ca.uhn.hl7v2.model.v22.segment.PID;
import ca.uhn.hl7v2.model.v22.segment.PV1;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import io.hankers.mdi.mdi_utils.MDILog;
import io.hankers.mdi.philips_center.Models.HL7VitalSign;

public class VitalSignHandler implements ReceivingApplication<Message> {

	Map<String, Field> _hl7fieldToObjFieldMap = new HashMap<String, Field>();

	public VitalSignHandler() {

		List<Field> classFields = Arrays.asList(HL7VitalSign.class.getDeclaredFields());

		String jsonStr = null;
		try {
			String pathStr = VitalSignHandler.class.getClassLoader().getResource("hl7map.json").toURI().getPath();
			jsonStr = new String(Files.readAllBytes(Paths.get(pathStr)));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			MDILog.e(e);
		} catch (IOException e) {
			e.printStackTrace();
			MDILog.e(e);
		}
		JsonParser jsonParser = new JsonParser();
		JsonObject json = (JsonObject) jsonParser.parse(jsonStr);
		for (String key : json.keySet()) {
			Optional<Field> found = classFields.stream().filter(x -> x.getName().equalsIgnoreCase(key)).findAny();
			if (found.isPresent()) {
				Field field = found.get();
				field.setAccessible(true);
				_hl7fieldToObjFieldMap.put(key.toUpperCase(), field);
			}
		}
		MDILog.d(_hl7fieldToObjFieldMap.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canProcess(Message theIn) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
			throws ReceivingApplicationException, HL7Exception {

		HapiContext context = new DefaultHapiContext();

		String encodedMessage = context.getPipeParser().encode(theMessage);
		MDILog.d("Received message:\n" + encodedMessage + "\n\n");

		parseHL7(context, encodedMessage);

		// Now generate a simple acknowledgment message and return it
		try {
			return theMessage.generateACK();
		} catch (IOException e) {
			throw new HL7Exception(e);
		} finally {
			try {
				context.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void parseHL7(HapiContext context, String msg) {
		// Sample from Philips Center Doc
//		String msg = "MSH|^~\\&|||||||ORU^R01|HP104220879017992|P|2.2||||||8859/1\r"
//				+ "PID|||MRN5733||Smith^John||19550508|M\r" + "PV1||I|^^Doc1&5&1\r" + "OBR|||||||20030110152630\r"
//				+ "OBX||NM|0002-4bb8^SpO2^MDIL|0|95|0004-0220^%^MDIL|||||F\r"
//				+ "OBX||NM|0002-5000^Resp^MDIL|0|15|0004-0ae0^rpm^MDIL|||||F\r"
//				+ "OBX||NM|0002-4182^HR^MDIL|0|60|0004-0aa0^bpm^MDIL|||||F\r"
//				+ "OBX||NM|0002-4a15^ABPs^MDIL|0|120|0004-0f20^mmHg^MDIL|||||F\r"
//				+ "OBX||NM|0002-4a16^ABPd^MDIL|0|70|0004-0f20^mmHg^MDIL|||||F\r"
//				+ "OBX||NM|0002-4a17^ABPm^MDIL|0|91|0004-0f20^mmHg^MDIL|||||F\r";

		Parser p = context.getGenericParser();

		Message hapiMsg;
		try {
			// The parse method performs the actual parsing
			hapiMsg = p.parse(msg);

			HL7VitalSign vs = new HL7VitalSign();

			ORU_R01 oruR01 = (ORU_R01) hapiMsg;
			// System.out.println(oruR01);
			for (String name : oruR01.getNames()) {
				Structure st = oruR01.get(name);
				if (st instanceof ORU_R01_PATIENT_RESULT) {
					ORU_R01_PATIENT_RESULT orpr = (ORU_R01_PATIENT_RESULT) st;
					String tmp = name + "," + st + "," + String.join(",", orpr.getNames());
					ORU_R01_PATIENT patient = orpr.getPATIENT();
					if (patient != null) {
						PID pid = patient.getPID();
						if (pid != null) {
							PN pn = pid.getPatientName();
							if (pn != null) {
								tmp += pn.getPn2_GivenName() + " " + pn.getFamilyName();
							}

							if (pid.getPatientIDInternalID() != null && pid.getPatientIDInternalID().length > 0)
								tmp += ", IID=" + pid.getPatientIDInternalID()[0].getCm_pat_id1_IDNumber();

							if (pid.getDateOfBirth() != null)
								tmp += "," + pid.getDateOfBirth().getTs1_TimeOfAnEvent();
						}

						PV1 pv1 = patient.getPV1();
						if (pv1 != null) {
							if (pv1.getAssignedPatientLocation() != null) {
								tmp += ", BED=" + pv1.getAssignedPatientLocation().getBed();
								vs._bedno = pv1.getAssignedPatientLocation().getBed().getValue();
							}
						}
						MDILog.d(tmp);
					}

					List<ORU_R01_ORDER_OBSERVATION> observs = orpr.getORDER_OBSERVATIONAll();
					for (ORU_R01_ORDER_OBSERVATION observ : observs) {
						String tmp2 = "name=" + observ.getName() + ", names=" + String.join(",", observ.getNames());

						if (observ.getOBR() != null && observ.getOBR().getObservationDateTime() != null
								&& observ.getOBR().getObservationDateTime().getTs1_TimeOfAnEvent() != null) {
							TSComponentOne ts = observ.getOBR().getObservationDateTime().getTs1_TimeOfAnEvent();
							tmp2 += ts;
							vs._timestamp = ts.getValueAsDate().getTime();
						}
						MDILog.d(tmp2);

						List<ORU_R01_OBSERVATION> obs = observ.getOBSERVATIONAll();
						for (ORU_R01_OBSERVATION ob : obs) {
							OBX obx = ob.getOBX();
							String id = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
							String txt = obx.getObx3_ObservationIdentifier().getCe2_Text().getValue();
							String val = obx.getObx5_ObservationValue().getData().toString();
							String unit = obx.getObx6_Units().getCe2_Text().getValue();

							String valStr = id + "," + txt + "," + val + "," + unit;
							MDILog.d(valStr);

							String idUpper = id.toUpperCase();
							String txtUpper = txt.toUpperCase();

							if (_hl7fieldToObjFieldMap.containsKey(idUpper) || _hl7fieldToObjFieldMap.containsKey(txtUpper)) {
								Field field = _hl7fieldToObjFieldMap.get(idUpper) != null ? _hl7fieldToObjFieldMap.get(idUpper) : _hl7fieldToObjFieldMap.get(txtUpper);
								field.set(vs, val);
							}
						}
					}
				} else if (st instanceof DSC) {
					DSC dsc = (DSC) st;
					MDILog.d(name + "," + st + "," + String.join(",", dsc.getNames()));
				} else {
					MDILog.d(name + "," + st);
				}
			}
			
			CachedPublisher.put(vs);
		} catch (EncodingNotSupportedException e) {
			e.printStackTrace();
			MDILog.e(e);
		} catch (HL7Exception e) {
			e.printStackTrace();
			MDILog.e(e);
		} catch (Exception e) {
			e.printStackTrace();
			MDILog.e(e);
		}
	}

}