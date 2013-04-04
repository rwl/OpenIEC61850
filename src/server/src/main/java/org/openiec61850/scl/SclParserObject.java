/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of openIEC61850.
 * For more information visit http://www.openmuc.org 
 *
 * openIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * openIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with openIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openiec61850.scl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openiec61850.AccessPoint;
import org.openiec61850.Array;
import org.openiec61850.BitStringType;
import org.openiec61850.ConstructedDataAttribute;
import org.openiec61850.DaBitString;
import org.openiec61850.DaBoolean;
import org.openiec61850.DaFloat32;
import org.openiec61850.DaFloat64;
import org.openiec61850.DaInt16;
import org.openiec61850.DaInt16u;
import org.openiec61850.DaInt32;
import org.openiec61850.DaInt32u;
import org.openiec61850.DaInt64;
import org.openiec61850.DaInt8;
import org.openiec61850.DaInt8u;
import org.openiec61850.DaOctetString;
import org.openiec61850.DaTimestamp;
import org.openiec61850.DaUnicodeString;
import org.openiec61850.DaVisibleString;
import org.openiec61850.DataSet;
import org.openiec61850.FcDataObject;
import org.openiec61850.FcModelNode;
import org.openiec61850.FunctionalConstraint;
import org.openiec61850.LogicalDevice;
import org.openiec61850.LogicalNode;
import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;
import org.openiec61850.ServerModel;
import org.openiec61850.common.model.report.BufferedReportControlBlock;
import org.openiec61850.common.model.report.OptFields;
import org.openiec61850.common.model.report.ReportControlBlock;
import org.openiec61850.common.model.report.TriggerConditions;
import org.openiec61850.common.model.report.UnbufferedReportContrlBlock;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class SclParserObject {

	private TypeDefinitions typeDefinitions;
	private Map<String, DataSet> dataSets = new HashMap<String, DataSet>();

	private String iedName;
	private List<AccessPoint> accessPoints = null;

	public SclParserObject() {
	}

	public List<AccessPoint> getAccessPoints() {
		return accessPoints;
	}

	public void parse(String icdFile) throws SclParseException {

		typeDefinitions = new TypeDefinitions();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new SclParseException(e);
		}

		Document doc;
		try {
			doc = builder.parse("file:" + new File(icdFile).getAbsolutePath());
		} catch (SAXException e) {
			throw new SclParseException(e);
		} catch (IOException e) {
			throw new SclParseException(e);
		}

		// read in DataTypeTemplates
		NodeList dttSections = doc.getElementsByTagName("DataTypeTemplates");

		if (dttSections.getLength() != 1) {
			throw new SclParseException("Only one DataTypeSection allowed");
		}

		Node dtt = dttSections.item(0);

		NodeList dataTypes = dtt.getChildNodes();

		for (int i = 0; i < dataTypes.getLength(); i++) {
			Node element = dataTypes.item(i);

			String nodeName = element.getNodeName();

			if (nodeName.equals("LNodeType")) {
				typeDefinitions.putLNodeType(new LnType(element));

			}
			else if (nodeName.equals("DOType")) {
				typeDefinitions.putDOType(new DoType(element));
			}
			else if (nodeName.equals("DAType")) {
				typeDefinitions.putDAType(new org.openiec61850.scl.DaType(element));
			}
			else if (nodeName.equals("EnumType")) {
				typeDefinitions.putEnumType(new EnumType(element));
			}
		}

		// read in the IED section
		NodeList iedList = doc.getElementsByTagName("IED");
		if (iedList == null || iedList.getLength() == 0) {
			throw new SclParseException("No IED section found!");
		}
		this.iedName = parseIEDName(iedList.item(0));

		// Create access points
		NodeList aps = doc.getElementsByTagName("AccessPoint");
		if (accessPoints == null) {
			accessPoints = new Vector<AccessPoint>(aps.getLength());
		}
		for (int i = 0; i < aps.getLength(); i++) {
			accessPoints.add(createAccessPoint(aps.item(i)));
		}

	}

	private AccessPoint createAccessPoint(Node iedServer) throws SclParseException {
		AccessPoint ap = null;

		NodeList elements = iedServer.getChildNodes();

		/* Create data models */
		for (int i = 0; i < elements.getLength(); i++) {
			Node element = elements.item(i);

			if (element.getNodeName().equals("Server")) {
				if (ap != null) {
					throw new SclParseException("AccessPoint has more then one server!");
				}
				ServerModel server = createServerModel(element);

				Node namedItem = iedServer.getAttributes().getNamedItem("name");
				if (namedItem == null) {
					throw new SclParseException("AccessPoint has no name attribute!");
				}
				String name = namedItem.getNodeValue();
				// TODO add ServicesSupported
				ap = new AccessPoint(name, server, null);
			}
		}

		if (ap == null) {
			throw new SclParseException("AccessPoint has no server!");
		}

		return ap;
	} /* createAccessPoint() */

	private ServerModel createServerModel(Node serverXMLNode) throws SclParseException {

		NodeList elements = serverXMLNode.getChildNodes();
		List<LogicalDevice> logicalDevices = new ArrayList<LogicalDevice>(elements.getLength());

		for (int i = 0; i < elements.getLength(); i++) {
			Node element = elements.item(i);

			if (element.getNodeName().equals("LDevice")) {
				logicalDevices.add(createNewLDevice(element));
			}
			else if (element.getNodeName().equals("Authentication")) {
				// TODO Authentication
				// System.out.println("TODO: Authentication");
			}
		}

		return new ServerModel(logicalDevices, dataSets.values());

	} /* createDataModel() */

	private LogicalDevice createNewLDevice(Node xmlNode) throws SclParseException {

		/* Parse attributes */

		String inst = null;
		String ldName = null;

		NamedNodeMap attributes = xmlNode.getAttributes();
		Node node;

		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("inst")) {
					inst = node.getNodeValue();
					// System.out.println("inst = " + inst);
				}
				else if (nodeName.equals("ldName")) {
					ldName = node.getNodeValue();
					// System.out.println("ldName = " + ldName);
				}
			}
		} // else System.out.println("createNewLDevice(): attributes = NULL!");

		if (inst == null) {
			throw new SclParseException("Required attribute \"inst\" not found!");
		}

		/* Parse Logical Node objects */
		NodeList elements = xmlNode.getChildNodes();
		Node element;

		List<LogicalNode> logicalNodes = new ArrayList<LogicalNode>();

		String ref;
		if ((ldName != null) && (ldName.length() != 0)) {
			ref = ldName;
		}
		else {
			ref = this.iedName + inst;
		}

		for (int i = 0; i < elements.getLength(); i++) {
			element = elements.item(i);

			if (element.getNodeName().equals("LN")) {
				// System.out.println("LN:");
				logicalNodes.add(createNewLogicalNode(element, ref));
			}
			else if (element.getNodeName().equals("LN0")) {
				// System.out.println("LN0:");
				logicalNodes.add(createNewLogicalNode(element, ref));
			}
			// else throw new SclParseException("Invalid tag name: " +
			// element.getNodeName());
		}

		LogicalDevice ldevice = new LogicalDevice(new ObjectReference(ref), logicalNodes);

		return ldevice;
	} /* createNewLDevice() */

	private LogicalNode createNewLogicalNode(Node lnXMLNode, String parentRef) throws SclParseException {

		/* Parse attributes */

		String inst = null;
		String lnClass = null;
		String lnType = null;

		// TODO desc useful?
		String desc = null;
		String prefix = "";

		NamedNodeMap attributes = lnXMLNode.getAttributes();
		Node node;
		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("inst")) {
					inst = node.getNodeValue();
				}
				else if (nodeName.equals("lnType")) {
					lnType = node.getNodeValue();
				}
				else if (nodeName.equals("lnClass")) {
					lnClass = node.getNodeValue();
				}
				else if (nodeName.equals("desc")) {
					desc = node.getNodeValue();
				}
				else if (nodeName.equals("prefix")) {
					prefix = node.getNodeValue();
				}
			}
		}

		if (inst == null) {
			throw new SclParseException("Required attribute \"inst\" not found!");
		}
		if (lnType == null) {
			throw new SclParseException("Required attribute \"lnType\" not found!");
		}
		if (lnClass == null) {
			throw new SclParseException("Required attribute \"lnClass\" not found!");
		}

		String lnName = prefix + lnClass + inst;
		String ref = parentRef + '/' + lnName;

		LnType lnTypeDef = typeDefinitions.getLNodeType(lnType);

		List<FcDataObject> dataObjects = new ArrayList<FcDataObject>();

		if (lnTypeDef == null) {
			throw new SclParseException("Type " + lnType + " not defined!");
		}
		for (Do dobject : lnTypeDef.getDataObjects()) {

			// look for DOI node with the name of the DO
			Node doiNodeFound = null;
			for (int i = 0; i < lnXMLNode.getChildNodes().getLength(); i++) {
				Node childNode = lnXMLNode.getChildNodes().item(i);
				if ("DOI".equals(childNode.getNodeName())) {

					NamedNodeMap doiAttributes = childNode.getAttributes();
					Node nameAttribute = doiAttributes.getNamedItem("name");
					if (nameAttribute != null && nameAttribute.getNodeValue().equals(dobject.getName())) {
						doiNodeFound = childNode;
					}
				}
			}

			// FcDataObject newDataObject = createDataObject(dobject.getName(),
			// ref, dobject.getType(), doiNodeFound);
			dataObjects.addAll(createDataObject(dobject.getName(), ref, dobject.getType(), doiNodeFound));

			// fcDataObjects.add(dataObject.getFcDataObjects();
		}

		LogicalNode lnNode = new LogicalNode(new ObjectReference(ref), dataObjects);
		// this creates the fcDataObjects list

		/* Add additional elements and parse data instance related information */
		for (int i = 0; i < lnXMLNode.getChildNodes().getLength(); i++) {
			Node childNode = lnXMLNode.getChildNodes().item(i);
			if ("DataSet".equals(childNode.getNodeName())) {

				DataSet dataSet = createDataSet(lnNode, childNode);
				dataSets.put(dataSet.getReferenceStr(), dataSet);

				// lnNode.addDataSet(createDataSet(lnNode, childNode));

			}
			else if ("ReportControl".equals(childNode.getNodeName())) {
				lnNode.addReportControlBlock(createReportControl(lnNode, childNode));
			}
			else if ("LogControl".equals(childNode.getNodeName())) {
				// TODO
				// lnode.addLogControlBlock(createLogControlBlock(lnode,
				// childNode));
			}

		}

		// return new LogicalNode(new ObjectReference(ref), dataObjects, null,
		// null);
		return lnNode;

	}

	// private void parseSDI(Node xmlNode, ModelNode parentNode) throws
	// SclParseException {
	// String name = null;
	//
	// NamedNodeMap attributes = xmlNode.getAttributes();
	// Node node;
	// if (attributes != null) {
	// String nodeName;
	//
	// for (int i = 0; i < attributes.getLength(); i++) {
	// node = attributes.item(i);
	// nodeName = node.getNodeName();
	//
	// if (nodeName.equals("name")) {
	// name = node.getNodeValue();
	// // } else if (nodeName.equals("ix")) {
	// // String ix = node.getNodeValue();
	// // TODO support arrays
	// }
	// }
	// }
	//
	// /* Check for required attributes */
	// if (name == null) {
	// throw new SclParseException("SDI requires \"name\" attribute!");
	// }
	//
	// ModelNode currentNode = parentNode.getChild(name);
	// if ((currentNode == null)) {
	// throw new SclParseException("SAI is not allowed for \"" +
	// parentNode.getReference() + "." + name + "\"!");
	// }
	//
	// // String sdiRef = parRef + "." + name;
	//
	// /* parse child SDIs and DAIs */
	// for (int k = 0; k < xmlNode.getChildNodes().getLength(); k++) {
	// Node attrNode = xmlNode.getChildNodes().item(k);
	//
	// if (attrNode.getNodeName().equals("SDI")) {
	// parseSDI(attrNode, currentNode);
	// }
	// else if (attrNode.getNodeName().equals("DAI")) {
	// parseDAI(attrNode, currentNode);
	// }
	// }
	//
	// } /* parseSDI() */

	// private void parseDAI(Node xmlNode, ModelNode parentNode) throws
	// SclParseException {
	// String name = null;
	// String sAddr = null;
	// String ix = null;
	//
	// NamedNodeMap attributes = xmlNode.getAttributes();
	// Node node;
	// if (attributes != null) {
	// String nodeName;
	//
	// for (int i = 0; i < attributes.getLength(); i++) {
	// node = attributes.item(i);
	// nodeName = node.getNodeName();
	//
	// if (nodeName.equals("name")) {
	// name = node.getNodeValue();
	// }
	// else if (nodeName.equals("sAddr")) {
	// sAddr = node.getNodeValue();
	// // } else if (nodeName.equals("valKind")) {
	// // valKind = node.getNodeValue();
	// // } else if (nodeName.equals("desc")) {
	// // desc = node.getNodeValue();
	// }
	// else if (nodeName.equals("ix")) {
	// ix = node.getNodeValue();
	// }
	// }
	// }
	//
	// /* Check for required attributes */
	// if (name == null) {
	// throw new SclParseException("DAI requires \"name\" attribute!");
	// }
	//
	// // String daiRef = lnode.getReference() + "." + doName + "." + name;
	//
	// ModelNode currentNode = parentNode.getChild(name);
	// if ((currentNode == null) || (currentNode.hasChildren())) {
	// throw new SclParseException("DAI is not allowed for \"" +
	// parentNode.getReference() + "\"!");
	// }
	//
	// ConstructedDataAttribute da;
	// if (currentNode.getObjectType() == ObjectType.ARRAY_OF_DA) {
	// Array array = (Array) currentNode;
	// int index = Integer.parseInt(ix);
	// if (ix == null || array.get(index) == null) {
	// throw new SclParseException("DAI array index \"ix\" out of bound: " +
	// parentNode.getReference() + '['
	// + index + ']');
	// }
	// da = (ConstructedDataAttribute) array.get(index);
	// }
	// else {
	// da = (ConstructedDataAttribute) currentNode;
	// }
	//
	// if (sAddr != null) {
	// da.getValue().setsAddr(sAddr);
	// // System.out.println("Short address: " + sAddr + " for " + daiRef);
	// }
	// } /* parseDAI() */
	//
	private DataSet createDataSet(LogicalNode lnNode, Node xmlNode) throws SclParseException {

		Node nameAttribute = xmlNode.getAttributes().getNamedItem("name");
		if (nameAttribute == null) {
			throw new SclParseException("DataSet must have a name");
		}

		String name = nameAttribute.getNodeValue();

		List<FcModelNode> members = new ArrayList<FcModelNode>();

		for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
			Node childNode = xmlNode.getChildNodes().item(i);
			if ("FCDA".equals(childNode.getNodeName())) {

				Node doNameAttribute = childNode.getAttributes().getNamedItem("doName");
				Node fcAttribute = childNode.getAttributes().getNamedItem("fc");
				if (fcAttribute == null) {
					throw new SclParseException("FCDA must have an fc");
				}
				FunctionalConstraint fc = FunctionalConstraint.fromString(fcAttribute.getNodeValue());

				if (doNameAttribute != null) {
					String doName = doNameAttribute.getNodeValue();

					if (!doName.isEmpty()) {
						ModelNode dataObject = lnNode.getChild(doName, fc);
						if (dataObject == null) {
							throw new SclParseException("LogicalNode " + lnNode.getNodeName()
									+ " does not have a DataObject " + doName + " for DataSet " + name);
						}
						members.add((FcModelNode) dataObject);
					}

					else {
						// add only the functionally constrained data objects if
						// doName is empty?
						List<FcDataObject> fcDataObjects = lnNode.getChildren(fc);
						if (fcDataObjects == null) {
							throw new SclParseException("LogicalNode " + lnNode.getNodeName()
									+ " does not have children with functional constraint " + fc + " for DataSet "
									+ name);
						}

						for (FcDataObject dataObj : fcDataObjects) {
							members.add(dataObj);
						}

					}
				}
				// check if daName exists
				else {
					Node daNameAttribute = childNode.getAttributes().getNamedItem("daName");
					String daName = daNameAttribute.getNodeValue();

					List<FcDataObject> fcDataObjects = lnNode.getChildren(fc);

					if (fcDataObjects == null) {
						throw new SclParseException("LogicalNode " + lnNode.getNodeName()
								+ " does not have children with functional constraint " + fc + " for DataSet " + name);
					}

					if (daNameAttribute == null || daName.isEmpty()) {

						for (FcDataObject dataObj : fcDataObjects) {
							members.addAll(dataObj.getBasicDataAttributes());
						}
					}

					else {
						// add all the data attributes with the same name and
						// functional constraint
						for (FcDataObject dataObj : fcDataObjects) {
							members.add((FcModelNode) dataObj.getChild(daName));
						}

					}

				}

			}

		}

		DataSet dataSet = new DataSet(lnNode.getReference().toString() + '.' + name, members, false);
		return dataSet;
	}

	private ReportControlBlock createReportControl(LogicalNode lnode, Node xmlNode) throws SclParseException {
		// ReportControlBlock rcb;
		// FunctionalConstraint fc;

		boolean bufferedReport;

		if (getAttribute(xmlNode, "buffered", null) != null) {
			// fc = FunctionalConstraint.BR;
			bufferedReport = true;
		}
		else {
			// fc = FunctionalConstraint.RP;
			bufferedReport = false;
		}
		TriggerConditions trigOps = null;
		OptFields optFields = null;
		boolean rptEna = false;

		ObjectReference reportObjRef = new ObjectReference(lnode.getReference().toString() + "."
				+ getAttribute(xmlNode, "name", "ReportControl must have a name"));
		List<ModelNode> children = new ArrayList<ModelNode>();

		for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
			Node childNode = xmlNode.getChildNodes().item(i);
			if ("TrgOps".equals(childNode.getNodeName())) {
				// rcb.setTrgOps(createTriggerConditions(childNode));
				trigOps = createTriggerConditions(childNode, lnode);
			}
			else if ("OptFields".equals(childNode.getNodeName())) {
				// rcb.setOptFlds(createOptFields(childNode));
				optFields = createOptFields(childNode, lnode);
			}
			else if ("RptEnabled".equals(childNode.getNodeName())) {
				// rcb.setRptEna(true);
				rptEna = false;
				// TODO there may be ClientLN fields here that describe for
				// which
				// clients
				// buffered reports shall buffer data. This is not implemented
				// yet
			}
		}
		// rcb.setName(getAttribute(xmlNode, "name",
		// "ReportControl must have a name"));

		children.add(new DaVisibleString(new ObjectReference(reportObjRef.toString() + ".RptID"), null, "",
				getAttribute(xmlNode, "rptID", "ReportControl must have a rptID").getBytes(), 129, false, false));
		children.add(new DaBoolean(new ObjectReference(reportObjRef.toString() + ".RptEna"), null, "", rptEna, false,
				false));

		if (!bufferedReport) {
			children.add(new DaBoolean(new ObjectReference(reportObjRef.toString() + ".Resv"), null, "", false, false,
					false));
		}

		// String nodeValue = getAttribute(xmlNode, "datSet", "ReportControl " +
		// reportObjRef
		// + " must have attribute datSet");

		Node dataSetAttribute = xmlNode.getAttributes().getNamedItem("datSet");
		DataSet dataSet = null;
		if (dataSetAttribute != null) {
			String nodeValue = dataSetAttribute.getNodeValue();
			dataSet = dataSets.get(lnode.getReference() + "." + nodeValue);
			// dataSet = lnode.getDataSet(nodeValue);
			if (dataSet == null) {
				throw new SclParseException("DataSet " + nodeValue + " does not exist for ReportControl");
			}

			children.add(new DaVisibleString(new ObjectReference(reportObjRef.toString() + ".DatSet"), null, "",
					dataSet.getReferenceStr().getBytes(), 255, false, false));
		}
		children.add(new DaInt64(new ObjectReference(reportObjRef.toString() + ".ConfRev"), null, "", getLongAttribute(
				xmlNode, "confRev", "ReportControl must have a convRef"), false, false));
		children.add(optFields);

		// in iec 61850-7-2 says it is of type INT32U for BufTm
		children.add(new DaInt32u(new ObjectReference(reportObjRef.toString() + ".BufTm"), null, "", new Long(
				(int) getLongAttribute(xmlNode, "bufTime", null)), false, false));
		children.add(new DaInt8u(new ObjectReference(reportObjRef.toString() + ".SqNum"), null, "",
				new Short((byte) 0), false, false));
		children.add(trigOps);
		children.add(new DaInt64(new ObjectReference(reportObjRef.toString() + ".IntgPd"), null, "", getLongAttribute(
				xmlNode, "intgPd", null), false, false));
		children.add(new DaBoolean(new ObjectReference(reportObjRef.toString() + ".GI"), null, "", false, false, false));

		// rcb.setDataSet(dataSet);
		// rcb.setIntgPd(getLongAttribute(xmlNode, "intgPd", null));
		// rcb.setRptID(getAttribute(xmlNode, "rptID",
		// "ReportControl must have a rptID"));
		// rcb.setConvRef(getLongAttribute(xmlNode, "confRev",
		// "ReportControl must have a convRef"));
		// rcb.setBufTm(getLongAttribute(xmlNode, "bufTime", null));

		if (bufferedReport) {

			children.add(new DaBoolean(new ObjectReference(reportObjRef.toString() + ".PurgeBuf"), null, "", false,
					false, false));

			children.add(new DaOctetString(new ObjectReference(reportObjRef.toString() + ".EntryID"), null, "",
					new byte[8], 8, false, false));

			children.add(new DaOctetString(new ObjectReference(reportObjRef.toString() + ".TimeOfEntry"), null, "",
					new byte[6], 6, false, false));

			// these are optional
			// children.add(new INT16(new
			// ObjectReference(reportObjRef.toString()+".ResvTms"), fc, "", new
			// Short((short) 0), false, false));
			//
			// children.add(new OCTET_STRING_64(new
			// ObjectReference(reportObjRef.toString()
			// + ".Owner"), null, "", new byte[0], false, false));
			BufferedReportControlBlock brcb = new BufferedReportControlBlock(reportObjRef, children);
			brcb.setDataSet(dataSet);
			return brcb;
		}
		else {
			// this is optional
			// children.add(new OCTET_STRING_64(new
			// ObjectReference(reportObjRef.toString()
			// + ".Owner"), null, "", new byte[0] , false, false));
			UnbufferedReportContrlBlock urcb = new UnbufferedReportContrlBlock(reportObjRef, children);
			urcb.setDataSet(dataSet);
			return urcb;
		}

	}

	private OptFields createOptFields(Node xmlNode, LogicalNode lnode) throws SclParseException {
		OptFields of = new OptFields(new ObjectReference(lnode.getReference().toString() + ".OptFlds"));
		of.setBufOvfl(getBooleanAttribute(xmlNode, "bufOvfl"));
		of.setConfigRef(getBooleanAttribute(xmlNode, "configRef"));
		of.setDataRef(getBooleanAttribute(xmlNode, "dataRef"));
		of.setDataSet(getBooleanAttribute(xmlNode, "dataSet"));
		of.setEntryId(getBooleanAttribute(xmlNode, "entryID"));
		of.setReasonCode(getBooleanAttribute(xmlNode, "reasonCode"));
		/* segmentation is depreciated and not used */
		// of.setSegmentation(getBooleanAttribute(xmlNode, "segmentation"));
		of.setSeqNum(getBooleanAttribute(xmlNode, "seqNum"));
		of.setTimeStamp(getBooleanAttribute(xmlNode, "timeStamp"));
		return of;
	}

	private TriggerConditions createTriggerConditions(Node xmlNode, LogicalNode lnode) throws SclParseException {
		TriggerConditions tc = new TriggerConditions(new ObjectReference(lnode.getReference().toString() + ".TrgOps"));
		tc.setDataChange(getBooleanAttribute(xmlNode, "dchg"));
		tc.setDataUpdate(getBooleanAttribute(xmlNode, "dupd"));
		tc.setQualityChange(getBooleanAttribute(xmlNode, "qchg"));
		tc.setIntegrity(getBooleanAttribute(xmlNode, "period"));

		// should be true if the gi field is not present in the SCL
		if (getAttribute(xmlNode, "gi", null) == null) {
			tc.setGeneralInterrogation(true);
		}
		else {
			tc.setGeneralInterrogation(getBooleanAttribute(xmlNode, "gi"));
		}
		return tc;
	}

	// private LogControlBlock createLogControlBlock(LogicalNode lnode,
	// Node xmlNode) throws SclParseException {
	// // LogControlBlock lcb = new LogControlBlock();
	// // String name = getAttribute(xmlNode, "name",
	// // "LogControl must have a name");
	// // lcb.setName(name);
	// // lcb.setLogRef(new ObjectReference(lnode.getReference() + "." +
	// // name));
	// // String nodeValue = getAttribute(xmlNode, "datSet",
	// // "LogControl must have attribute datSet");
	// // DataSet dataSet = lnode.getDataSet(nodeValue);
	// // if (dataSet == null) {
	// // throw new SclParseException("DataSet " + nodeValue
	// // + " does not exist for LogControl");
	// // }
	// // lcb.setDataSet(dataSet);
	// // lcb.setIntgPd(getLongAttribute(xmlNode, "intgPd", null));
	// // lcb.setLogEna(getBooleanAttribute(xmlNode, "logEna"));
	// // for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
	// // Node childNode = xmlNode.getChildNodes().item(i);
	// // if ("TrgOps".equals(childNode.getNodeName())) {
	// // lcb.setTrgOps(createTriggerConditions(childNode));
	// // }
	// // }
	// return null;
	// }

	private boolean getBooleanAttribute(Node xmlNode, String name) throws SclParseException {
		String value = getAttribute(xmlNode, name, null);
		return value != null && "true".equalsIgnoreCase(value);
	}

	private long getLongAttribute(Node xmlNode, String name, String errorMessage) throws SclParseException {
		String value = getAttribute(xmlNode, name, errorMessage);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				throw new SclParseException("Wrong number format for node " + xmlNode.getNodeName() + ", attribute "
						+ name + ": " + e.getMessage());
			}
		}
		return 0L;
	}

	private String getAttribute(Node xmlNode, String name, String errorMessage) throws SclParseException {
		Node dataSetAttribute = xmlNode.getAttributes().getNamedItem(name);
		if (dataSetAttribute == null) {
			if (errorMessage == null) {
				return null;
			}
			else {
				throw new SclParseException(errorMessage);
			}
		}
		return dataSetAttribute.getNodeValue();
	}

	private List<FcDataObject> createDataObject(String name, String parentRef, String doTypeID, Node doiNode)
			throws SclParseException {

		DoType doType = typeDefinitions.getDOType(doTypeID);

		if (doType == null) {
			throw new SclParseException("DO type " + doType + " not defined!");
		}

		String ref = parentRef + '.' + name;

		List<ModelNode> childNodes = new ArrayList<ModelNode>();

		for (Da dattr : doType.getdAttributes()) {

			// look for DAI node with the name of the DA
			Node iNodeFound = findINode(doiNode, dattr.getName());

			if (dattr.getCount() >= 1) {
				childNodes.add(createArrayOfDataAttributes(ref + '.' + dattr.getName(), dattr, iNodeFound));
			}
			else {
				childNodes.add(createDataAttribute(ref + '.' + dattr.getName(), dattr.getFc(), dattr, iNodeFound));
			}

		}

		for (Sdo sdo : doType.getSdos()) {
			// TODO array of data object
			Node iNodeFound = findINode(doiNode, sdo.getName());

			List<FcDataObject> fcDataObjects = createDataObject(sdo.getName(), ref, sdo.getType(), iNodeFound);

			childNodes.addAll(createDataObject(sdo.getName(), ref, sdo.getType(), iNodeFound));

			// childNodes.add(createDataObject(sdo.getName(), ref,
			// sdo.getType(), iNodeFound));
		}

		if (doiNode != null) {
			NamedNodeMap doiAttributes = doiNode.getAttributes();
			for (int j = 0; j < doiAttributes.getLength(); j++) {
				String attributeName = doiAttributes.item(j).getNodeName();

				if (attributeName.equals("ix")) {
					// TODO parse "ix"
				}
				// TODO parse "accessControl"
			}
		}

		Map<FunctionalConstraint, List<FcModelNode>> subFCDataMap = new LinkedHashMap<FunctionalConstraint, List<FcModelNode>>();

		for (FunctionalConstraint fc : FunctionalConstraint.values()) {
			subFCDataMap.put(fc, new LinkedList<FcModelNode>());
		}

		for (ModelNode childNode : childNodes) {
			subFCDataMap.get(((FcModelNode) childNode).getFunctionalConstraint()).add((FcModelNode) childNode);
		}

		List<FcDataObject> fcDataObjects = new LinkedList<FcDataObject>();
		ObjectReference objectReference = new ObjectReference(ref);

		for (FunctionalConstraint fc : FunctionalConstraint.values()) {
			if (subFCDataMap.get(fc).size() > 0) {
				fcDataObjects.add(new FcDataObject(objectReference, fc, subFCDataMap.get(fc), doType.getCdc()));
			}
		}

		return fcDataObjects;
	}

	private Node findINode(Node iNode, String dattrName) {

		if (iNode == null) {
			return null;
		}

		Node iNodeFound = null;
		for (int i = 0; i < iNode.getChildNodes().getLength(); i++) {
			Node childNode = iNode.getChildNodes().item(i);
			// System.out.println("node name: " + iNode.getNodeName());
			// System.out.println("child node name: " +
			// childNode.getNodeName());
			if (childNode.getAttributes() != null) {

				Node nameAttribute = childNode.getAttributes().getNamedItem("name");
				if (nameAttribute != null && nameAttribute.getNodeValue().equals(dattrName)) {
					iNodeFound = childNode;
				}
			}
		}
		return iNodeFound;
	}

	private Array createArrayOfDataAttributes(String ref, Da sclDA, Node iXMLNode) throws SclParseException {

		FunctionalConstraint fc = sclDA.getFc();
		int size = sclDA.getCount();

		List<FcModelNode> arrayItems = new ArrayList<FcModelNode>();
		for (int i = 0; i < size; i++) {
			// ArrayItem item = createDataAttribute(parentRef, fc, sclDA);
			// ArrayItem item = new DataAttribute(parentRef,
			// sclDA.getName(), fc);
			// newArray.add(item);
			// TODO:
			// if (prototypeItem.getBasicType() == BasicTypeEnum.FLOAT32) {
			// prototypeItem.getValue().setValue(i / 100f); // FIXME just a
			// // test
			// }
			// changed from "[]" to "()" for arrays
			arrayItems.add(createDataAttribute(ref + '(' + i + ')', null, sclDA, iXMLNode));
		}

		return new Array(new ObjectReference(ref), fc, arrayItems);
	}

	// returns ConstructedDataAttribute or BasicDataAttribute
	private FcModelNode createDataAttribute(String ref, FunctionalConstraint fc, AbstractDataAttribute dattr,
			Node iXMLNode) throws SclParseException {

		// String ref = parentRef + "." + dattr.getName();

		/* Check if attribute is a basic type */
		if (Util.isBasicType(dattr.getbType())) {
			// System.out.println(" | BASIC TYPE: " + dattr.getbType());

			String sAddr = null;
			if (iXMLNode != null) {

				NamedNodeMap iAttributes = iXMLNode.getAttributes();

				// FIXME already done in AbstractDataAttribute?
				for (int j = 0; j < iAttributes.getLength(); j++) {
					String attributeName = iAttributes.item(j).getNodeName();

					if (attributeName.equals("sAddr")) {
						// System.out.println(".............................................sAddr found: "
						// + iAttributes.item(j).getNodeValue());
						sAddr = iAttributes.item(j).getNodeValue();
					}
				}
			}

			// DaType basicType = Util.bTypeFromSCLString(dattr.getbType());

			boolean dchg = false;
			boolean dupd = false;
			boolean qchg = false;

			if (dattr instanceof Da) {
				dchg = ((Da) dattr).isDchg();
				dupd = ((Da) dattr).isDupd();
				qchg = ((Da) dattr).isQchg();
			}

			String bType = dattr.getbType();

			if (bType.equals("BOOLEAN")) {
				return new DaBoolean(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT8")) {
				return new DaInt8(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT16")) {
				return new DaInt16(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT32")) {
				return new DaInt32(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT64")) {
				return new DaInt64(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT8U")) {
				return new DaInt8u(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT16U")) {
				return new DaInt16u(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("INT32U")) {
				return new DaInt32u(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("FLOAT32")) {
				return new DaFloat32(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("FLOAT64")) {
				return new DaFloat64(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.startsWith("VisString")) {
				return new DaVisibleString(new ObjectReference(ref), fc, sAddr, null, Integer.parseInt(dattr.getbType()
						.substring(9)), dchg, dupd);
			}
			if (bType.startsWith("Unicode")) {
				return new DaUnicodeString(new ObjectReference(ref), fc, sAddr, null, Integer.parseInt(dattr.getbType()
						.substring(7)), dchg, dupd);
			}
			if (bType.startsWith("Octet")) {
				return new DaOctetString(new ObjectReference(ref), fc, sAddr, null, Integer.parseInt(dattr.getbType()
						.substring(5)), dchg, dupd);
			}
			if (bType.equals("Quality")) {
				return new DaBitString(new ObjectReference(ref), fc, sAddr, null, 13, BitStringType.QUALITY, dchg, dupd);
			}
			if (bType.equals("Check")) {
				return new DaBitString(new ObjectReference(ref), fc, sAddr, null, 2, BitStringType.CHECK, dchg, dupd);
			}
			if (bType.equals("Dbpos")) {
				return new DaBitString(new ObjectReference(ref), fc, sAddr, null, 2, BitStringType.DBPOS, dchg, dupd);
			}
			if (bType.equals("Tcmd")) {
				return new DaBitString(new ObjectReference(ref), fc, sAddr, null, 2, BitStringType.TCMD, dchg, dupd);
			}
			if (bType.equals("Timestamp")) {
				return new DaTimestamp(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}
			if (bType.equals("Enum")) {
				// TODO check size and choose corresponding INT type
				return new DaInt8(new ObjectReference(ref), fc, sAddr, null, dchg, dupd);
			}

			throw new SclParseException("Invalid bType: " + bType);

		}
		else {
			// System.out.println(" | COMPLEX TYPE: " + dattr.getbType() +
			// " OF TYPE " + dattr.getType());

			if (dattr.getbType().equals("Struct")) {
				org.openiec61850.scl.DaType datype = typeDefinitions.getDAType(dattr.getType());

				if (datype == null) {
					throw new SclParseException("DAType " + dattr.getbType() + " not declared!");
				}

				List<FcModelNode> subDataAttributes = new ArrayList<FcModelNode>();
				for (Bda bda : datype.getBdas()) {

					Node iNodeFound = findINode(iXMLNode, dattr.getName());

					subDataAttributes.add(createDataAttribute(ref + '.' + bda.getName(), fc, bda, iNodeFound));
				}

				return new ConstructedDataAttribute(new ObjectReference(ref), fc, subDataAttributes);
			}
			// else if (dattr.getbType().equals("Enum")) {
			//
			// String sAddr = null;
			// if (iXMLNode != null) {
			//
			// NamedNodeMap iAttributes = iXMLNode.getAttributes();
			// for (int j = 0; j < iAttributes.getLength(); j++) {
			// String attributeName = iAttributes.item(j).getNodeName();
			//
			// if (attributeName.equals("sAddr")) {
			// sAddr = iAttributes.item(j).getNodeValue();
			// }
			// }
			// }
			//
			// return new BasicDataAttribute(new ObjectReference(ref), fc,
			// BasicType.INT32, sAddr, null, false, false,
			// false);
			//
			// // TODO implement Enums
			// // newDataAttribute = new ConstructedDataAttribute(parentRef,
			// // dattr.getName(), fc);
			// // newDataAttribute.setValue(new
			// // DataAttributeValue(BasicTypeEnum.INT32));
			// // newDataAttribute.getValue().setsAddr(dattr.getsAddr());
			// }
			// else if (dattr.getbType().equals("Dbpos")) {
			// // TODO implement Dbpos
			// // newDataAttribute = new ConstructedDataAttribute(parentRef,
			// // dattr.getName(), fc);
			// }
			else {
				throw new SclParseException("Basic type " + dattr.getbType() + " not implemented!");
			}

			// return null;
		}

	} /* createDataAttribute() */

	private String parseIEDName(Node iedNode) throws SclParseException {
		Node nameAttribute = iedNode.getAttributes().getNamedItem("name");
		if (nameAttribute != null) {
			String name = nameAttribute.getNodeValue();
			if ((name != null) && (name.length() != 0)) {
				return name;
			}
		}
		throw new SclParseException("IED must have a name!");
	}

	private TypeDefinitions getTypeDefinitions() {
		return typeDefinitions;
	}
}
