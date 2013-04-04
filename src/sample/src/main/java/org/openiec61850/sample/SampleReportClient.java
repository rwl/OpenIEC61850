package org.openiec61850.sample;

/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of openIEC61850.
 * For more information visit http://www.openmuc.org 
 *
 * openIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * openIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with openIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.util.List;

import org.openiec61850.BasicDataAttribute;
import org.openiec61850.ClientAssociation;
import org.openiec61850.ClientSAP;
import org.openiec61850.DaBoolean;
import org.openiec61850.DaVisibleString;
import org.openiec61850.LogicalNode;
import org.openiec61850.ModelNode;
import org.openiec61850.ServerModel;
import org.openiec61850.ServiceError;
import org.openiec61850.common.model.report.Report;
import org.openiec61850.common.model.report.ReportControlBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Stefan Feuerhahn
 * 
 *         Note that this class is for demonstration purposes only. Since the
 *         openIEC61850 server does not support reporting yet it cannot be run
 *         in combination with the SampleServer.
 * 
 */
public class SampleReportClient extends Thread {

	private static Logger logger = LoggerFactory.getLogger(SampleReportClient.class);

	static ClientAssociation clientAssociation = null;

	public static void main(String[] args) throws InterruptedException, ServiceError {

		if (args.length != 2) {
			System.out.println("usage: org.openiec61850.sample.SampleMMSClient <host> <port>");
			return;
		}

		String serverAddress = args[0];
		int serverPort = Integer.parseInt(args[1]);

		logger.debug("Using MMS");
		ClientSAP clientSAP = new ClientSAP();

		// optionally you can set the remote and local Transport Selectors and
		// the Maximum TPDUSize:
		clientSAP.setTSelRemote(new byte[] { 0, 1 });
		clientSAP.setTSelLocal(new byte[] { 0, 0 });
		clientSAP.setMaxTpduSizeParam(10);

		logger.debug("Associating with server " + serverAddress + " on port " + serverPort);
		// ClientACSI mmsAssociation = clientSAP.associate(serverAddress + ":" +
		// serverPort, "", 60000);

		clientAssociation = clientSAP.associate(serverAddress + ":" + serverPort, null, 60000);

		ServerModel server = clientAssociation.retrieveModel();

		printRecursive(server);

		List<BasicDataAttribute> basicDataAttributes = server.getBasicDataAttributes();

		System.out.println("numBDAs: " + basicDataAttributes.size());

		// for (BasicDataAttribute bda : basicDataAttributes) {
		// mmsAssociation.getDataValues(bda);
		// mmsAssociation.setDataValues(bda);
		// }

		SampleReportClient reportHandlerThread = new SampleReportClient();
		reportHandlerThread.start();

		// you have to replace the reference with the correct reference of the
		// URCB that you want:
		ReportControlBlock urcb = ((LogicalNode) server.findSubNode("SomeLDName/LLN0"))
				.getReportControlBlock("exampleURCB");
		if (urcb == null) {
			logger.error("ReportControlBlock not found");
		}
		else {
			clientAssociation.getRCBValues(urcb);
			logger.debug("urcb name: " + urcb.getName());
			logger.debug("urcb name: " + urcb.getNodeName());
			logger.debug("RptID: " + ((DaVisibleString) urcb.getChild("RptID")).getValue());
			((DaBoolean) urcb.getChild("RptEna")).setValue(true);
			((DaBoolean) urcb.getChild("GI")).setValue(true);
			clientAssociation.setRCBValues(urcb);
		}
		logger.debug("Client is done. Will quit.");

	}

	@Override
	public void run() {
		Report report = null;
		try {
			report = clientAssociation.getReport();
		} catch (ServiceError e) {
			e.printStackTrace();
		}

		logger.debug("got report with dataset ref: " + report.getDataSet().getReferenceStr());
		// do something with the report

	}

	private static void printRecursive(ModelNode modelNode) {
		logger.debug(modelNode.toString());
		if (modelNode.getChildren() != null) {
			for (ModelNode modelNodeChild : modelNode.getChildren()) {
				printRecursive(modelNodeChild);
			}
		}
	}

}
