package org.openiec61850;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openiec61850.common.model.report.BufferedReportControlBlock;
import org.openiec61850.common.model.report.ReportControlBlock;
import org.openiec61850.common.model.report.UnbufferedReportContrlBlock;
import org.openiec61850.jmms.mms.asn1.ConfirmedResponsePdu;
import org.openiec61850.jmms.mms.asn1.ConfirmedServiceResponse;
import org.openiec61850.jmms.mms.asn1.GetVariableAccessAttributesResponse;
import org.openiec61850.jmms.mms.asn1.StructComponent;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_structure.SubSeqOf_components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataDefinitionResParser {

	private static Logger logger = LoggerFactory.getLogger(DataDefinitionResParser.class);

	static LogicalNode parseGetDataDefinitionResponse(ConfirmedResponsePdu confirmedResponsePdu, ObjectReference lnRef)
			throws ServiceError {

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.getVariableAccessAttributes == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataDefinitionResponse: Error decoding GetDataDefinitionResponsePdu");
		}

		GetVariableAccessAttributesResponse varAccAttrs = confirmedServiceResponse.getVariableAccessAttributes;
		TypeSpecification typeSpec = varAccAttrs.typeSpecification;
		if (typeSpec.structure == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataDefinitionResponse: Error decoding GetDataDefinitionResponsePdu");
		}

		SubSeqOf_components structure = typeSpec.structure.components;

		List<FcDataObject> fcDataObjects = new LinkedList<FcDataObject>();
		List<ReportControlBlock> reports = new LinkedList<ReportControlBlock>();

		FunctionalConstraint fc;
		for (StructComponent fcComponent : structure.seqOf) {
			if (fcComponent.componentName == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}

			if (fcComponent.componentType.structure == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}

			String fcString = fcComponent.componentName.toString();
			if (fcString.equals("LG") || fcString.equals("GO") || fcString.equals("GS") || fcString.equals("MS")
					|| fcString.equals("US")) {
				continue;
			}

			fc = FunctionalConstraint.fromString(fcComponent.componentName.toString());
			SubSeqOf_components subStructure = fcComponent.componentType.structure.components;

			if (fcString.equals("RP") || fcString.equals("BR")) {
				reports.addAll(getRCBFromSubStructure(lnRef, fcString, subStructure));
			}
			else {
				fcDataObjects.addAll(getFCDataObjectsFromSubStructure(lnRef, fc, subStructure));
			}
		}

		LogicalNode ln = new LogicalNode(lnRef, fcDataObjects);
		for (ReportControlBlock rcb : reports) {
			ln.addReportControlBlock(rcb);
		}

		return ln;

	}

	private static List<FcDataObject> getFCDataObjectsFromSubStructure(ObjectReference lnRef, FunctionalConstraint fc,
			SubSeqOf_components structure) throws ServiceError {

		List<StructComponent> structComponents = structure.seqOf;
		List<FcDataObject> dataObjects = new ArrayList<FcDataObject>(structComponents.size());

		for (StructComponent doComp : structComponents) {
			if (doComp.componentName == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}
			if (doComp.componentType.structure == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}

			ObjectReference doRef = new ObjectReference(lnRef + "." + doComp.componentName.toString());
			List<FcModelNode> children = getDOSubModelNodesFromSubStructure(doRef, fc,
					doComp.componentType.structure.components, false);
			dataObjects.add(new FcDataObject(doRef, fc, children, null));

		}

		return dataObjects;

	}

	private static List<FcModelNode> getDOSubModelNodesFromSubStructure(ObjectReference parentRef,
			FunctionalConstraint fc, SubSeqOf_components structure, boolean parentWasArray) throws ServiceError {

		Collection<StructComponent> structComponents = structure.seqOf;
		List<FcModelNode> dataObjects = new ArrayList<FcModelNode>(structComponents.size());

		for (StructComponent component : structComponents) {
			if (component.componentName == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}

			String childName = component.componentName.toString();
			ObjectReference childReference;
			if (!parentWasArray) {
				childReference = new ObjectReference(parentRef + "." + childName);
			}
			else {
				childReference = new ObjectReference(parentRef + childName);
			}
			dataObjects.add(getModelNodesFromTypeSpecification(childReference, fc, component.componentType, false));

		}
		return dataObjects;
	}

	private static FcModelNode getModelNodesFromTypeSpecification(ObjectReference ref, FunctionalConstraint fc,
			TypeSpecification mmsTypeSpec, boolean parentWasArray) throws ServiceError {

		if (mmsTypeSpec.array != null) {

			int numArrayElements = (int) mmsTypeSpec.array.numberOfElements.val;
			List<FcModelNode> arrayChildren = new ArrayList<FcModelNode>(numArrayElements);
			for (int i = 0; i < numArrayElements; i++) {
				arrayChildren.add(getModelNodesFromTypeSpecification(
						new ObjectReference(ref + "(" + Integer.toString(i) + ")"), fc, mmsTypeSpec.array.elementType,
						true));
			}

			return new Array(ref, fc, arrayChildren);

		}

		if (mmsTypeSpec.structure != null) {
			List<FcModelNode> children = getDOSubModelNodesFromSubStructure(ref, fc, mmsTypeSpec.structure.components,
					parentWasArray);
			return (new ConstructedDataAttribute(ref, fc, children));
		}

		// it is a single element
		BasicDataAttribute bt = convertMmsBasicTypeSpec(ref, fc, mmsTypeSpec);
		if (bt == null) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
					"decodeGetDataDefinitionResponse: Unknown data type received " + ref.getName());
		}
		return (bt);

	}

	private static List<ReportControlBlock> getRCBFromSubStructure(ObjectReference lnRef, String fcString,
			SubSeqOf_components structure) throws ServiceError {
		List<StructComponent> structComponents = structure.seqOf;
		List<ReportControlBlock> rcbs = new ArrayList<ReportControlBlock>(structComponents.size());

		for (StructComponent rcb : structComponents) {
			if (rcb.componentName == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}
			if (rcb.componentType.structure == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
						"Error decoding GetDataDefinitionResponsePdu");
			}

			ObjectReference rcbRef = new ObjectReference(lnRef + "." + rcb.componentName.toString());

			List<ModelNode> children = new LinkedList<ModelNode>();
			for (StructComponent component : rcb.componentType.structure.components.seqOf) {
				if (component.componentName == null) {
					throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
							"Error decoding GetDataDefinitionResponsePdu");
				}
				ObjectReference childName = new ObjectReference(lnRef + "." + component.componentName.toString());
				TypeSpecification typeSpec = component.componentType;

				// if (typeSpec.bit_string != null) {
				// if (typeSpec.bit_string.val == -10) {
				// children.add(new OptFields(childName));
				//
				// }
				// else if (typeSpec.bit_string.val == -6) {
				//
				// children.add(new TriggerConditions(childName));
				// }
				// }
				//
				// else {
				children.add(getModelNodesFromTypeSpecification(childName, null, component.componentType, false));
				// }
			}

			if (fcString.equals("RP")) {
				rcbs.add(new UnbufferedReportContrlBlock(rcbRef, children));
			}
			else {
				rcbs.add(new BufferedReportControlBlock(rcbRef, children));
			}

		}

		return rcbs;

	}

	private static BasicDataAttribute convertMmsBasicTypeSpec(ObjectReference ref, FunctionalConstraint fc,
			TypeSpecification mmsTypeSpec) throws ServiceError {

		if (mmsTypeSpec.boolean_ != null) {
			return new DaBoolean(ref, fc, null, null, false, false);
		}
		if (mmsTypeSpec.bit_string != null) {
			return new DaBitString(ref, fc, null, null, Math.abs((int) mmsTypeSpec.bit_string.val),
					BitStringType.UNKNOWN, false, false);
		}
		else if (mmsTypeSpec.integer != null) {
			switch ((int) mmsTypeSpec.integer.val) {
			case 8:
				return new DaInt8(ref, fc, null, null, false, false);
			case 16:
				return new DaInt16(ref, fc, null, null, false, false);
			case 32:
				return new DaInt32(ref, fc, null, null, false, false);
			case 64:
				return new DaInt64(ref, fc, null, null, false, false);
			}
		}
		else if (mmsTypeSpec.unsigned != null) {
			switch ((int) mmsTypeSpec.unsigned.val) {
			case 8:
				return new DaInt8u(ref, fc, null, null, false, false);
			case 16:
				return new DaInt16u(ref, fc, null, null, false, false);
			case 32:
				return new DaInt32u(ref, fc, null, null, false, false);
			}
		}
		else if (mmsTypeSpec.floating_point != null) {
			int floatSize = (int) mmsTypeSpec.floating_point.format_width.val;
			if (floatSize == 32) {
				return new DaFloat32(ref, fc, null, null, false, false);
			}
			else if (floatSize == 64) {
				return new DaFloat64(ref, fc, null, null, false, false);
			}
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "FLOAT of size: " + floatSize
					+ " is not supported.");
		}
		else if (mmsTypeSpec.octet_string != null) {
			int stringSize = (int) mmsTypeSpec.octet_string.val;
			if (stringSize > 255 || stringSize < -255) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "OCTET_STRING of size: "
						+ stringSize + " is not supported.");
			}
			return new DaOctetString(ref, fc, null, null, Math.abs(stringSize), false, false);

		}
		else if (mmsTypeSpec.visible_string != null) {
			int stringSize = (int) mmsTypeSpec.visible_string.val;
			if (stringSize > 255 || stringSize < -255) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "VISIBLE_STRING of size: "
						+ stringSize + " is not supported.");
			}
			return new DaVisibleString(ref, fc, null, null, Math.abs(stringSize), false, false);
		}
		else if (mmsTypeSpec.mms_string != null) {
			int stringSize = (int) mmsTypeSpec.mms_string.val;
			if (stringSize > 255 || stringSize < -255) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "UNICODE_STRING of size: "
						+ stringSize + " is not supported.");
			}
			return new DaUnicodeString(ref, fc, null, null, Math.abs(stringSize), false, false);
		}
		else if (mmsTypeSpec.utc_time != null) {
			return new DaTimestamp(ref, fc, null, null, false, false);
		}
		else if (mmsTypeSpec.binary_time != null) {
			return new DaEntryTime(ref, fc, null, null, false, false);
		}
		return null;
	}
}
