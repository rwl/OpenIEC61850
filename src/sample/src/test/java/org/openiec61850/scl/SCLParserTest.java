//package org.openiec61850.scl;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.Vector;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.openiec61850.common.model.Array;
//import org.openiec61850.common.model.DataSet;
//import org.openiec61850.common.model.LogicalDevice;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.Server;
//import org.openiec61850.common.model.report.ReportControlBlock;
//import org.openiec61850.common.model.report.UnbufferedReportContrlBlock;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.scl.ICDParser;
//import org.openiec61850.server.scl.SCLParserException;
//import org.xml.sax.SAXException;
//
//
//public class SCLParserTest {
//
//	@Test
//	public void testSCLParser() throws ParserConfigurationException, SAXException, IOException, SCLParserException {
//
//		ICDParser parser = new ICDParser("icds/sampleBiogasPlant.icd");
//
//		// Prüfen, ob der SCLParser den Dateiinhalt richtig gelesen hat
//		Vector<AccessPoint> accessPoints = parser.getAccessPoints();
//		Assert.assertNotNull(accessPoints);
//		Assert.assertEquals(1, accessPoints.size());
//
//		AccessPoint accessPoint = accessPoints.get(0);
//		Assert.assertEquals("AccessPoint1", accessPoint.getName());
//		Assert.assertNotNull(accessPoint.getServer());
//
//		Server server = accessPoint.getServer();
//		Assert.assertNotNull(server.getLDevices());
//		Assert.assertEquals(1, server.getLDevices().size());
//
//		LogicalDevice lDevice = server.getLDevices().iterator().next();
//		Assert.assertNotNull(lDevice);
//		Assert.assertEquals("IEDBiogasCHP", lDevice.getNodeName());
//		Assert.assertEquals("IEDBiogasCHP", lDevice.getReference().toString());
//		Assert.assertEquals(7, lDevice.getLNodes().size());
//
//		LogicalNode lnodeDgen = lDevice.getLNodes().get(3);
//		// Assert.assertEquals("DGEN", lnodeDgen.getLnClass());
//		Assert.assertEquals("DGEN1", lnodeDgen.getNodeName());
//		// Assert.assertEquals("DGEN_TYPE", lnodeDgen.getLnType());
//		Assert.assertEquals(1, lnodeDgen.getDataSets().size());
//
//		DataSet dataset = lnodeDgen.getDataSets().get(0);
//		Assert.assertEquals(16, dataset.getDataObjects().size());
//
//		DataContainer dataObject = dataset.getDataObjects().get(0);
//		Assert.assertEquals("TotWh", dataObject.getNodeName());
//		Assert.assertEquals(1, lnodeDgen.getReportControlBlocks().size());
//
//		ReportControlBlock rcb = lnodeDgen.getReportControlBlocks().get(0);
//		Assert.assertTrue(rcb instanceof UnbufferedReportContrlBlock);
//		Assert.assertNotNull(rcb.getDataSet());
//		Assert.assertEquals("DS_DGEN", rcb.getDataSet().getNodeName());
//		Assert.assertEquals(5000L, rcb.getIntgPd());
//		Assert.assertEquals("IEDBiogasCHP/DGEN1$ucrb", rcb.getRptID());
//		Assert.assertEquals(1L, rcb.getConvRef());
//		Assert.assertEquals(1000L, rcb.getBufTm());
//		Assert.assertNotNull(rcb.getTrgOps());
//		Assert.assertTrue(rcb.getTrgOps().isDataChange());
//		Assert.assertFalse(rcb.getTrgOps().isIntegrity());
//		// ... TODO: viele weitere Asserts ...
//
//		Array valArray = (Array) server.getChild("IEDBiogasCHP").getChild("DSCH1").getChild("SchdAbsTm")
//				.getChild("val");
//		Assert.assertEquals(96, valArray.size());
//
//		Array valArray2 = (Array) server.findChild("IEDBiogasCHP/DSCH1.SchdAbsTm.time");
//		Assert.assertEquals(96, valArray2.size());
//		System.out.println(valArray2.getChildren().toString());
//	}
//
//	/**
//	 * Dieser Test erwartet, dass eine {@link FileNotFoundException} fliegt und
//	 * ist erfolgreich wenn genau das passiert.
//	 */
//	@Test(expected = FileNotFoundException.class)
//	public void testInvalidSCLFile() throws ParserConfigurationException, SAXException, IOException, SCLParserException {
//		new ICDParser("Diese_Datei_gibt_es_nicht.scl");
//	}
// }
