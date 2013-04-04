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
import org.openiec61850.ModelNode;
import org.openiec61850.ServerModel;
import org.openiec61850.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class SampleClient {

	private static Logger logger = LoggerFactory.getLogger(SampleClient.class);

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
		ClientAssociation clientAssociation = clientSAP.associate(serverAddress + ":" + serverPort, null, 60000);

		// you may instead pass an association string:
		// ClientACSI mmsAssociation = clientSAP.associate(serverAddress + ":" +
		// serverPort, "demoauthval", 60000);

		ServerModel server = clientAssociation.retrieveModel();

		printRecursive(server);

		List<BasicDataAttribute> basicDataAttributes = server.getBasicDataAttributes();

		System.out.println("numBDAs: " + basicDataAttributes.size());

		for (BasicDataAttribute bda : basicDataAttributes) {
			clientAssociation.getDataValues(bda);
			clientAssociation.setDataValues(bda);
		}

		// the following demonstrates how you could read
		// INT32 daNode = (INT32) server.findSubNode(new
		// ObjectReference("SampleIEDDevice1/DGEN1.GnOpSt.stVal"),
		// FunctionalConstraint.ST);
		// daNode.setValue(2);
		// mmsAssociation.setDataValues(daNode);
		//
		// VISIBLE_STRING_255 desc = (VISIBLE_STRING_255) server.findSubNode(new
		// ObjectReference(
		// "SampleIEDDevice1/MMXU2.NamPlt.d"), FunctionalConstraint.DC);
		// desc.setValue("test test");
		// mmsAssociation.setDataValues(desc);
		//
		// ModelNode dobj = server.findSubNode(new
		// ObjectReference("SampleIEDDevice1/LPHD1.PhyNam.vendor"),
		// FunctionalConstraint.DC);
		// dobj = mmsAssociation.getDataValues(dobj);
		//
		// ModelNode dobj2 = server.findSubNode(new
		// ObjectReference("SampleIEDDevice1/MMXU1.NamPlt"),
		// FunctionalConstraint.DC);
		// dobj2 = mmsAssociation.getDataValues(dobj2);

		logger.debug("Client is done. Will quit.");
		clientAssociation.close();

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
