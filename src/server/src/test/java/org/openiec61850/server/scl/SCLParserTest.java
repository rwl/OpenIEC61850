//package org.openiec61850.server.scl;
//
//import java.io.IOException;
//import java.util.List;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.junit.Test;
//import org.openiec61850.common.model.DataObject;
//import org.openiec61850.common.model.LogicalDevice;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.ModelNode;
//import org.openiec61850.common.model.Server;
//import org.openiec61850.server.AccessPoint;
//import org.xml.sax.SAXException;
//
//public class SCLParserTest {
//
//	@Test
//	public void testParser() throws ParserConfigurationException, SAXException, IOException, SCLParserException {
//		String sclFileName = "/home/stfe/master/openiec61850/openiec61850-demo/icds/sampleBiogasPlant.icd";
//
//		ICDParser parser = new ICDParser(sclFileName);
//
//		List<AccessPoint> aps = parser.getAccessPoints();
//
//		AccessPoint ap = aps.get(0);
//		Server server = ap.getServer();
//
//		for (ModelNode mn : server) {
//			System.out.println(mn.getReference());
//		}
//		LogicalDevice ld = (LogicalDevice) server.getChild("IEDBiogasCHP");
//		for (ModelNode mn : ld) {
//			System.out.println(mn.getReference());
//		}
//
//		LogicalNode ln = (LogicalNode) ld.getChild("T1PTOC0");
//
//		for (ModelNode dataObject : ln) {
//			System.out.println(dataObject.getReference());
//		}
//
//		DataObject dataObject = (DataObject) ln.getChild("TmASt");
//
//		for (ModelNode node : dataObject) {
//			System.out.println(node.getReference());
//		}
//
//		ModelNode subDataObject = dataObject.getChild("crvPts");
//
//		for (ModelNode node : subDataObject) {
//			System.out.println(node.getReference());
//			System.out.println(node.getReference().getName());
//		}
//
//		// for (ModelNode da : dataObject.recursive()) {
//		// System.out.println(da.getReference());
//		// }
//
//	}
// }