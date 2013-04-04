///**
// * 
// */
//package org.openiec61850.server.report.test;
//
//import java.util.Properties;
//
//import org.openiec61850.common.ServiceError;
//import org.openiec61850.common.model.DataContainer;
//import org.openiec61850.common.model.FunctionalConstraint;
//import org.openiec61850.server.data.DataSource;
//
//public class SimpleDataSourceMock implements DataSource {
//	private static Object valueToSend = 88;
//
//	public void init(Properties sourceProps) {
//	}
//
//	public boolean readValues(DataContainer dataObject, FunctionalConstraint fc) throws ServiceError {
//		dataObject.recursive().iterator().next().getValue().setValue(getValueToSend());
//		return true;
//	}
//
//	public boolean writeValues(DataContainer dataObject, FunctionalConstraint fc) throws ServiceError {
//		return false;
//	}
//
//	public static void setValueToSend(Object valueToSend) {
//		SimpleDataSourceMock.valueToSend = valueToSend;
//	}
//
//	public static Object getValueToSend() {
//		return valueToSend;
//	}
// }
