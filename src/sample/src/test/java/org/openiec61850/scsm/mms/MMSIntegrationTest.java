//package org.openiec61850.scsm.mms;
//
//import org.openiec61850.scsm.SCSMIntegrationTest;
//import org.openiec61850.scsm.mms.client.MmsScsmClientAssociation;
//import org.openiec61850.scsm.mms.server.MMSServerSAP;
//import org.openiec61850.server.IED;
//import org.openiec61850.server.scsm.SCSMServerSAP;
//
///**
// * Integration test for server and client (over MMS).
// * 
// * @author Tobias Weidelt
// * @author Stefan Feuerhahn
// */
//public class MMSIntegrationTest extends SCSMIntegrationTest {
//	private static final int MMS_PORT = 12342;
//
//	@BeforeClass
//	public static void setUpClass() throws Exception {
//		SCSMIntegrationTest.setUpClass();
//		setupServer();
//		setupClient();
//	}
//
//	protected static void setupClient() throws Exception {
//		client = new MmsScsmClientAssociation();
//		client.associate("localhost:" + MMS_PORT, "", 60000, 60000);
//	}
//
//	protected static void setupServer() throws Exception {
//		server = new IED();
//		server.addAccessPoint(accessPoint);
//
//		DataSourceFactory.theFactory().readConfig("config/dummy-iec-server.properties");
//		 
//		SCSMServerSAP sap = new MMSServerSAP(MMS_PORT, 0, null, accessPoint);
//		server.addSCSMSAP(sap);
//		server.run();
//	}
//
//	// @Test
//	// public void testMMSGetDataDefinition() throws ServiceError {
//	// // test some mms specific stuff
//	//
//	// super.testGetDataDefinition();
//	//
//	// String doRef = "IEDBiogasCHP/DSCH1.SchdAbsTm";
//	// IModelNode dd = client.getDataDefinition(doRef);
//	//
//	// // Expect entries ordered by MMSFunctionalConstraint - to match order in
//	// // GetDataValuesResponse which doesn't contain node names
//	// Iterator<IModelNode> it = dd.iterator();
//	// Assert.assertEquals("d", it.next().getNodeName());
//	// Assert.assertEquals("dU", it.next().getNodeName());
//	// Assert.assertEquals("numPts", it.next().getNodeName());
//	// Assert.assertEquals("val", it.next().getNodeName());
//	// Assert.assertEquals("rmpTyp", it.next().getNodeName());
//	// Assert.assertEquals("time", it.next().getNodeName());
//	// Assert.assertEquals("cdcNs", it.next().getNodeName());
//	// Assert.assertEquals("cdcName", it.next().getNodeName());
//	// Assert.assertEquals("dataNs", it.next().getNodeName());
//	// }
//
// }
