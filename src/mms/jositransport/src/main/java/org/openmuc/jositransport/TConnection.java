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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Stefan Feuerhahn
 * @author Chau Do
 * 
 */

public class TConnection {

	private Socket socket;
	private volatile boolean valid = false;
	private volatile boolean connected = false;
	private DataOutputStream os;
	private DataInputStream is;
	private static Integer connectionCounter = 0;

	public byte[] tSelRemote = null;
	public byte[] tSelLocal = null;

	private int srcRef;
	private int dstRef;
	private byte sduBuffer[];
	private int maxTPDUSizeParam;
	private int maxTPDUSize;
	private int messageTimeout;
	private int messageFragmentTimeout;

	protected TConnection(Socket socket, int maxTPDUSizeParam, int messageTimeout, int messageFragmentTimeout)
			throws IOException {
		if (maxTPDUSizeParam < 7 || maxTPDUSizeParam > 16) {
			throw new RuntimeException("maxTPDUSizeParam is incorrect");
		}
		this.socket = socket;
		os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

		synchronized (connectionCounter) {
			connectionCounter++;
			connectionCounter %= 65520;
			if (connectionCounter == 0) {
				connectionCounter = 1; // some servers do not like srcRef 0
			}
			srcRef = connectionCounter;
		}

		this.messageTimeout = messageTimeout;
		this.messageFragmentTimeout = messageFragmentTimeout;
		this.maxTPDUSizeParam = maxTPDUSizeParam;
		maxTPDUSize = ClientTSAP.getMaxTPDUSize(maxTPDUSizeParam);
		this.valid = true;
	}

