///**
// * 
// */
//package org.openiec61850.server.report.test;
//
//import org.openiec61850.common.model.DataContainer;
//import org.openiec61850.common.model.report.DataTriggerable;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.data.TriggeringDataSource;
//
//public class TriggeringDataSourceMock extends SimpleDataSourceMock implements TriggeringDataSource {
//
//	private static String lastTriggerId;
//	private static DataTriggerable lastTriggerable;
//
//	public void addTrigger(DataTriggerable triggerable, String triggerId, TriggerConditions trgOps,
//			DataContainer dataObject) {
//		setLastTriggerId(triggerId);
//		setLastTriggerable(triggerable);
//	}
//
//	private static void setLastTriggerId(String lastTriggerId) {
//		TriggeringDataSourceMock.lastTriggerId = lastTriggerId;
//	}
//
//	static String getLastTriggerId() {
//		return lastTriggerId;
//	}
//
//	private static void setLastTriggerable(DataTriggerable lastTriggerable) {
//		TriggeringDataSourceMock.lastTriggerable = lastTriggerable;
//	}
//
//	public static DataTriggerable getLastTriggerable() {
//		return lastTriggerable;
//	}
// }
