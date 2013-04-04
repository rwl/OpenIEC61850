package org.openiec61850;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.openiec61850.jmms.iso.acse.AcseAssociation;
import org.openiec61850.jmms.mms.asn1.ConfirmedRequestPdu;
import org.openiec61850.jmms.mms.asn1.MmsPdu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ClientReceiver extends Thread {

	private static Logger logger = LoggerFactory.getLogger(ClientReceiver.class);

	private AcseAssociation acseAssociation;

	private BlockingQueue<MmsPdu> incomingResponses;
	private BlockingQueue<MmsPdu> incomingReports;
	private AtomicInteger expectedResponses = new AtomicInteger(0);

	private volatile boolean queueReports = false;
	private volatile boolean stopped = false;

	ClientReceiver(AcseAssociation acseAssociation, BlockingQueue<MmsPdu> incomingResponses,
			BlockingQueue<MmsPdu> incomingReports) {
		this.acseAssociation = acseAssociation;
		this.incomingResponses = incomingResponses;
		this.incomingReports = incomingReports;
	}

	@Override
	public void run() {
		try {
			ByteBuffer encodedResponsePdu = null;
			while (true) {
				try {
					encodedResponsePdu = acseAssociation.receive();
				} catch (SocketTimeoutException e) {
					if (stopped == false && expectedResponses.get() == 0) {
						continue;
					}
					else {
						shutdown();
						break;
					}
				}

				MmsPdu decodedResponsePdu = new MmsPdu();
				try {
					decodedResponsePdu.decode(
							new ByteArrayInputStream(encodedResponsePdu.array(), encodedResponsePdu.arrayOffset()
									+ encodedResponsePdu.position(), encodedResponsePdu.limit()
									- encodedResponsePdu.position()), null);
				} catch (IOException e) {
					logger.warn("unable to decode received message: " + e.getMessage());
					shutdown();
					break;
				}

				if (decodedResponsePdu.unconfirmedPDU != null) {
					if (decodedResponsePdu.unconfirmedPDU.unconfirmedService.informationReport.variableAccessSpecification.listOfVariable != null) {
						logger.debug("discarding LastApplError Report");
					}
					else if (queueReports == true) {
						try {
							incomingReports.put(decodedResponsePdu);
						} catch (InterruptedException e) {
						}
					}
					else {
						logger.debug("discarding report because reports are disabled");
					}
				}
				else {
					try {
						incomingResponses.put(decodedResponsePdu);
						if (expectedResponses.decrementAndGet() < 0) {
							logger.warn("unexpected incoming response");
							shutdown();
							break;
						}
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (IOException e) {
			if (stopped == false) {
				logger.info("server closed connection unexpectedly");
				shutdown();
			}
		} catch (IllegalStateException e) {
			logger.info("unexpected connection close");
			shutdown();
		}
	}

	void enableReportQueueing() {
		queueReports = true;
	}

	void disableReportQueueing() {
		queueReports = false;
	}

	void setResponseExpected() {
		expectedResponses.incrementAndGet();
	}

	void close() {
		stopped = true;
		try {
			acseAssociation.close();
		} catch (Throwable e) {
			// ignore
		}
	}

	void disconnect() {
		stopped = true;
		try {
			acseAssociation.disconnect();
		} catch (Throwable e) {
			// ignore
		}
	}

	private void shutdown() {
		if (stopped == false) {
			disconnect();
		}
		try {
			incomingResponses.put(new MmsPdu(new ConfirmedRequestPdu(), null, null, null, null, null));
			incomingReports.put(new MmsPdu(new ConfirmedRequestPdu(), null, null, null, null, null));
		} catch (InterruptedException e1) {
		}
	}
}