	/**
	 * This function is called once a client has connected to the server. It
	 * listens for a Connection Request (CR). If this is successful it replies
	 * afterwards with a Connection Confirm (CC). According to the norm a syntax
	 * error in the CR should be followed by an ER. This implementation does not
	 * send an ER because it seems unnecessary.
	 * 
	 * @throws IOException
	 */
	protected void listenForCR() throws IOException {
		if (!valid) {
			throw new IOException();
		}
		if (connected == true) {
			throw new IllegalStateException("already connected");
		}

		socket.setSoTimeout(messageFragmentTimeout);

		byte myByte;
		int lengthIndicator;
		int parameterLength;

		// start reading rfc 1006 header
		if (is.read() != 0x03) {
			throw new IOException();
		}
		if (is.read() != 0) {
			throw new IOException();
		}
		// read Packet length, but is not needed
		is.readShort();
		// reading rfc 1006 header finished

		lengthIndicator = is.read() & 0xff;
		// 0xe0 is the CR-Code
		if ((is.read() & 0xff) != 0xe0) {
			throw new IOException();
		}
		// DST-REF needs to be 0 in a CR packet
		if (is.readShort() != 0) {
			throw new IOException();
		}
		// read the srcRef which is the dstRef for this end-point
		dstRef = is.readShort() & 0xffff;
		// read class
		if ((is.read() & 0xff) != 0) {
			throw new IOException();
		}
		int variableBytesRead = 0;
		while (lengthIndicator > (6 + variableBytesRead)) {
			// read parameter code
			myByte = is.readByte();
			switch (myByte & 0xff) {
			case 0xc2:
				parameterLength = is.readByte() & 0xff;

				if (tSelLocal == null) {
					tSelLocal = new byte[parameterLength];
					is.readFully(tSelLocal);
				}
				else {
					if (parameterLength != tSelLocal.length) {
						throw new IOException("local T-SElECTOR is wrong.");
					}
					for (int i = 0; i < parameterLength; i++) {
						if ((tSelLocal[i] & 0xff) != is.read()) {
							throw new IOException("local T-SElECTOR is wrong.");
						}
					}
				}
				variableBytesRead += 2 + parameterLength;
				break;
			case 0xc1:
				parameterLength = is.readByte() & 0xff;

				if (tSelRemote == null) {
					tSelRemote = new byte[parameterLength];
					is.readFully(tSelRemote);
				}
				else {
					if (parameterLength != tSelRemote.length) {
						throw new IOException("remote T-SElECTOR is wrong.");
					}
					for (int i = 0; i < parameterLength; i++) {
						if ((tSelRemote[i] & 0xff) != is.read()) {
							throw new IOException("remote T-SElECTOR is wrong.");
						}
					}
				}
				variableBytesRead += 2 + parameterLength;
				break;

			case 0xc0:
				if ((is.readByte() & 0xff) != 1) {
					throw new IOException();
				}
				myByte = is.readByte();
				int newMaxTPDUSizeParam = (myByte & 0xff);
				if (newMaxTPDUSizeParam < 7 || newMaxTPDUSizeParam > 16) {
					throw new IOException("maxTPDUSizeParam is out of bound");
				}
				else {
					if ((newMaxTPDUSizeParam) < maxTPDUSizeParam) {
						maxTPDUSizeParam = (newMaxTPDUSizeParam);
						maxTPDUSize = ClientTSAP.getMaxTPDUSize(maxTPDUSizeParam);
					}
				}
				variableBytesRead += 3;
				break;
			default:
				throw new IOException();
			}

		}

		// write RFC 1006 Header
		os.write(0x03);
		os.write(0x00);
		// write complete packet length

		int variableLength = 0;
		if (maxTPDUSizeParam < 16 && maxTPDUSizeParam >= 7) {
			variableLength += 3;
		}
		if (tSelLocal != null) {
			variableLength += 2 + tSelLocal.length;
		}
		if (tSelRemote != null) {
			variableLength += 2 + tSelRemote.length;
		}
		os.writeShort(4 + 7 + variableLength);

		// write connection request (CR) TPDU (§13.3)

		// write length indicator
		os.write(6 + variableLength);

		// write fixed part
		// write CC CDT
		os.write(0xd0);
		// write DST-REF
		os.writeShort(dstRef);
		// write SRC-REF
		os.writeShort(srcRef);
		// write class
		os.write(0);

		// write variable part
		if (tSelLocal != null) {
			os.write(0xc2);
			os.write(tSelLocal.length);
			os.write(tSelLocal);
		}

		if (tSelRemote != null) {
			os.write(0xc1);
			os.write(tSelRemote.length);
			os.write(tSelRemote);
		}
		if (maxTPDUSizeParam < 16 && maxTPDUSizeParam >= 7) {
			// write proposed maximum TPDU Size
			os.write(0xc0);
			os.write(1);
			os.write(maxTPDUSizeParam);
		}
		os.flush();

		connected = true;
	}

