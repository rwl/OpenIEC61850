package org.openiec61850.jmms.iso.acse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class ClientServerITest {

	private final byte[] mmsInitRequestPDU = new byte[] { (byte) 0xa8, 0x26, (byte) 0x80, 0x03, 0x00, (byte) 0xfd,
			(byte) 0xe8, (byte) 0x81, 0x01, 0x06, (byte) 0x82, 0x01, 0x06, (byte) 0x83, 0x01, 0x06, (byte) 0xa4, 0x16,
			(byte) 0x80, 0x01, 0x01, (byte) 0x81, 0x03, 0x05, (byte) 0xf1, 0x00, (byte) 0x82, 0x0c, 0x03, (byte) 0xee,
			0x08, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, (byte) 0xef, 0x18 };

	private final byte[] testData = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
			(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
			(byte) 0x0e, (byte) 0x0f, };

	public class SampleServer implements AcseAssociationListener {

		public void serverStoppedListeningIndication(IOException e) {
		}

		public void connectionIndication(AcseAssociation acseAssociation, ByteBuffer data) {

			try {
				acseAssociation.accept(data);
			} catch (IOException e) {
				System.err.println("Caught accepting association:");
				e.printStackTrace();
				return;
			}

			ByteBuffer receivedData = null;
			try {
				receivedData = acseAssociation.receive();
			} catch (IOException e) {
				System.err.println("Caught exception receiving data:");
				e.printStackTrace();
				return;
			}

			try {
				acseAssociation.send(receivedData);
			} catch (IOException e) {
				System.err.println("Caught exception sending data:");
				e.printStackTrace();
				return;
			}

		}
	}

	@Test
	public void testClientServerCom() throws IOException {

		int port = 14322;

		ServerAcseSAP serverAcseSAP = new ServerAcseSAP(port, 0, null, new SampleServer());
		serverAcseSAP.serverTSAP.setMessageTimeout(6000);

		serverAcseSAP.startListening();

		InetAddress address = InetAddress.getByName("127.0.0.1");

		ClientAcseSAP clientAcseSAP = new ClientAcseSAP();
		clientAcseSAP.tSAP.setMaxTPDUSizeParam(7);
		clientAcseSAP.tSAP.setMessageTimeout(6000);
		clientAcseSAP.pSelLocal = new byte[] { 0x1, 0x1, 0x1, 0x1 };

		AcseAssociation acseAssociation = clientAcseSAP.associate(address, port, null,
				ByteBuffer.wrap(mmsInitRequestPDU));

		ByteBuffer associationResponsePDU = acseAssociation.getAssociateResponseAPDU();

		Assert.assertThat(findSubArr(associationResponsePDU.array(), mmsInitRequestPDU), is(not(0)));

		acseAssociation.send(ByteBuffer.wrap(testData));

		ByteBuffer receivedData = acseAssociation.receive();

		Assert.assertThat(findSubArr(receivedData.array(), testData), is(not(0)));

		acseAssociation.disconnect();

		serverAcseSAP.stopListening();
	}

	public static int findArray(Byte[] array, Byte[] subArray) {
		return Collections.indexOfSubList(Arrays.asList(array), Arrays.asList(subArray));
	}

	public static String getByteArrayString(byte[] byteArray) {
		StringBuilder builder = new StringBuilder();
		int l = 1;
		for (byte b : byteArray) {
			if ((l != 1) && ((l - 1) % 8 == 0)) {
				builder.append(' ');
			}
			if ((l != 1) && ((l - 1) % 16 == 0)) {
				builder.append('\n');
			}
			l++;
			builder.append("0x");
			String hexString = Integer.toHexString(b & 0xff);
			if (hexString.length() == 1) {
				builder.append(0);
			}
			builder.append(hexString + " ");
		}
		return builder.toString();
	}

	int findSubArr(byte[] arr, byte[] subarr) {
		int lim = arr.length - subarr.length;
		byte[] tmpArr = new byte[subarr.length];
		for (int i = 0; i <= lim; i++) {
			System.arraycopy(arr, i, tmpArr, 0, subarr.length);
			if (Arrays.equals(tmpArr, subarr))
				return i; // returns starting index of sub array
		}
		return -1;// return -1 on finding no sub-array

	}

}
