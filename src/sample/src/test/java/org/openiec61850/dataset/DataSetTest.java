//package org.openiec61850.dataset;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Vector;
//
//import org.openiec61850.client.ClientACSI;
//import org.openiec61850.client.ClientACSI.ServerDirectoryType;
//import org.openiec61850.common.ACSIClass;
//import org.openiec61850.common.ServiceError;
//import org.openiec61850.common.model.FunctionalConstraint;
//<<<<<<< HEAD
//import org.openiec61850.common.model.ModelNode;
//import org.openiec61850.common.model.datypes.DAType;
//=======
//import org.openiec61850.common.model.basictypes.BasicType;
//>>>>>>> reportingLogging
//import org.openiec61850.scsm.mms.client.MmsScsmClient;
//import org.openiec61850.scsm.mms.server.MMSServerSAP;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.IED;
//import org.openiec61850.server.scl.ICDParser;
//import org.openiec61850.server.scsm.SCSMServerSAP;
//
///**
// * Test for data set services between client and server
// * 
// * 
// */
//
//public class DataSetTest {
//	private static final int MMS_PORT = 12343;
//	protected static ICDParser scl;
//	protected static AccessPoint accessPoint;
//	protected static IED server;
//	protected static ClientACSI client;
//
//	@BeforeClass
//	public static void setUpClass() throws Exception {
//		setupSCLParser();
//		setupAccessPoint();
//		setupServer();
//		setupClient();
//		setupModel();
//	}
//
//	protected static void setupClient() throws Exception {
//		client = new MmsScsmClient();
//		client.associate("localhost:" + MMS_PORT, "", 60000, 60000);
//	}
//
//	protected static void setupServer() throws Exception {
//		server = new IED();
//		server.addAccessPoint(accessPoint);
//
//		SCSMServerSAP sap = new MMSServerSAP(MMS_PORT, 0, null, accessPoint);
//		System.setProperty("iecServerConfigFileName",
//				"/home/cdo/openiec61850/openiec61850-demo/config/simple/iec61850server.properties");
//		server.addSCSMSAP(sap);
//		server.run();
//	}
//
//	protected static void setupSCLParser() throws Exception {
//		scl = new ICDParser("icds/sampleBiogasPlant.icd");
//		Assert.assertNotNull(scl);
//	}
//
//	protected static void setupAccessPoint() throws Exception {
//		Vector<AccessPoint> aps = scl.getAccessPoints();
//		Assert.assertEquals(1, aps.size());
//		accessPoint = aps.elementAt(0);
//		Assert.assertNotNull(accessPoint);
//	}
//
//	protected static void setupModel() throws Exception {
//		// need to establish model on the client side
//		List<String> myLDs = client.getServerDirectory(ServerDirectoryType.LOGICAL_DEVICES);
//
//		List<String> lnRefs = null;
//		for (String myLD : myLDs) {
//			lnRefs = client.getLogicalDeviceDirectory(myLD);
//
//			List<String> doNames = null;
//			for (String lnRef : lnRefs) {
//				doNames = client.getLogicalNodeDirectory(lnRef, ACSIClass.DATA_OBJECT);
//				for (String doName : doNames) {//
//					client.getDataDefinition(lnRef + "." + doName);
//				}
//			}
//
//		}
//	}
//
//	@Test
//	public void testGetLogicalNodeDirectory() throws ServiceError {
//		List<String> dataSetDirectory = client.getLogicalNodeDirectory("IEDBiogasCHP/MMXU1", ACSIClass.DATA_SET);
//		Assert.assertNotNull(dataSetDirectory);
//		Assert.assertEquals(1, dataSetDirectory.size());
//		Assert.assertThat(dataSetDirectory, IsCollectionContaining.hasItem("DS_MMXU"));
//
//	}
//
//	@Test
//	public void testGetDataSetDirectory() throws ServiceError {
//		// call getLogicalNodeDirectory first so it exist on the client side or
//		// else error is thrown
//		client.getLogicalNodeDirectory("IEDBiogasCHP/MMXU1", ACSIClass.DATA_SET);
//
//		List<String> dataSetMem = client.getDataSetDirectory("IEDBiogasCHP/MMXU1.DS_MMXU");
//		Assert.assertNotNull(dataSetMem);
//		Assert.assertEquals(17, dataSetMem.size());
//		Assert.assertThat(dataSetMem, IsCollectionContaining.hasItem("IEDBiogasCHP/MMXU1.TotPF"));
//		Assert.assertThat(dataSetMem, IsCollectionContaining.hasItem("IEDBiogasCHP/MMXU1.Mod"));
//	}
//
//	// TODO @Test
//	public void testGetDataSetValues() throws ServiceError {
//		client.getLogicalNodeDirectory("IEDBiogasCHP/MMXU1", ACSIClass.DATA_SET);
//		client.getDataSetDirectory("IEDBiogasCHP/MMXU1.DS_MMXU");
//
//		List<IModelNode> dataSetValues = client.getDataSetValues("IEDBiogasCHP/MMXU1.DS_MMXU");
//		Assert.assertNotNull(dataSetValues);
//		Assert.assertEquals(17, dataSetValues.size());
//
//		for (IModelNode mem : dataSetValues) {
//			Assert.assertTrue(mem.hasChildren());
//			// test some values to see if they are present
//			if (mem.getReference().toString().equals("IEDBiogasCHP/MMXU1.TotW")) {
//				IModelNode child = mem.getChild("instMag");
//				Assert.assertNotNull(child);
//				Assert.assertTrue(child.hasChildren());
//			}
//
//			if (mem.getReference().toString().equals("IEDBiogasCHP/MMXU1.TotVAr")) {
//				IModelNode child = mem.getChild("range");
//				Assert.assertNotNull(child);
//				Assert.assertEquals(FunctionalConstraint.MX, child.getFunctionalConstraint());
//			}
//
//			// other tests?
//		}
//	}
//
//	@Test
//	public void testCreateDataSet() throws ServiceError {
//		List<String> dsMembers = new LinkedList<String>();
//		List<FunctionalConstraint> fc = new LinkedList<FunctionalConstraint>();
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.TotW");
//		fc.add(FunctionalConstraint.EX);
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.Hz");
//		fc.add(FunctionalConstraint.CF);
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.TotVA");
//		fc.add(FunctionalConstraint.EX);
//
//		// for persistent data set
//		client.createDataSet("IEDBiogasCHP/MMXU1.dsTest1", dsMembers, fc);
//		// 20.07.2011: test does not work with multi level nodes such as
//		// PPV.phsAB
//
//		List<String> dataSetMem = client.getDataSetDirectory("IEDBiogasCHP/MMXU1.dsTest1");
//		Assert.assertNotNull(dataSetMem);
//		Assert.assertEquals(3, dataSetMem.size());
//		Assert.assertThat(dataSetMem, IsCollectionContaining.hasItem("IEDBiogasCHP/MMXU1.TotVA"));
//
//		// List<IModelNode> dataSetValues =
//		// client.getDataSetValues("IEDBiogasCHP/MMXU1.dsTest1");
//		// Assert.assertNotNull(dataSetValues);
//		// Assert.assertEquals(3, dataSetValues.size());
//		// for(IModelNode mem : dataSetValues){
//		// Assert.assertTrue(mem.hasChildren());
//		// if(mem.getReference().toString().equals("IEDBiogasCHP/MMXU1.TotW")){
//		// IModelNode child = mem.getChild("cdcNs");
//		// Assert.assertNotNull(child);
//		// Assert.assertEquals(FunctionalConstraint.EX,
//		// child.getFunctionalConstraint());
//		// Assert.assertEquals(BasicTypeEnum.VISIBLE_STRING_255,
//		// child.getBasicType());
//		// }
//		//
//		// if(mem.getReference().toString().equals("IEDBiogasCHP/MMXU1.Hz")){
//		// IModelNode child = mem.getChild("sVC");
//		// Assert.assertNotNull(child);
//		// Assert.assertEquals(FunctionalConstraint.CF,
//		// child.getFunctionalConstraint());
//		// Assert.assertTrue(child.hasChildren());
//		// }
//		// }
//
//		// try for non persistent data set
//		client.createDataSet("@dsTest2", dsMembers, fc);
//		// 20.07.2011: test does not work with multi-level nodes such as
//		// PPV.phsAB
//
//		List<String> dataSetMem2 = client.getDataSetDirectory("@dsTest2");
//		Assert.assertNotNull(dataSetMem2);
//		Assert.assertEquals(3, dataSetMem2.size());
//		Assert.assertThat(dataSetMem2, IsCollectionContaining.hasItem("IEDBiogasCHP/MMXU1.TotVA"));
//
//		// List<IModelNode> dataSetValues2 =
//		// client.getDataSetValues("@dsTest2");
//		// Assert.assertNotNull(dataSetValues2);
//		// Assert.assertEquals(3, dataSetValues2.size());
//		// for(IModelNode mem : dataSetValues2){
//		// Assert.assertTrue(mem.hasChildren());
//		// if(mem.getReference().toString().equals("IEDBiogasCHP/MMXU1.TotW")){
//		// IModelNode child = mem.getChild("cdcNs");
//		// Assert.assertNotNull(child);
//		// Assert.assertEquals(FunctionalConstraint.EX,
//		// child.getFunctionalConstraint());
//		// Assert.assertEquals(BasicTypeEnum.VISIBLE_STRING_255,
//		// child.getBasicType());
//		// }
//		//
//		// if(mem.getReference().toString().equals("IEDBiogasCHP/MMXU1.Hz")){
//		// IModelNode child = mem.getChild("sVC");
//		// Assert.assertNotNull(child);
//		// Assert.assertEquals(FunctionalConstraint.CF,
//		// child.getFunctionalConstraint());
//		// Assert.assertTrue(child.hasChildren());
//		// }
//		// }
//	}
//
//	@Test
//	public void testDeleteDataSet() throws ServiceError {
//		// create data sets that can be deleted
//		List<String> dsMembers = new LinkedList<String>();
//		List<FunctionalConstraint> fc = new LinkedList<FunctionalConstraint>();
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.TotW");
//		fc.add(FunctionalConstraint.EX);
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.Hz");
//		fc.add(FunctionalConstraint.CF);
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.TotVA");
//		fc.add(FunctionalConstraint.EX);
//
//		// for persistent data set
//		client.createDataSet("IEDBiogasCHP/MMXU1.dsTest2", dsMembers, fc);
//
//		List<String> dataSetDirectory = client.getLogicalNodeDirectory("IEDBiogasCHP/MMXU1", ACSIClass.DATA_SET);
//		Assert.assertEquals(3, dataSetDirectory.size());
//		Assert.assertThat(dataSetDirectory, IsCollectionContaining.hasItem("DS_MMXU"));
//		Assert.assertThat(dataSetDirectory, IsCollectionContaining.hasItem("dsTest1"));
//		// this was from the create data set test. Change to remove dependency?
//
//		Assert.assertThat(dataSetDirectory, IsCollectionContaining.hasItem("dsTest2"));
//
//		int[] deleteResult = client.deleteDataSet("IEDBiogasCHP/MMXU1.dsTest2");
//		Assert.assertEquals(1, deleteResult[0]);
//		Assert.assertEquals(1, deleteResult[1]);
//
//		List<String> dataSetDirectory2 = client.getLogicalNodeDirectory("IEDBiogasCHP/MMXU1", ACSIClass.DATA_SET);
//		Assert.assertEquals(2, dataSetDirectory2.size());
//		Assert.assertThat(dataSetDirectory, IsCollectionContaining.hasItem("DS_MMXU"));
//		Assert.assertThat(dataSetDirectory, IsCollectionContaining.hasItem("dsTest1"));
//	}
//
//	// TODO @Test
//	public void testSetDataSetValues() throws ServiceError {
//		List<String> dsMembers = new LinkedList<String>();
//		List<FunctionalConstraint> fc = new LinkedList<FunctionalConstraint>();
//
//		dsMembers.add("IEDBiogasCHP/MMXU1.TotW");
//		fc.add(FunctionalConstraint.EX);
//
//		// for persistent data set
//		client.createDataSet("IEDBiogasCHP/MMXU1.dsTest3", dsMembers, fc);
//
//		List<DataAttributeValue> setValues = new LinkedList<DataAttributeValue>();
//		for (int i = 0; i < 3; i++) {
//			DataAttributeValue value = new DataAttributeValue(new String("testing"), DAType.VISIBLE_STRING_255);
//			setValues.add(value);
//		}
//
//		List<String> writeResults = client.setDataSetValues("IEDBiogasCHP/MMXU1.dsTest3", setValues);
//		Assert.assertEquals("Success", writeResults.get(0));
//		// using current data factory, values are not being written
//
//	}
//
//	// @AfterClass
//	// public static void tearDown() throws Exception {
//	// client.close();
//	// server.stopServer();
//	// }
//
// }