	/**
	 * Starts a connection, sends a CR, waits for a CC and throws an IOException
	 * if not successful
	 * 
	 * @throws IOException
	 */
	protected void startConnection() throws IOException {
		if (!valid) {
			throw new IOException();
		}
		if (connected == true) {
			throw new IllegalStateException("already connected");
		}

		socket.setSoTimeout(messageTimeout);

		// write RFC 1006 Header
		os.write(0x03);
		os.write(0x00);

		// write complete packet length
		int variableLength = 0;
		if (maxTPDUSizeParam < 16 && maxTPDUSizeParam >= 7) {
			variableLength += 3;
		}
		if (tSelLocal != null) {
			variableLength += 2 + tSelLocal.length;
		}
		if (tSelRemote != null) {
			variableLength += 2 + tSelRemote.length;
		}
		os.writeShort(4 + 7 + variableLength);
		// writing RFC 1006 Header finished

		// write connection request (CR) TPDU (§13.3)

		// write length indicator
		os.write(6 + variableLength);

		// write fixed part
		// write CR CDT
		os.write(0xe0);
		// write DST-REF
		os.write(0);
		os.write(0);
		// write SRC-REF
		os.writeShort(srcRef);
		// write class
		os.write(0);

		// write variable part
		if (maxTPDUSizeParam < 16 && maxTPDUSizeParam >= 7) {
			// write proposed maximum TPDU Size
			os.write(0xc0);
			os.write(1);
			os.write(maxTPDUSizeParam);
		}
		if (tSelRemote != null) {
			os.write(0xc2);
			os.write(tSelRemote.length);
			os.write(tSelRemote);
		}
		if (tSelLocal != null) {
			os.write(0xc1);
			os.write(tSelLocal.length);
			os.write(tSelLocal);
		}

		os.flush();

		byte myByte;
		int lengthIndicator;
		int parameterLength;
		if (is.readByte() != 0x03) {
			throw new IOException();
		}
		if (is.readByte() != 0) {
			throw new IOException();
		}
		// read Packet length, but is not needed
		is.readShort();
		lengthIndicator = is.readByte() & 0xff;
		if ((is.readByte() & 0xff) != 0xd0) {
			throw new IOException();
		}
		// read the dstRef which is the srcRef for this end-point
		is.readShort();
		// read the srcRef which is the dstRef for this end-point
		dstRef = is.readShort() & 0xffff;
		// read class
		if (is.readByte() != 0) {
			throw new IOException();
		}

		int variableBytesRead = 0;
		while (lengthIndicator > (6 + variableBytesRead)) {
			// read parameter code
			myByte = is.readByte();
			switch (myByte & 0xff) {

			case 0xc1:
				parameterLength = is.readByte() & 0xff;

				if (tSelLocal == null) {
					tSelLocal = new byte[parameterLength];
					is.readFully(tSelLocal);
				}
				else {
					if (parameterLength != tSelLocal.length) {
						throw new IOException("local T-SElECTOR is wrong.");
					}
					for (int i = 0; i < parameterLength; i++) {
						if ((tSelLocal[i] & 0xff) != is.read()) {
							throw new IOException("local T-SElECTOR is wrong.");
						}
					}
				}
				variableBytesRead += 2 + parameterLength;
				break;
			case 0xc2:
				parameterLength = is.readByte() & 0xff;

				if (tSelRemote == null) {
					tSelRemote = new byte[parameterLength];
					is.readFully(tSelRemote);
				}
				else {
					if (parameterLength != tSelRemote.length) {
						throw new IOException("remote T-SElECTOR is wrong.");
					}
					for (int i = 0; i < parameterLength; i++) {
						if ((tSelRemote[i] & 0xff) != is.read()) {
							throw new IOException("remote T-SElECTOR is wrong.");
						}
					}
				}
				variableBytesRead += 2 + parameterLength;
				break;

			case 0xc0:
				if ((is.readByte() & 0xff) != 1) {
					throw new IOException();
				}
				myByte = is.readByte();
				if ((myByte & 0xff) < 7 || (myByte & 0xff) > maxTPDUSizeParam) {
					throw new IOException();
				}
				else {
					if ((myByte & 0xff) < maxTPDUSizeParam) {
						maxTPDUSizeParam = (myByte & 0xff);
					}
				}
				variableBytesRead += 4;
				break;
			default:
				throw new IOException();
			}

		}

		connected = true;
	}

