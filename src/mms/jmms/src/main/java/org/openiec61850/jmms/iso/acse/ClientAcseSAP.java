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

import org.openmuc.jositransport.ClientTSAP;

/**
 * This class implements the Service Access Point (SAP) for the Application
 * Control Service Element (ACSE) protocol as defined by ISO 8650 or ITU
 * X.217/X.227. The ACSE provides services for establishing and releasing
 * application-associations. The class also realizes the lower ISO Presentation
 * Layer as defined by ISO 8823/ITU X226 and the ISO Session Layer as defined by
 * 8327/ITU X.225.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class ClientAcseSAP {

	public ClientTSAP tSAP = null;

	static final byte[] P_SEL_DEFAULT = { 0, 0, 0, 1 };

	public byte[] pSelRemote = P_SEL_DEFAULT;
	public byte[] pSelLocal = P_SEL_DEFAULT;

	/**
	 * Use this constructor to create a client ACSE Service Access Point (SAP)
	 * that will start connections to remote ACSE SAPs. Once constructed the
	 * AcseSAP contains a public TSAP that can be accessed to set its
	 * configuration.
	 */
	public ClientAcseSAP() {
		this.tSAP = new ClientTSAP();
	}

	/**
	 * Associate to a remote ServerAcseSAP that is listening at the destination
	 * address.
	 * 
	 * @param address
	 *            remote InetAddress
	 * @param port
	 *            remote port
	 * @return the association object
	 * @throws IOException
	 *             is thrown if association was unsuccessful.
	 */
	public AcseAssociation associate(InetAddress address, int port, String authenticationParameter, ByteBuffer apdu)
			throws IOException {
		AcseAssociation acseAssociation = new AcseAssociation(null, pSelLocal);
		try {
			acseAssociation.startAssociation(apdu, address, port, authenticationParameter, pSelRemote, tSAP);
		} catch (IOException e) {
			acseAssociation.disconnect();
			throw e;
		}
		return acseAssociation;
	}

}
