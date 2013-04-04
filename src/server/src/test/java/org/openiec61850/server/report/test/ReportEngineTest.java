//package org.openiec61850.server.report.test;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
//
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.openiec61850.common.ServiceError;
//import org.openiec61850.common.model.ConstructedDataAttribute;
//import org.openiec61850.common.model.DataContainer;
//import org.openiec61850.common.model.DataObject;
//import org.openiec61850.common.model.DataSet;
//import org.openiec61850.common.model.FunctionalConstraint;
//import org.openiec61850.common.model.LogicalDevice;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.Server;
//import org.openiec61850.common.model.basictypes.BasicType;
//import org.openiec61850.common.model.report.BufferedReportControlBlock;
//import org.openiec61850.common.model.report.Report;
//import org.openiec61850.common.model.report.ReportControlBlock;
//import org.openiec61850.common.model.report.ReportEntryData;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.data.ConfigurationException;
//import org.openiec61850.server.data.DataSourceFactory;
//import org.openiec61850.server.report.ReportEngine;
//import org.openiec61850.server.scsm.SCSMConnectionHandler;
//
//public class ReportEngineTest {
//
//	private final class ClientConnectionMock implements SCSMConnectionHandler {
//		private final List<Report> reports = new ArrayList<Report>();
//
//		public void sendReport(Report report) {
//			reports.add(report);
//		}
//
//		public void close() {
//		}
//
//		public String getClientId() {
//			return "testclient";
//		}
//	}
//
//	@BeforeClass
//	public static void setup() throws ConfigurationException {
//		Properties props = new Properties();
//		props.setProperty("source.testAccessPoint.testDevice/testLogicalNode.testDO1.class",
//				TriggeringDataSourceMock.class.getName());
//		props.setProperty("source.testAccessPoint.testDevice/testLogicalNode.testDO2.class",
//				SimpleDataSourceMock.class.getName());
//		DataSourceFactory.theFactory().setConfig(props);
//	}
//
//	@Test
//	public void testTriggeringDataSource() throws InterruptedException, ServiceError {
//		Server server = new Server();
//		AccessPoint ap = new AccessPoint("testAccessPoint", server);
//		LogicalDevice logicalDevice = new LogicalDevice("testDevice");
//		server.addDevice(logicalDevice);
//		LogicalNode logicalNode = createLogicalNode(logicalDevice, TriggerConditions.DATA_CHANGE,
//				new ReportControlBlock());
//		ReportEngine reportEngine = new ReportEngine(Collections.singletonList(ap));
//		reportEngine.run();
//		ClientConnectionMock client = new ClientConnectionMock();
//		reportEngine.registerClient(client, "testReportId");
//		Assert.assertEquals("testReportId", TriggeringDataSourceMock.getLastTriggerId());
//		Assert.assertEquals(reportEngine, TriggeringDataSourceMock.getLastTriggerable());
//
//		DataContainer dataObject1 = logicalNode.getDO("testDO1");
//		TriggeringDataSourceMock.getLastTriggerable().dataTrigger("popel", TriggerConditions.DATA_CHANGE, dataObject1);
//		Assert.assertEquals(0, client.reports.size());
//
//		TriggeringDataSourceMock.getLastTriggerable().dataTrigger("testReportId", TriggerConditions.DATA_CHANGE,
//				dataObject1);
//		Assert.assertEquals(1, client.reports.size());
//		Report report = client.reports.get(0);
//		Assert.assertEquals("testDevice/testLogicalNode.dataSet", report.getDataSet());
//		Assert.assertEquals("testReportId", report.getRptId());
//		Assert.assertEquals(1, report.getEntryData().size());
//		ReportEntryData reportEntry = report.getEntryData().get(0);
//		Assert.assertEquals(88, reportEntry.getValue());
//		Assert.assertEquals("testDevice/testLogicalNode.testDO1.testDA1", reportEntry.getDataRef());
//		Assert.assertEquals(true, reportEntry.getReasonCode().isDataChange());
//	}
//
//	@Test
//	public void testSimpleDataSource() throws InterruptedException, ServiceError {
//		Server server = new Server();
//		AccessPoint ap = new AccessPoint("testAccessPoint", server);
//		LogicalDevice logicalDevice = new LogicalDevice("testDevice");
//		server.addDevice(logicalDevice);
//		createLogicalNode(logicalDevice, TriggerConditions.DATA_CHANGE, new ReportControlBlock());
//		ReportEngine reportEngine = new ReportEngine(Collections.singletonList(ap));
//		reportEngine.run();
//		ClientConnectionMock client = new ClientConnectionMock();
//		reportEngine.registerClient(client, "testReportId");
//
//		Thread.sleep(1200);
//		SimpleDataSourceMock.setValueToSend(77);
//		Thread.sleep(1200);
//
//		Assert.assertEquals(1, client.reports.size());
//		Report report = client.reports.get(0);
//		Assert.assertEquals("testDevice/testLogicalNode.dataSet", report.getDataSet());
//		Assert.assertEquals("testReportId", report.getRptId());
//		Assert.assertEquals(1, report.getEntryData().size());
//		ReportEntryData reportEntry = report.getEntryData().get(0);
//		Assert.assertEquals(77, reportEntry.getValue());
//		Assert.assertEquals("testDevice/testLogicalNode.testDO2.testDA2", reportEntry.getDataRef());
//		Assert.assertEquals(true, reportEntry.getReasonCode().isDataChange());
//		reportEngine.deregisterClient(client, "testReportId");
//	}
//
//	@Test
//	public void testIntegrityReport() throws InterruptedException, ServiceError {
//		Server server = new Server();
//		AccessPoint ap = new AccessPoint("testAccessPoint", server);
//		LogicalDevice logicalDevice = new LogicalDevice("testDevice");
//		server.addDevice(logicalDevice);
//		LogicalNode logicalNode = createLogicalNode(logicalDevice, TriggerConditions.INTEGRITY,
//				new ReportControlBlock());
//		ReportEngine reportEngine = new ReportEngine(Collections.singletonList(ap));
//		reportEngine.run();
//		ClientConnectionMock client = new ClientConnectionMock();
//		reportEngine.registerClient(client, "testReportId");
//
//		Thread.sleep(1200);
//		Assert.assertEquals(0, client.reports.size());
//		SimpleDataSourceMock.setValueToSend(55);
//		logicalNode.getReportControlBlocks().get(0).setIntgPd(2000L);
//		Thread.sleep(1200);
//		Assert.assertEquals(1, client.reports.size());
//
//		Report report = client.reports.get(0);
//		Assert.assertEquals("testDevice/testLogicalNode.dataSet", report.getDataSet());
//		Assert.assertEquals("testReportId", report.getRptId());
//		Assert.assertEquals(2, report.getEntryData().size());
//		ReportEntryData reportEntry = report.getEntryData().get(1);
//		Assert.assertEquals(55, reportEntry.getValue());
//		Assert.assertEquals("testDevice/testLogicalNode.testDO2.testDA2", reportEntry.getDataRef());
//		Assert.assertEquals(false, reportEntry.getReasonCode().isDataChange());
//		Assert.assertEquals(true, reportEntry.getReasonCode().isIntegrity());
//		reportEngine.deregisterClient(client, "testReportId");
//	}
//
//	/**
//	 * In this case the client registers AFTER some reports have been generated.
//	 * It is expected that the client receives these reports from the buffer.
//	 * 
//	 * @throws ServiceError
//	 */
//	@Test
//	public void testBufferedReporting() throws ServiceError {
//		Server server = new Server();
//		AccessPoint ap = new AccessPoint("testAccessPoint", server);
//		LogicalDevice logicalDevice = new LogicalDevice("testDevice");
//		server.addDevice(logicalDevice);
//		LogicalNode logicalNode = createLogicalNode(logicalDevice, TriggerConditions.DATA_CHANGE,
//				new BufferedReportControlBlock());
//		ReportEngine reportEngine = new ReportEngine(Collections.singletonList(ap));
//		reportEngine.run();
//
//		DataContainer dataObject1 = logicalNode.getDO("testDO1");
//		((ConstructedDataAttribute) dataObject1.getChild("testDA1")).getValue().setValue(37);
//		TriggeringDataSourceMock.getLastTriggerable().dataTrigger("testReportId", TriggerConditions.DATA_CHANGE,
//				dataObject1);
//
//		ClientConnectionMock client = new ClientConnectionMock();
//		reportEngine.registerClient(client, "testReportId");
//		Assert.assertEquals(1, client.reports.size());
//		Report report = client.reports.get(0);
//		Assert.assertEquals("testDevice/testLogicalNode.dataSet", report.getDataSet());
//		Assert.assertEquals("testReportId", report.getRptId());
//		Assert.assertEquals(1, report.getEntryData().size());
//		ReportEntryData reportEntry = report.getEntryData().get(0);
//		Assert.assertEquals(37, reportEntry.getValue());
//		Assert.assertEquals("testDevice/testLogicalNode.testDO1.testDA1", reportEntry.getDataRef());
//		Assert.assertEquals(true, reportEntry.getReasonCode().isDataChange());
//	}
//
//	private LogicalNode createLogicalNode(LogicalDevice logicalDevice, TriggerConditions trgOps,
//			ReportControlBlock reportControlBlock) {
//		LogicalNode logicalNode = new LogicalNode();
//		logicalNode.setReference(logicalDevice.getReference().toString(), "testLogicalNode");
//		logicalDevice.addLogicalNode(logicalNode);
//		DataSet dataSet = new DataSet();
//		dataSet.setReference(logicalNode.getReference().toString(), "dataSet");
//		logicalNode.addDataSet(dataSet);
//		ReportControlBlock rcb = reportControlBlock;
//		rcb.setRptID("testReportId");
//		rcb.setDataSet(dataSet);
//		rcb.setTrgOps(trgOps);
//		logicalNode.addReportControlBlock(rcb);
//		createDataObject(logicalNode, logicalNode.getDataSet("dataSet"), "testDO1", "testDA1");
//		createDataObject(logicalNode, logicalNode.getDataSet("dataSet"), "testDO2", "testDA2");
//		return logicalNode;
//	}
//
//	private DataObject createDataObject(LogicalNode logicalNode, DataSet dataSet, String name, String attributeName) {
//		DataObject dataObject1 = new DataObject(logicalNode.getReference().toString(), name);
//		logicalNode.addDataObject(dataObject1);
//		dataSet.addDataObject(dataObject1);
//		ConstructedDataAttribute dataAttribute = new ConstructedDataAttribute(dataObject1.getReference().toString(), attributeName,
//				FunctionalConstraint.MX);
//		dataObject1.addDataAttribute(dataAttribute);
//		DataAttributeValue dataAttributeValue = new DataAttributeValue(88, BasicType.INT16);
//		dataAttribute.setValue(dataAttributeValue);
//		return dataObject1;
//	}
//
// }