	public void send(List<byte[]> tsdus, List<Integer> offsets, List<Integer> lengths) throws IOException {
		if (!valid) {
			throw new IOException();
		}
		if (connected == false) {
			throw new IllegalStateException("not connected");
		}

		int bytesLeft = 0;
		// for (byte[] tsdu : tsdus) {
		// bytesLeft += tsdu.length;
		// }
		for (int length : lengths) {
			bytesLeft += length;
		}
		int tsduOffset = 0;
		int byteArrayListIndex = 0;
		int numBytesToWrite;
		boolean lastPacket = false;
		int maxTSDUSize = maxTPDUSize - 3;
		while (bytesLeft > 0) {

			if (bytesLeft > maxTSDUSize) {
				numBytesToWrite = maxTSDUSize;
			}
			else {
				numBytesToWrite = bytesLeft;
				lastPacket = true;
			}

			// --write RFC 1006 Header--
			// write Version
			os.write(0x03);
			// write reserved bits
			os.write(0);
			// write packet Length
			os.writeShort(numBytesToWrite + 7);

			// --write 8073 Header--
			// write Length Indicator of header
			os.write(0x02);
			// write TPDU Code for DT Data
			os.write(0xf0);
			// write TPDU-NR and EOT, TPDU-NR is always 0 for class 0
			if (lastPacket) {
				os.write(0x80);
			}
			else {
				os.write(0x00);
			}

			bytesLeft -= numBytesToWrite;
			while (numBytesToWrite > 0) {
				byte[] tsdu = tsdus.get(byteArrayListIndex);
				int length = lengths.get(byteArrayListIndex);
				int offset = offsets.get(byteArrayListIndex);

				int tsduWriteLength = length - tsduOffset;

				if (numBytesToWrite > tsduWriteLength) {
					os.write(tsdu, offset + tsduOffset, tsduWriteLength);
					numBytesToWrite -= tsduWriteLength;
					tsduOffset = 0;
					byteArrayListIndex++;
				}
				else {
					os.write(tsdu, offset + tsduOffset, numBytesToWrite);
					if (numBytesToWrite == tsduWriteLength) {
						tsduOffset = 0;
						byteArrayListIndex++;
					}
					else {
						tsduOffset += numBytesToWrite;
					}
					numBytesToWrite = 0;
				}
			}

			os.flush();
		}
	}

	public void send(byte[] tsdu, int offset, int length) throws IOException {
		List<byte[]> tsdus = new ArrayList<byte[]>();
		tsdus.add(tsdu);
		List<Integer> offsets = new ArrayList<Integer>();
		offsets.add(offset);
		List<Integer> lengths = new ArrayList<Integer>();
		lengths.add(length);
		send(tsdus, offsets, lengths);
	}

	public int getMessageTimeout() {
		return this.messageTimeout;
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
		this.messageTimeout = messageTimeout;
	}

	public int getMessageFragmentTimeout() {
		return this.messageFragmentTimeout;
	}

	/**
	 * Set the TConnection timeout for receiving data once the beginning of a
	 * message has been received. Default is 60000 (60 seconds)
	 * 
	 * @param messageFragmentTimeout
	 *            in milliseconds
	 * @throws SocketException
	 */
	public void setMessageFragmentTimeout(int messageFragmentTimeout) throws SocketException {
		this.messageFragmentTimeout = messageFragmentTimeout;
	}

