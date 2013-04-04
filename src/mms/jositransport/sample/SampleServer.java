import java.io.IOException;

import org.openmuc.jositransport.ServerTSAP;
import org.openmuc.jositransport.TConnection;
import org.openmuc.jositransport.TConnectionListener;


/**
 * This is a sample jOSITransport server.
 * 
 */
public class SampleServer implements TConnectionListener {

	// port to listen on
	static private final int port = 10002;

	public static void main(String args[]) throws IOException {

		ServerTSAP serverTSAP = new ServerTSAP(port, new SampleServer());

		System.out.println("Starting to listen on port: " + port);

		serverTSAP.startListening();

		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		serverTSAP.stopListening();
		System.out.println("Stopped listening on the port.");
	}

	public void connectionIndication(TConnection tConnection) {

		System.out.println("\nSomeone connected...");

		byte[] tsdu = null;

		System.out.println("Reading data...");
		try {
			tsdu = tConnection.receive();
		} catch (IOException e1) {
			System.err.println("Caught exception reading data:");
			e1.printStackTrace();
			return;
		}

		System.out.println(getByteArrayString(tsdu));

		System.out.println("Writing data");
		try {
		    tConnection.send(tsdu,0,tsdu.length);
		} catch (IOException e) {
			System.err.println("Caught exception writing data:");
			e.printStackTrace();
			return;
		}

		System.out.println("Disconnecting...");
		tConnection.disconnect();
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

	public void serverStoppedListeningIndication(IOException e) {
		System.out.println("Got indication that Server stopped listening.");
	}

}
