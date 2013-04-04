//package org.openiec61850.server.log.test;
//
//import java.util.Collections;
//import java.util.Date;
//import java.util.Properties;
//
//import junit.framework.Assert;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.openiec61850.common.model.ConstructedDataAttribute;
//import org.openiec61850.common.model.DataObject;
//import org.openiec61850.common.model.DataSet;
//import org.openiec61850.common.model.LogicalDevice;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.ObjectReference;
//import org.openiec61850.common.model.Server;
//import org.openiec61850.common.model.basictypes.BasicType;
//import org.openiec61850.common.model.basictypes.Timestamp;
//import org.openiec61850.common.model.log.Log;
//import org.openiec61850.common.model.log.LogControlBlock;
//import org.openiec61850.common.model.log.LogEntry;
//import org.openiec61850.common.model.log.LogEntryData;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.data.ConfigurationException;
//import org.openiec61850.server.data.DataSourceFactory;
//import org.openiec61850.server.log.DataStorageFactory;
//import org.openiec61850.server.log.InMemoryDataStorage;
//import org.openiec61850.server.log.LogEngine;
//import org.openiec61850.server.report.test.SimpleDataSourceMock;
//import org.openiec61850.server.report.test.TriggeringDataSourceMock;
//
//public class LogEngineTest {
//
//	@BeforeClass
//	public static void setup() throws ConfigurationException {
//		Properties props = new Properties();
//		props.setProperty("source.testAccessPoint.testDevice/testLogicalNode.testDO1.class",
//				TriggeringDataSourceMock.class.getName());
//		props.setProperty("source.testAccessPoint.testDevice/testLogicalNode.testDO2.class",
//				SimpleDataSourceMock.class.getName());
//		DataSourceFactory.theFactory().setConfig(props);
//		Properties storageProps = new Properties();
//		storageProps.setProperty("class", InMemoryDataStorage.class.getName());
//		DataStorageFactory.theFactory().setConfig(storageProps);
//	}
//
//	@Test
//	public void testLogEngine() throws ConfigurationException {
//		Server server = new Server();
//		AccessPoint ap = new AccessPoint("testAccessPoint", server);
//		LogicalDevice logicalDevice = new LogicalDevice("testDevice");
//		server.addDevice(logicalDevice);
//		LogicalNode logicalNode = createLogicalNode(logicalDevice, TriggerConditions.DATA_CHANGE);
//		LogEngine logEngine = new LogEngine(Collections.singletonList(ap));
//		logEngine.run();
//		TriggeringDataSourceMock.getLastTriggerable().dataTrigger("testLog", TriggerConditions.DATA_CHANGE,
//				logicalNode.getDO("testDO1"));
//		Log log = logEngine.queryLogByTime(new ObjectReference("testDevice/testLogicalNode.testLog"), new Timestamp(
//				new Date(new Date().getTime() - 10000L)), new Timestamp(new Date(new Date().getTime() + 10000L)));
//		Assert.assertNotNull(log);
//		Assert.assertEquals("testDevice/testLogicalNode.testLog", log.getLogRef().toString());
//		Assert.assertEquals(1, log.getEntries().size());
//		LogEntry logEntry = log.getEntries().get(0);
//		Assert.assertEquals(1, logEntry.getEntryData().size());
//		LogEntryData led = logEntry.getEntryData().get(0);
//		Assert.assertEquals("testDevice/testLogicalNode.testDO1.testDA1", led.getDataRef());
//		Assert.assertTrue(led.getReasonCode().isDataChange());
//		Assert.assertEquals(88, led.getValue());
//	}
//
//	private LogicalNode createLogicalNode(LogicalDevice logicalDevice, TriggerConditions trgOps) {
//		LogicalNode logicalNode = new LogicalNode();
//		logicalNode.setReference(logicalDevice.getReference().toString(), "testLogicalNode");
//		logicalDevice.addLogicalNode(logicalNode);
//		DataSet dataSet = new DataSet();
//		dataSet.setReference(logicalNode.getReference().toString(), "dataSet");
//		logicalNode.addDataSet(dataSet);
//		LogControlBlock lcb = new LogControlBlock();
//		lcb.setDataSet(dataSet);
//		lcb.setLogRef(new ObjectReference(logicalNode.getReference() + "." + "testLog"));
//		lcb.setName("testLog");
//		lcb.setTrgOps(trgOps);
//		logicalNode.addLogControlBlock(lcb);
//		createDataObject(logicalNode, logicalNode.getDataSet("dataSet"), "testDO1", "testDA1");
//		createDataObject(logicalNode, logicalNode.getDataSet("dataSet"), "testDO2", "testDA2");
//		return logicalNode;
//	}
//
//	private DataObject createDataObject(LogicalNode logicalNode, DataSet dataSet, String name, String attributeName) {
//		DataObject dataObject1 = new DataObject(logicalNode.getReference().toString(), name);
//		logicalNode.addDataObject(dataObject1);
//		dataSet.addDataObject(dataObject1);
//		ConstructedDataAttribute dataAttribute = new ConstructedDataAttribute(dataObject1.getReference().toString(), attributeName, null);
//		dataObject1.addDataAttribute(dataAttribute);
//		DataAttributeValue dataAttributeValue = new DataAttributeValue(88, BasicType.INT16);
//		dataAttribute.setValue(dataAttributeValue);
//		return dataObject1;
//	}
//
// }