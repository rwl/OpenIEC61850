import java.io.IOException;
import java.net.InetAddress;

import org.openmuc.jositransport.TConnection;
import org.openmuc.jositransport.ClientTSAP;

public class SampleClient {

	public static void main(String[] args) throws IOException {

		InetAddress address;
		int port = 10002;
		TConnection tConnection = null;
		byte[] tsdu = null;

		address = InetAddress.getByName("localhost");

		ClientTSAP tSAP = new ClientTSAP();
		tSAP.setMaxTPDUSizeParam(7);

		tConnection = tSAP.connectTo(address, port);

		byte[] testData = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
				(byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e,
				(byte) 0x0f };

		System.out.println("writing data");
		tConnection.send(testData,0,testData.length);

		System.out.println("Reading data...");
		tsdu = tConnection.receive();

		System.out.println(getByteArrayString(tsdu));

		tConnection.disconnect();

		System.out.println("quitting");
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

}
