package io.hankers.mdi.philips_center;

import java.io.IOException;
import java.util.Map;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import io.hankers.mdi.mdi_utils.MDIConfig;
import io.hankers.mdi.mdi_utils.MDILog;

public class DataListener {

	private boolean _bRun = true;

	public void start() {
		HapiContext context = null;
		HL7Service server = null;

		while (_bRun) {
			try {
				int port = MDIConfig.getListenPort(); // The port to listen on
				boolean useTls = false; // Should we use TLS/SSL?
				context = new DefaultHapiContext();
				server = context.newServer(port, useTls);

				ReceivingApplication<Message> handler = new VitalSignHandler();
				server.registerApplication("ORU", "R01", handler);

				server.registerConnectionListener(new MyConnectionListener());

				server.setExceptionHandler(new MyExceptionHandler());

				server.startAndWait();
				
				MDILog.d("Listening on " + port);
				
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
				MDILog.w(e);

				try {
					if (server != null) {
						server.stopAndWait();
						server = null;
					}
				} catch (Exception e2) {
					e2.printStackTrace();
					MDILog.w(e2);
				}
				
				try {
					if (context != null) {
						context.close();
						context = null;
					}
				} catch (IOException e2) {
					e2.printStackTrace();
					MDILog.w(e2);
				}

				try {
					Thread.sleep(6000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	public static class MyConnectionListener implements ConnectionListener {

		public void connectionReceived(Connection theC) {
			System.out.println("New connection received: " + theC.getRemoteAddress().toString());
		}

		public void connectionDiscarded(Connection theC) {
			System.out.println("Lost connection from: " + theC.getRemoteAddress().toString());
		}

	}

	public static class MyExceptionHandler implements ReceivingApplicationExceptionHandler {

		public String processException(String theIncomingMessage, Map<String, Object> theIncomingMetadata,
				String theOutgoingMessage, Exception theE) throws HL7Exception {

			return theOutgoingMessage;
		}

	}

}