	/**
	 * This function will throw an EOFException in case a Disconnect Request
	 * (DR) or ErrorPDU (ER) was received or the socket was simply closed. An
	 * IOException is thrown in case of a syntax error. A SocketTimeoutException
	 * is thrown if a MessageTimeout or MessageFragmentTimeout occurred. For all
	 * exceptions except the MessageTimeout the connection is closed by this
	 * function.
	 * 
	 * @throws IOException
	 */
	public byte[] receive() throws IOException {
		if (!valid) {
			throw new IOException();
		}

		if (connected == false) {
			throw new IllegalStateException("not connected");
		}

		int packetLength;
		int eot = 0;
		int bytesWritten = 0;
		int bytesCopied = 0;
		int LI = 0;
		int tpduCode;
		boolean soTimeoutSet = false;
		byte[] sdu = null;
		Vector<byte[]> bufferVector = new Vector<byte[]>();

		try {
			socket.setSoTimeout(messageTimeout);

			do {

				// read version
				if ((is.readByte() & 0xff) != 3) {
					close();
					throw new IOException("Syntax error: version not equal to 3");
				}

				if (!soTimeoutSet) {
					socket.setSoTimeout(messageFragmentTimeout);
					soTimeoutSet = true;
				}

				// read reserved
				if (is.readByte() != 0) {
					close();
					throw new IOException("Syntax error: reserved not equal to 0");
				}
				// read Packet length
				packetLength = is.readShort() & 0xffff;
				if (packetLength <= 7) {
					close();
					throw new IOException("Syntax error: packet length parameter < 7");
				}

				// read length indicator
				LI = is.readByte() & 0xff;

				// read TPDU code
				tpduCode = is.readByte() & 0xff;

				if (tpduCode == 0xf0) {
					// Data Transfer (DT) Code

					if (LI != 2) {
						throw new IOException("Syntax error: LI field does not equal 2");
					}

					// read EOT
					eot = is.readByte() & 0xff;
					if (eot != 0 && eot != 0x80) {
						throw new IOException("Syntax error: eot wrong");
					}

					// eot = End of TSDU
					// if this is not the last packet
					if (eot != 0x80) {
						sduBuffer = new byte[packetLength - 7];
						// is.readFully(sduBuffer, bytesWritten, packetLength -
						// 7);
						is.readFully(sduBuffer);
						bytesWritten += packetLength - 7;
						bufferVector.add(sduBuffer);

					}
				}
				else if (tpduCode == 0x80) {
					// Disconnect Request (DR)

					if (LI != 6) {
						throw new IOException();
					}

					// check if the DST-REF field is set to the reference of the
					// receiving entity -> srcRef
					if (is.readShort() != srcRef) {
						throw new IOException("Syntax error: srcRef wrong");
					}

					// check if the SRC-REF field is that of the entity sending
					// the DR
					if (is.readShort() != dstRef) {
						throw new IOException("Syntax error: dstRef wrong");
					}

					// check the reason field, for class 0 only between 1 and 4
					int reason = is.readByte() & 0xff;
					if (reason < 0 || reason > 4) {
						throw new IOException("Syntax error: reason out of bound");
					}

					// Disconnect is valid, throw exception
					throw new EOFException("Disconnect request. Reason:" + reason);

				}
				else if (tpduCode == 0x80) {
					throw new EOFException("Got TPDU error (ER) message");
				}
				else {
					throw new IOException("Syntax error: unknown TPDU code");
				}
			} while (eot != 0x80);

			sdu = new byte[bytesWritten + packetLength - 7];
			Iterator<byte[]> it = bufferVector.iterator();
			while (it.hasNext()) {
				sduBuffer = it.next();
				System.arraycopy(sduBuffer, 0, sdu, bytesCopied, sduBuffer.length);
				bytesCopied += sduBuffer.length;
			}
			is.readFully(sdu, bytesWritten, packetLength - 7);

		} catch (SocketTimeoutException ste) {
			if (soTimeoutSet == true) {
				close();
				throw new SocketTimeoutException("MessageFragmentTimeout, socket closed");
			}
			throw new SocketTimeoutException("MessageTimeout");
		} catch (IOException e) {
			close();
			throw e;
		}

		return sdu;
	}

	/**
	 * This function sends a Disconnect Request but does not wait for a
	 * Disconnect Confirm.
	 */
	public void disconnect() {
		if (!valid) {
			return;
		}
		try {
			// write header for rfc
			// write version
			os.write(0x03);
			// write reserved
			os.write(0x00);

			// write packet length
			os.writeShort(4 + 7); // this does not include the variable part
			// which
			// contains additional user information for
			// disconnect

			// beginning of ISO 8073 header
			// write LI
			os.write(0x06);

			// write DR
			os.write(0x80);

			// write DST-REF
			os.writeShort(dstRef);

			// write SRC-REF
			os.writeShort(srcRef);

			// write reason - 0x00 corresponds to reason not specified. Can
			// write
			// the reasons as case structure, but need input from client
			os.write(0x00);

			os.flush();
		} catch (IOException e) {
		} finally {
			close();
		}
	}

	public void close() {
		valid = false;
		connected = false;
		if (socket != null && socket.isBound()) {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
		if (os != null) {
			try {
				os.close();
			} catch (Exception e) {
			}
		}

		if (is != null) {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
	}
}
