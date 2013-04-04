/*
 * Copyright Fraunhofer ISE, 2010
 *    
 * This file is part of jOSITransport.
 * For more information visit http://www.openmuc.org 
 * 
 * jOSITransport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * jOSITransport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jOSITransport.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.jositransport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class extends Thread. It is started by ServerTSAP and listens on a
 * socket for connections and hands them to the ConnectionHandler class. It
 * notifies ConnectionListener if the socket is closed.
 * 
 * @author Stefan Feuerhahn
 * 
 */
class ServerThread extends Thread {

	private ServerSocket serverSocket;
	private int numConnections = 0;
	private ServerTSAP serverTSAP;
	protected int maxTPDUSizeParam;
	private int maxConnections;
	protected int messageTimeout;
	protected int messageFragmentTimeout;
	private boolean stopServer = false;

	protected ServerThread(ServerTSAP serverTSAP, ServerSocket socket, int maxTPDUSizeParam, int maxConnections,
			int messageTimeout, int messageFragmentTimeout) {
		this.serverTSAP = serverTSAP;
		serverSocket = socket;
		this.maxTPDUSizeParam = maxTPDUSizeParam;
		this.maxConnections = maxConnections;
		this.messageTimeout = messageTimeout;
		this.messageFragmentTimeout = messageFragmentTimeout;
	}

	@Override
	public void run() {

		ExecutorService executor = Executors.newFixedThreadPool(maxConnections);
		try {

			Socket clientSocket = null;

			while (true) {
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					if (stopServer == false) {
						serverTSAP.getConnectionListener().serverStoppedListeningIndication(e);
					}
					return;
				}

				if (numConnections < maxConnections) {
					numConnections++;
					ConnectionHandler myConnectionHandler = new ConnectionHandler(clientSocket, this);
					executor.execute(myConnectionHandler);
				}
				else {
					System.err
							.println("Transport Layer Server: Maximum number of connections reached. Ignoring connection request. NumConnections: "
									+ numConnections);
				}

			}
		} finally {
			executor.shutdown();
		}

	}

	protected void connectionIndication(TConnection tConnection) {
		serverTSAP.getConnectionListener().connectionIndication(tConnection);
	}

	protected void removeHandler(ConnectionHandler handler) {
		numConnections--;
	}

	/**
	 * stops listening on the port but does not close all connections
	 */
	public void stopServer() {
		stopServer = true;
		if (serverSocket != null && serverSocket.isBound()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
	}

}