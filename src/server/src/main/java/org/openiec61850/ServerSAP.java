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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.net.ServerSocketFactory;

import org.openiec61850.jmms.iso.acse.AcseAssociation;
import org.openiec61850.jmms.iso.acse.AcseAssociationListener;
import org.openiec61850.jmms.iso.acse.ServerAcseSAP;
import org.openiec61850.server.data.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSAP implements AcseAssociationListener {

	private static Logger logger = LoggerFactory.getLogger(ServerSAP.class);

	private AccessPoint accessPoint;
	private ServerStopListener sapStopListener;
	private ServerAcseSAP acseSAP;
	private int port;

	/**
	 * Create an MMSServerSAP with a ServerSocketFactory. The
	 * ServerSocketFactory could be used to create SSLServerSockets.
	 * 
	 * @param port
	 *            local port
	 * @param backlog
	 *            when 0 default value is used
	 * @param bindAddr
	 *            local ip address to bind to, pass null to bind to all
	 * @param accessPoint
	 *            the associated AccessPoint (implementing ServerACSI)
	 * @param serverSocketFactory
	 *            the factory class to generate the ServerSocket
	 */
	public ServerSAP(int port, int backlog, InetAddress bindAddr, AccessPoint accessPoint,
			ServerSocketFactory serverSocketFactory, ServerStopListener sapStopListener) {
		this.accessPoint = accessPoint;
		this.sapStopListener = sapStopListener;
		acseSAP = new ServerAcseSAP(port, backlog, bindAddr, this, serverSocketFactory);
	}

	public ServerSAP() {
	}

	/**
	 * Create a simple MMSServerSAP.
	 * 
	 * @param port
	 *            local port
	 * @param backlog
	 *            when 0 default value is used
	 * @param bindAddr
	 *            local ip address to bind to, pass null to bind to all
	 * @param accessPoint
	 *            the associated AccessPoint (implementing ServerACSI)
	 */
	public ServerSAP(int port, int backlog, InetAddress bindAddr, AccessPoint accessPoint,
			ServerStopListener sapStopListener) {
		this(port, backlog, bindAddr, accessPoint, ServerSocketFactory.getDefault(), sapStopListener);
	}

	public void init(AccessPoint accessPoint, ServerStopListener sapStopListener, Properties properties, String sapPropName)
			throws ConfigurationException {
		this.accessPoint = accessPoint;
		this.sapStopListener = sapStopListener;

		port = 102;
		String portString = properties.getProperty("openIEC61850.serverSAP." + sapPropName + ".port");
		if (portString != null) {
			port = Integer.parseInt(portString);
		}

		int backlog = 0;
		String backlogString = properties.getProperty("openIEC61850.serverSAP." + sapPropName + ".backlog");
		if (backlogString != null) {
			backlog = Integer.parseInt(backlogString);
		}

		String bindAddr = properties.getProperty("openIEC61850.serverSAP." + sapPropName + ".bindAddr");
		InetAddress inetAddress;
		if (bindAddr == null) {
			bindAddr = "0.0.0.0";
		}

		try {
			inetAddress = InetAddress.getByName(bindAddr);
		} catch (UnknownHostException e) {
			throw new ConfigurationException(e.getMessage(), e);
		}

		logger.info("Initializing MMS Server SAP: bindAddress={}, port={}, backlog={}", new Object[] { bindAddr, port,
				backlog });

		acseSAP = new ServerAcseSAP(port, backlog, inetAddress, this, ServerSocketFactory.getDefault());

	}

	/**
	 * Set the maxTPDUSize. The default maxTPDUSize is 65531 (see RFC 1006).
	 * Only use this function if you want to change this.
	 * 
	 * @param maxTPDUSizeParam
	 *            The maximum length is equal to 2^(maxTPDUSizeParam) octets.
	 *            Note that the actual TSDU size that can be transfered is equal
	 *            to TPDUSize-3. Default is 65531 octets (see RFC 1006), 7 <=
	 *            maxTPDUSizeParam <= 16, needs to be set before listening or
	 *            connecting
	 */
	public void setMaxTPDUSizeParam(int maxTPDUSizeParam) {
		acseSAP.serverTSAP.setMaxTPDUSizeParam(maxTPDUSizeParam);
	}

	/**
	 * Set the maximum number of connections that are allowed in parallel by the
	 * Server SAP.
	 * 
	 * @param maxConnections
	 *            the number of connections allowed (default is 100)
	 */
	public void setMaxConnections(int maxConnections) {
		acseSAP.serverTSAP.setMaxConnections(maxConnections);
	}

	/**
	 * Set the TConnection timeout for waiting for the first byte of a new
	 * message. Default is 0 (unlimited)
	 * 
	 * @param messageTimeout
	 *            in milliseconds
	 * @throws SocketException
	 */
	public void setMessageTimeout(int messageTimeout) throws SocketException {
		acseSAP.serverTSAP.setMessageTimeout(messageTimeout);
	}

	/**
	 * Set the TConnection timeout for receiving data once the beginning of a
	 * message has been received. Default is 2000 (2seconds)
	 * 
	 * @param messageFragmentTimeout
	 *            in milliseconds
	 * @throws SocketException
	 */
	public void setMessageFragmentTimeout(int messageFragmentTimeout) throws SocketException {
		acseSAP.serverTSAP.setMessageFragmentTimeout(messageFragmentTimeout);
	}

	public void startListening() throws IOException {
		logger.info("Starting to listen on port {}", port);
		acseSAP.startListening();
	}

	public void stopListening() {
		acseSAP.stopListening();
	}

	public void connectionIndication(AcseAssociation acseAssociation, ByteBuffer psdu) {
		ConnectionHandler mmsConnectionHandler = new ConnectionHandler(accessPoint);
		try {
			mmsConnectionHandler.connectionIndication(acseAssociation, psdu);
		} catch (ServiceError e) {
			// TODO send service error
			acseAssociation.disconnect();
			e.printStackTrace();
		} catch (IOException e) {
			if (acseAssociation != null) {
				acseAssociation.disconnect();
			}
			e.printStackTrace();
			// TODO to something
		}
	}

	public void serverStoppedListeningIndication(IOException e) {
		sapStopListener.serverStoppedListening(this);
	}

}
