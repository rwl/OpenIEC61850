//package org.openiec61850.scsm;
//
//import java.util.List;
//import java.util.Vector;
//
//import org.openiec61850.client.ClientACSI;
//import org.openiec61850.client.ClientACSI.ServerDirectoryType;
//import org.openiec61850.common.ACSIClass;
//import org.openiec61850.common.ServiceError;
//import org.openiec61850.common.model.ConstructedDataAttribute;
//import org.openiec61850.common.model.FunctionalConstraint;
//<<<<<<< HEAD
//import org.openiec61850.common.model.IModelNode;
//import org.openiec61850.common.model.datypes.DAType;
//=======
//import org.openiec61850.common.model.basictypes.BasicType;
//>>>>>>> reportingLogging
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.IED;
//import org.openiec61850.server.scl.ICDParser;
//
///**
// * @author Michael Zillgith
// * @author Tobias Weidelt
// */
//public abstract class SCSMIntegrationTest {
//	protected static ICDParser scl;
//	protected static AccessPoint accessPoint;
//	protected static IED server;
//	protected static ClientACSI client;
//
//	@BeforeClass
//	public static void setUpClass() throws Exception {
//		setupSCLParser();
//		setupAccessPoint();
//	}
//
//	protected static void setupSCLParser() throws Exception {
//		scl = new ICDParser("icds/sampleSCLFile.icd");
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
//	@Test
//	public void testGetServerDirectory() throws ServiceError {
//		List<String> directory = client.getServerDirectory(ServerDirectoryType.LOGICAL_DEVICES);
//
//		Assert.assertEquals(1, directory.size());
//		Assert.assertThat(directory, IsCollectionContaining.hasItem("SampleIEDDevice1"));
//	}
//
//	@Test
//	public void testGetLogicalDeviceDirectory() throws ServiceError {
//		List<String> directory = null;
//
//		directory = client.getLogicalDeviceDirectory("SampleIEDDevice1");
//		Assert.assertNotNull(directory);
//		Assert.assertEquals(6, directory.size());
//
//		/* Check if all items start with "BHKW/" */
//		for (String lnode : directory) {
//			Assert.assertTrue(lnode.startsWith("SampleIEDDevice1/"));
//		}
//		// Assert.assertThat(directory,
//		// IsCollectionContaining.hasItem("SampleIEDDevice1/MMXU1"));
//		Assert.assertThat(directory, IsCollectionContaining.hasItem("SampleIEDDevice1/LLN0"));
//
//		/* Check if ServiceError is thrown if wrong Logical Device name is used */
//		try {
//			directory = client.getLogicalDeviceDirectory("NonExistingLD");
//			Assert.fail("ServiceError expected.");
//		} catch (ServiceError e) {
//			try {
//				Assert.assertEquals(ServiceError.INSTANCE_NOT_AVAILABLE, e.getErrorCode());
//			} catch (AssertionError ae) {
//				e.printStackTrace();
//				throw e;
//			}
//		}
//	}
//
//	@Test
//	public void testGetLogicalNodeDirectory() throws ServiceError {
//		List<String> directory = client.getLogicalNodeDirectory("SampleIEDDevice1/MMXU2", ACSIClass.DATA_OBJECT);
//		Assert.assertNotNull(directory);
//		Assert.assertEquals(5, directory.size());
//		Assert.assertThat(directory, IsCollectionContaining.hasItem("TotW"));
//		directory = client.getLogicalNodeDirectory("SampleIEDDevice1/LLN0", ACSIClass.DATA_OBJECT);
//		Assert.assertNotNull(directory);
//		Assert.assertEquals(4, directory.size());
//		Assert.assertThat(directory, IsCollectionContaining.hasItem("Mod"));
//	}
//
//	@Test
//	public void testGetDataDirectory() throws ServiceError {
//		List<String> directory = client.getDataDirectory("SampleIEDDevice1/MMXU2.TotW");
//		Assert.assertNotNull(directory);
//		Assert.assertThat(directory, IsCollectionContaining.hasItem("mag"));
//		// Assert.assertThat(directory,
//		// IsCollectionContaining.hasItem("instMag"));
//
//		// directory =
//		// client.getDataDirectory("SampleIEDDevice1/MMXU2.PPV.phsAB");
//		// Assert.assertEquals(22, directory.size());
//		// directory.size() liefert momentan 3, sollte aber 22 sein. Problem
//		// tritt nur im LN MMXU1 auf, dort aber in vielen SDOs, z.B.
//		// MMXU1.PPV.phsAB, MMXU1.PPV.phsBC, MMXU1.PhV.phsA, MMXU1.PhV.neut
//	}
//
//	@Test
//	public void testGetDataDefinition() throws ServiceError {
//		String doRef = "SampleIEDDevice1/LLN0.Mod";
//		IModelNode dd = client.getDataDefinition(doRef);
//
//		Assert.assertNotNull(dd);
//		Assert.assertTrue(dd.hasChildren());
//		Assert.assertEquals(doRef, dd.getReference().toString());
//
//		Assert.assertNotNull(dd.getChild("t"));
//		// Assert.assertSame(Array.class, dd.getChild("time").getClass());
//		Assert.assertSame(ConstructedDataAttribute.class, dd.getChild("t").getClass());
//		// Assert.assertEquals(96, ((Array) dd.getChild("time")).size());
//	}
//
//	@Test
//	public void testGetDataValues() throws ServiceError {
//		// Je nach DataSource kann die folgende Zeile zu einer Exception
//		// führen (ServiceError/DataAccessError)
//
//		// DataContainer container =
//		// client.getDataValues("IEDBiogasCHP/MMXU1.TotW.instMag.f",
//		// FunctionalConstraint.MX);
//		// Assert.assertNotNull(container);
//
//		// Assert.assertEquals(true, container.isLeaf()); //?
//
//		DataContainer container = client.getDataValues("SampleIEDDevice1/MMXU2.TotW.mag", null);
//		Assert.assertNotNull(container);
//		Assert.assertEquals(1, container.getChildren().size());
//
//		container = client.getDataValues("SampleIEDDevice1/MMXU2.TotW", FunctionalConstraint.MX);
//		Assert.assertNotNull(container);
//		Assert.assertEquals(3, container.getChildren().size());
//
//		// container = client.getDataValues("SampleIEDDevice1/DSCH1.SchdAbsTm",
//		// null);
//		// Assert.assertNotNull(container);
//		// Assert.assertEquals(9, container.getSubContainers().size());
//
//		// Array valArray = (Array)
//		// container.findChild("SampleIEDDevice1/DSCH1.SchdAbsTm.val");
//		// Assert.assertEquals(96, valArray.size());
//		//
//		// Array timeArray = (Array)
//		// container.findChild("SampleIEDDevice1/DSCH1.SchdAbsTm.time");
//		// Assert.assertEquals(96, timeArray.size());
//		//
//		// List<String> refs = new LinkedList<String>();
//		// for (DataContainer da : container.recursive()) {
//		// refs.add(da.getReference().toString());
//		// System.out.println(da.toString());
//		// }
//		//
//		// Assert.assertTrue(refs.contains("SampleIEDDevice1/DSCH1.SchdAbsTm.numPts"));
//	}
//
//	@Test(expected = ServiceError.class)
//	public void testGetDataValuesWithInvalidReference() throws ServiceError {
//		try {
//			client.getDataValues("InvalidLD/InvalidLN.InvalidDO", null);
//		} catch (ServiceError e) {
//			Assert.assertEquals(ServiceError.INSTANCE_NOT_AVAILABLE, e.getErrorCode());
//			throw e;
//		}
//	}
//
//	// TODO @Test
//	public void testSetDataValues() throws ServiceError {
//		String dataRef = "IEDBiogasCHP/MMXU1.TotW.instMag.f";
//		client.setDataValue(dataRef, new DataAttributeValue(123.456f, DAType.FLOAT32));
//
//		DataAttributeValue value = client.getDataValue(dataRef, null);
//		Assert.assertNotNull(value);
//
//		Assert.assertTrue((Float) value.getValue() > 123f);
//		Assert.assertTrue((Float) value.getValue() < 124f);
//	}
//
//	@AfterClass
//	public static void tearDown() throws Exception {
//		client.close();
//		server.stopServer();
//	}
// }
