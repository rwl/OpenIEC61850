/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of jMMS.
 * For more information visit http://www.openmuc.org 
 *
 * jMMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jMMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jMMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openiec61850.jmms.iso.acse;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import javax.net.ServerSocketFactory;

import org.openmuc.jositransport.ServerTSAP;
import org.openmuc.jositransport.TConnection;
import org.openmuc.jositransport.TConnectionListener;

/**
 * This class implements the server Service Access Point (SAP) for the
 * Application Control Service Element (ACSE) protocol as defined by ISO 8650 or
 * ITU X.217/X.227. The ACSE provides services for establishing and releasing
 * application-associations. The class also realizes the lower ISO Presentation
 * Layer as defined by ISO 8823/ITU X226 and the ISO Session Layer as defined by
 * 8327/ITU X.225.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class ServerAcseSAP implements TConnectionListener {

	private AcseAssociationListener associationListener = null;
	public ServerTSAP serverTSAP = null;

	public byte[] pSelLocal = ClientAcseSAP.P_SEL_DEFAULT;

	/**
	 * Use this constructor to create a server ACSE SAP that listens on a fixed
	 * port.
	 * 
	 * @param associationListener
	 *            the AssociationListener that will be notified when remote
	 *            clients have associated. Once constructed the AcseSAP contains
	 *            a public TSAP that can be accessed to set its configuration.
	 */
	public ServerAcseSAP(int port, int backlog, InetAddress bindAddr, AcseAssociationListener associationListener) {
		this(port, backlog, bindAddr, associationListener, ServerSocketFactory.getDefault());
	}

	/**
	 * Use this constructor to create a server ACSE SAP that listens on a fixed
	 * port.
	 * 
	 * @param associationListener
	 *            the AssociationListener that will be notified when remote
	 *            clients have associated. Once constructed the AcseSAP contains
	 *            a public TSAP that can be accessed to set its configuration.
	 */
	public ServerAcseSAP(int port, int backlog, InetAddress bindAddr, AcseAssociationListener associationListener,
			ServerSocketFactory serverSocketFactory) {
		this.associationListener = associationListener;
		this.serverTSAP = new ServerTSAP(port, backlog, bindAddr, this);
	}

	/**
	 * Start listening for incoming connections. Only for server SAPs.
	 * 
	 * @throws IOException
	 */
	public void startListening() throws IOException {
		if (associationListener == null || serverTSAP == null) {
			throw new IllegalStateException("AcseSAP is unable to listen because it was not initialized.");
		}
		serverTSAP.startListening();
	}

	/**
	 * Stop listing on the port. Only for server SAPs.
	 */
	public void stopListening() {
		serverTSAP.stopListening();
	}

	/**
	 * This function is internal and should not be called by users of this
	 * class.
	 */
	public void serverStoppedListeningIndication(IOException e) {
		associationListener.serverStoppedListeningIndication(e);
	}

	/**
	 * This function is internal and should not be called by users of this
	 * class.
	 * 
	 */
	public void connectionIndication(TConnection tConnection) {
		AcseAssociation acseAssociation = new AcseAssociation(tConnection, pSelLocal);

		ByteBuffer asdu = null;
		try {
			asdu = acseAssociation.listenForCN();
		} catch (IOException e) {
			System.err.println("Server: Connection unsuccessful. IOException:" + e.getMessage());
			tConnection.disconnect();
			return;
		}
		associationListener.connectionIndication(acseAssociation, asdu);
	}
}
