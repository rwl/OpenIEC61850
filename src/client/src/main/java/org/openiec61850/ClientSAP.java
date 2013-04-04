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
package org.openiec61850;

import org.openiec61850.jmms.iso.acse.ClientAcseSAP;

public class ClientSAP {

	static final int MINIMUM_PDU_SIZE = 1000;

	private int proposedMaxPduSize = 65000;
	private int proposedMaxServOutstandingCalling = 5;
	private int proposedMaxServOutstandingCalled = 5;
	private int proposedDataStructureNestingLevel = 10;
	private byte[] servicesSupportedCalling = new byte[] { 0x40 & 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int servicesSupportedCallingNumBits = 85;

	private ClientAcseSAP acseSAP;

	public ClientSAP() {
		acseSAP = new ClientAcseSAP();
	}

	/**
	 * Set the maximum MMS Pdu size in bytes. The default size is 65000.
	 * 
	 * @param maxPduSize
	 *            should be at least MINIMUM_PDU_SIZE
	 */
	public void setMaxPduSize(int maxPduSize) {
		if (maxPduSize >= MINIMUM_PDU_SIZE) {
			this.proposedMaxPduSize = maxPduSize;
		}
		else {
			throw new IllegalArgumentException("maximum size is too small");
		}
	}

	public void setProposedMaxServOutstandingCalling(int proposedMaxServOutstandingCalling) {
		this.proposedMaxServOutstandingCalling = proposedMaxServOutstandingCalling;
	}

	public void setProposedMaxServOutstandingCalled(int proposedMaxServOutstandingCalled) {
		this.proposedMaxServOutstandingCalled = proposedMaxServOutstandingCalled;
	}

	public void setProposedDataStructureNestingLevel(int proposedDataStructureNestingLevel) {
		this.proposedDataStructureNestingLevel = proposedDataStructureNestingLevel;
	}

	public void setTSelRemote(byte[] tSelRemote) {
		acseSAP.tSAP.tSelRemote = tSelRemote;
	}

	public void setTSelLocal(byte[] tSelLocal) {
		acseSAP.tSAP.tSelLocal = tSelLocal;
	}

	/**
	 * Set the maxTpduSize. The default maximum TPDU size is 65531 (see RFC
	 * 1006). Only use this function if you want to change this.
	 * 
	 * @param maxTpduSizeParam
	 *            The maximum length is equal to 2^(maxTPDUSizeParam) octets.
	 *            Note that the actual TSDU size that can be transfered is equal
	 *            to TPDUSize-3. Default is 65531 octets (see RFC 1006), 7 <=
	 *            maxTPDUSizeParam <= 16, needs to be set before listening or
	 *            connecting
	 */
	public void setMaxTpduSizeParam(int maxTpduSizeParam) {
		acseSAP.tSAP.setMaxTPDUSizeParam(maxTpduSizeParam);
	}

	/**
	 * For the MMS-Mapping the ServerAccessPointReference should be of the form
	 * "ip:port" or "domain:port" or simply "ip" or "domain". The default port
	 * is 102. The AuthenticationParamter has no effect for the MMS-Mapping
	 * because Version 1.0 of IEC61850-8-1 does not support authentication.
	 * 
	 * The Associate service which tries to connect and build an association to
	 * an IEC 61850 server.
	 * 
	 * @param serverAccessPointReference
	 *            the syntax of this parameter is mapping specific
	 * @param authenticationParameter
	 *            the syntax of this parameter is mapping specific
	 * @param connectTimeout
	 *            the time in milliseconds after which the TCP/IP connection
	 *            attempt shall be aborted and a service error is thrown
	 * @throws ServiceError
	 * @throws IllegalArgumentException
	 */
	public ClientAssociation associate(String serverAccessPointReference, String authenticationParameter,
			int connectTimeout) throws ServiceError, IllegalArgumentException {
		return associate(serverAccessPointReference, authenticationParameter, connectTimeout, 0, 60000);
	}

	public ClientAssociation associate(String serverAccessPointReference, String authenticationParameter,
			int connectTimeout, int messageTimeout, int messageFragmentTimeout) throws ServiceError,
			IllegalArgumentException {

		acseSAP.tSAP.setConnectTimeout(connectTimeout);
		acseSAP.tSAP.setMessageTimeout(messageTimeout);
		acseSAP.tSAP.setMessageFragmentTimeout(messageFragmentTimeout);

		return new ClientAssociation(serverAccessPointReference, authenticationParameter, acseSAP, proposedMaxPduSize,
				proposedMaxServOutstandingCalling, proposedMaxServOutstandingCalled, proposedDataStructureNestingLevel,
				servicesSupportedCalling, servicesSupportedCallingNumBits);
	}

}
