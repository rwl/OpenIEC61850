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
package org.openiec61850;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openiec61850.common.model.report.BufferedReportControlBlock;
import org.openiec61850.common.model.report.OptFields;
import org.openiec61850.common.model.report.Report;
import org.openiec61850.common.model.report.ReportControlBlock;
import org.openiec61850.common.model.report.ReportEntryData.ReasonCode;
import org.openiec61850.common.model.report.UnbufferedReportContrlBlock;
import org.openiec61850.jmms.iso.acse.AcseAssociation;
import org.openiec61850.jmms.iso.acse.ClientAcseSAP;
import org.openiec61850.jmms.mms.asn1.AccessResult;
import org.openiec61850.jmms.mms.asn1.ConfirmedRequestPdu;
import org.openiec61850.jmms.mms.asn1.ConfirmedResponsePdu;
import org.openiec61850.jmms.mms.asn1.ConfirmedServiceRequest;
import org.openiec61850.jmms.mms.asn1.ConfirmedServiceResponse;
import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.DefineNamedVariableListRequest;
import org.openiec61850.jmms.mms.asn1.DeleteNamedVariableListRequest;
import org.openiec61850.jmms.mms.asn1.DeleteNamedVariableListRequest.SubSeqOf_listOfVariableListName;
import org.openiec61850.jmms.mms.asn1.DeleteNamedVariableListResponse;
import org.openiec61850.jmms.mms.asn1.GetNameListRequest;
import org.openiec61850.jmms.mms.asn1.GetNameListRequest.SubChoice_objectScope;
import org.openiec61850.jmms.mms.asn1.GetNameListResponse;
import org.openiec61850.jmms.mms.asn1.GetNamedVariableListAttributesResponse;
import org.openiec61850.jmms.mms.asn1.GetVariableAccessAttributesRequest;
import org.openiec61850.jmms.mms.asn1.InformationReport;
import org.openiec61850.jmms.mms.asn1.InitRequestDetail;
import org.openiec61850.jmms.mms.asn1.InitiateRequestPdu;
import org.openiec61850.jmms.mms.asn1.InitiateResponsePdu;
import org.openiec61850.jmms.mms.asn1.MmsPdu;
import org.openiec61850.jmms.mms.asn1.ObjectClass;
import org.openiec61850.jmms.mms.asn1.ObjectName;
import org.openiec61850.jmms.mms.asn1.ObjectName.SubSeq_domain_specific;
import org.openiec61850.jmms.mms.asn1.ReadRequest;
import org.openiec61850.jmms.mms.asn1.ReadResponse;
import org.openiec61850.jmms.mms.asn1.ServiceError.SubChoice_errorClass;
import org.openiec61850.jmms.mms.asn1.UnconfirmedPDU;
import org.openiec61850.jmms.mms.asn1.UnconfirmedService;
import org.openiec61850.jmms.mms.asn1.VariableAccessSpecification;
import org.openiec61850.jmms.mms.asn1.VariableAccessSpecification.SubSeqOf_listOfVariable;
import org.openiec61850.jmms.mms.asn1.VariableDef;
import org.openiec61850.jmms.mms.asn1.VariableSpecification;
import org.openiec61850.jmms.mms.asn1.WriteRequest;
import org.openiec61850.jmms.mms.asn1.WriteRequest.SubSeqOf_listOfData;
import org.openiec61850.jmms.mms.asn1.WriteResponse;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerBoolean;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerNull;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAssociation {

	private static final BerInteger version = new BerInteger(new byte[] { (byte) 0x01, (byte) 0x01 });
	private static final BerBitString proposedParameterCbbBitString = new BerBitString(new byte[] { 0x03, 0x05,
			(byte) 0xf1, 0x00 });

	private static Logger logger = LoggerFactory.getLogger(ClientAssociation.class);

	private AcseAssociation acseAssociation = null;
	private ClientReceiver mmsScsmClientReceiver;

	private InetAddress serverAddress;
	private int serverPort;

	private BlockingQueue<MmsPdu> incomingResponses = new LinkedBlockingQueue<MmsPdu>();
	private BlockingQueue<MmsPdu> incomingReports = new LinkedBlockingQueue<MmsPdu>();

	ServerModel serverModel;

	private int invokeID = 0;

	// not being used:
	// private int negotiatedMaxServOutstandingCalling;
	// private int negotiatedMaxServOutstandingCalled;
	// private int negotiatedDataStructureNestingLevel;

	private int negotiatedMaxPduSize;

	// private String[] mmsFCs = { "MX", "ST", "CO", "CF", "DC", "SP", "SG",
	// "RP", "LG", "BR", "GO", "GS", "SV", "SE",
	// "EX", "SR", "OR", "BL" };

	private int getInvokeID() {
		invokeID = (invokeID + 1) % 2147483647;
		return invokeID;
	}

	private void checkInvokeID(ConfirmedResponsePdu confirmedResponsePdu) throws ServiceError {
		if (confirmedResponsePdu.invokeID.val != invokeID) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding response : incorrect invokeID");
		}
	}

	private static int mmsDataAccessErrorToServiceError(BerInteger dataAccessError) {
		switch ((int) dataAccessError.val) {
		case 1: // hardware_fault
			return ServiceError.FAILED_DUE_TO_SERVER_CONSTRAINT;
		case 2: // temporarily-unavailable
			return ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT;
		case 3: // object-access-denied
			return ServiceError.ACCESS_VIOLATION;
		case 5: // invalid-address
			return ServiceError.PARAMETER_VALUE_INCONSISTENT;
		case 7: // type-inconsistent
			return ServiceError.TYPE_CONFLICT;
		case 10: // object-non-existent
			return ServiceError.INSTANCE_NOT_AVAILABLE;
		default:
			return ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT; // ?
		}
	}

	private static void testForErrorResponse(MmsPdu mmsResponsePdu) throws ServiceError {
		if (mmsResponsePdu.initiateErrorPdu == null) {
			return;
		}
		SubChoice_errorClass errClass = mmsResponsePdu.initiateErrorPdu.errorClass;
		if (errClass != null) {
			if (errClass.vmd_state != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"vmd_state\" with val: " + errClass.vmd_state.val);
			}
			if (errClass.application_reference != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"application_reference\" with val: " + errClass.application_reference.val);
			}
			if (errClass.definition != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"definition\" with val: " + errClass.definition.val);
			}
			if (errClass.resource != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"resource\" with val: " + errClass.resource.val);
			}
			if (errClass.service != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"service\" with val: " + errClass.service.val);
			}
			if (errClass.service_preempt != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"service_preempt\" with val: " + errClass.service_preempt.val);
			}
			if (errClass.time_resolution != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"time_resolution\" with val: " + errClass.time_resolution.val);
			}
			if (errClass.access != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"access\" with val: " + errClass.access.val);
			}
			if (errClass.initiate != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"initiate\" with val: " + errClass.initiate.val);
			}
			if (errClass.conclude != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"conclude\" with val: " + errClass.conclude.val);
			}
			if (errClass.cancel != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"cancel\" with val: " + errClass.cancel.val);
			}
			if (errClass.file != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"file\" with val: " + errClass.file.val);
			}
			if (errClass.others != null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"error class \"others\" with val: " + errClass.others.val);
			}
		}

		throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "unknown error class");
	}

	private MmsPdu encodeWriteReadDecode(MmsPdu requestPdu) throws ServiceError {
		BerByteArrayOutputStream baos = new BerByteArrayOutputStream(500, true);
		try {
			requestPdu.encode(baos, true);
		} catch (Exception e) {
			acseAssociation.disconnect();
			throw new RuntimeException("Error encoding MmsPdu.", e);
		}

		mmsScsmClientReceiver.setResponseExpected();
		try {
			acseAssociation.send(baos.getByteBuffer());
		} catch (IOException e) {
			acseAssociation.disconnect();
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "Error sending packet.", e);
		}

		MmsPdu decodedResponsePdu = null;
		try {
			while (decodedResponsePdu == null && mmsScsmClientReceiver.isAlive()) {
				// Actually, we would need yet another timout setting here, a
				// timeout for receiving a
				// complete message (initial timeout + all fragment timeouts).
				// Since we do not know how
				// many fragments will be used, we re-use the message timeout
				// and loop if there is a chance
				// of getting more data. Without any timeout (when using take()
				// on the queue), this thread
				// would be stuck if the client reciever thread dies.
				decodedResponsePdu = incomingResponses.poll(acseAssociation.getMessageTimeout(), TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
		}
		return decodedResponsePdu;
	}

	ClientAssociation(String serverAccessPointReference, String authenticationParameter, ClientAcseSAP acseSAP,
			int proposedMaxPduSize, int proposedMaxServOutstandingCalling, int proposedMaxServOutstandingCalled,
			int proposedDataStructureNestingLevel, byte[] servicesSupportedCalling, int servicesSupportedCallingNumBits)
			throws IllegalArgumentException, ServiceError {

		negotiatedMaxPduSize = proposedMaxPduSize;

		associate(serverAccessPointReference, authenticationParameter, acseSAP, proposedMaxPduSize,
				proposedMaxServOutstandingCalling, proposedMaxServOutstandingCalled, proposedDataStructureNestingLevel,
				servicesSupportedCalling, servicesSupportedCallingNumBits);

		mmsScsmClientReceiver = new ClientReceiver(acseAssociation, incomingResponses, incomingReports);
		mmsScsmClientReceiver.start();
	}

	private void associate(String serverAccessPointReference, String authenticationParameter, ClientAcseSAP acseSAP,
			int proposedMaxPduSize, int proposedMaxServOutstandingCalling, int proposedMaxServOutstandingCalled,
			int proposedDataStructureNestingLevel, byte[] servicesSupportedCalling, int servicesSupportedCallingNumBits)
			throws ServiceError, IllegalArgumentException {

		readServerAPReference(serverAccessPointReference);

		MmsPdu initiateRequestMMSpdu = constructInitRequestPdu(proposedMaxPduSize, proposedMaxServOutstandingCalling,
				proposedMaxServOutstandingCalled, proposedDataStructureNestingLevel, servicesSupportedCalling,
				servicesSupportedCallingNumBits);

		BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(500, true);
		try {
			initiateRequestMMSpdu.encode(berOStream, true);
		} catch (IOException e) {
			throw new ServiceError(ServiceError.FATAL, e);
		}

		try {
			acseAssociation = acseSAP.associate(serverAddress, serverPort, authenticationParameter,
					berOStream.getByteBuffer());

		} catch (IOException e) {
			if (acseAssociation != null) {
				acseAssociation.disconnect();
			}
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "Error starting association.",
					e);
		}
		ByteBuffer initResponse = acseAssociation.getAssociateResponseAPDU();

		MmsPdu initiateResponseMMSpdu = new MmsPdu();
		ByteArrayInputStream iStream = new ByteArrayInputStream(initResponse.array(), initResponse.arrayOffset()
				+ initResponse.position(), initResponse.limit() - initResponse.position());
		try {
			initiateResponseMMSpdu.decode(iStream, null);
		} catch (IOException e) {
			throw new ServiceError(ServiceError.FATAL, e);
		}

		handleInitiateResponse(initiateResponseMMSpdu, proposedMaxPduSize, proposedMaxServOutstandingCalling,
				proposedMaxServOutstandingCalled, proposedDataStructureNestingLevel);
	}

	private void readServerAPReference(String serverAccessPointReference) throws ServiceError, IllegalArgumentException {
		serverPort = 102;

		String[] serverAPRefSplits = serverAccessPointReference.split(":");
		if (serverAPRefSplits.length < 1 || serverAPRefSplits.length > 2) {
			throw new IllegalArgumentException("serverAccesPointReference argument is invalid");
		}
		if (serverAPRefSplits.length == 2) {
			try {
				serverPort = Integer.decode(serverAPRefSplits[1]);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("serverAccesPointReference argument is invalid", e);
			}
			if (serverPort < 0 || serverPort > 65535) {
				throw new IllegalArgumentException("invalid port");
			}
		}

		try {
			serverAddress = InetAddress.getByName(serverAPRefSplits[0]);
		} catch (UnknownHostException e) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Hostname could not be resolved.", e);
		}
	}

	private static MmsPdu constructInitRequestPdu(int proposedMaxPduSize, int proposedMaxServOutstandingCalling,
			int proposedMaxServOutstandingCalled, int proposedDataStructureNestingLevel,
			byte[] servicesSupportedCalling, int servicesSupportedCallingNumBits) {

		InitRequestDetail initRequestDetail = new InitRequestDetail(version, proposedParameterCbbBitString,
				new BerBitString(servicesSupportedCalling, servicesSupportedCallingNumBits));

		InitiateRequestPdu initiateRequestPdu = new InitiateRequestPdu(new BerInteger(proposedMaxPduSize),
				new BerInteger(proposedMaxServOutstandingCalling), new BerInteger(proposedMaxServOutstandingCalled),
				new BerInteger(proposedDataStructureNestingLevel), initRequestDetail);

		MmsPdu initiateRequestMMSpdu = new MmsPdu(null, null, null, initiateRequestPdu, null, null);

		return initiateRequestMMSpdu;
	}

	private void handleInitiateResponse(MmsPdu responsePdu, int proposedMaxPduSize,
			int proposedMaxServOutstandingCalling, int proposedMaxServOutstandingCalled,
			int proposedDataStructureNestingLevel) throws ServiceError {

		testForErrorResponse(responsePdu);

		if (responsePdu.initiateResponsePdu == null) {
			acseAssociation.disconnect();
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding InitiateResponse Pdu");
		}

		InitiateResponsePdu initiateResponsePdu = responsePdu.initiateResponsePdu;

		if (initiateResponsePdu.localDetailCalled != null) {
			negotiatedMaxPduSize = (int) initiateResponsePdu.localDetailCalled.val;
		}

		int negotiatedMaxServOutstandingCalling = (int) initiateResponsePdu.negotiatedMaxServOutstandingCalling.val;
		int negotiatedMaxServOutstandingCalled = (int) initiateResponsePdu.negotiatedMaxServOutstandingCalled.val;

		int negotiatedDataStructureNestingLevel;
		if (initiateResponsePdu.negotiatedDataStructureNestingLevel != null) {
			negotiatedDataStructureNestingLevel = (int) initiateResponsePdu.negotiatedDataStructureNestingLevel.val;
		}
		else {
			negotiatedDataStructureNestingLevel = proposedDataStructureNestingLevel;
		}

		if (negotiatedMaxPduSize < ClientSAP.MINIMUM_PDU_SIZE || negotiatedMaxPduSize > proposedMaxPduSize
				|| negotiatedMaxServOutstandingCalling > proposedMaxServOutstandingCalling
				|| negotiatedMaxServOutstandingCalling < 0
				|| negotiatedMaxServOutstandingCalled > proposedMaxServOutstandingCalled
				|| negotiatedMaxServOutstandingCalled < 0
				|| negotiatedDataStructureNestingLevel > proposedDataStructureNestingLevel
				|| negotiatedDataStructureNestingLevel < 0) {
			acseAssociation.disconnect();
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "Error negotiating paramters");
		}

		int version = (int) initiateResponsePdu.mmsInitResponseDetail.negotiatedVersionNumber.val;
		if (version != 1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Unsupported version number was negotiated.");
		}

		byte[] servicesSupported = initiateResponsePdu.mmsInitResponseDetail.servicesSupportedCalled.bitString;
		if ((servicesSupported[0] & 0x40) != 0x40) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Obligatory services are not supported by the server.");
		}
	}

	/**
	 * The Model contains not SubDataObjects because they cannot be
	 * distinguished from Constructed Data Attributes in MMS. Also at the moment
	 * the model only contains FCDataObjects
	 */
	public ServerModel retrieveModel() throws ServiceError {

		List<String> ldNames = retrieveLogicalDevices();
		List<List<String>> lnNames = new ArrayList<List<String>>(ldNames.size());

		for (int i = 0; i < ldNames.size(); i++) {
			lnNames.add(retrieveLogicalNodes(ldNames.get(i)));
		}
		List<LogicalDevice> lds = new ArrayList<LogicalDevice>();
		for (int i = 0; i < ldNames.size(); i++) {
			List<LogicalNode> lns = new ArrayList<LogicalNode>();
			for (int j = 0; j < lnNames.get(i).size(); j++) {
				lns.add(retrieveDataDefinitions(new ObjectReference(ldNames.get(i) + "/" + lnNames.get(i).get(j))));
			}
			lds.add(new LogicalDevice(new ObjectReference(ldNames.get(i)), lns));
		}

		serverModel = new ServerModel(lds);

		updateDataSets();

		return serverModel;
	}

	private List<String> retrieveLogicalDevices() throws ServiceError {
		MmsPdu requestPdu = constructGetServerDirectoryRequest(getInvokeID());
		MmsPdu responsePdu = encodeWriteReadDecode(requestPdu);
		return decodeGetServerDirectoryResponse(responsePdu);
	}

	private static MmsPdu constructGetServerDirectoryRequest(int invokeID) {
		ObjectClass objectClass = new ObjectClass(new BerInteger(9));

		GetNameListRequest.SubChoice_objectScope objectScope = new GetNameListRequest.SubChoice_objectScope(
				new BerNull(), null, null);

		GetNameListRequest getNameListRequest = new GetNameListRequest(objectClass, objectScope, null);

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(getNameListRequest, null, null,
				null, null, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(invokeID),
				confirmedServiceRequest);

		MmsPdu confirmedRequestMMSPdu = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);

		return confirmedRequestMMSPdu;

	}

	private List<String> decodeGetServerDirectoryResponse(MmsPdu getServerDirectoryResponseMmsPdu) throws ServiceError {
		testForErrorResponse(getServerDirectoryResponseMmsPdu);

		if (getServerDirectoryResponseMmsPdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding Get Server Directory Response Pdu");
		}

		ConfirmedResponsePdu confirmedResponsePdu = getServerDirectoryResponseMmsPdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.getNameList == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding Get Server Directory Response Pdu");
		}

		List<BerVisibleString> identifiers = confirmedServiceResponse.getNameList.listOfIdentifier.seqOf;
		ArrayList<String> objectRefs = new ArrayList<String>(); // ObjectReference[identifiers.size()];

		for (BerVisibleString identifier : identifiers) {
			objectRefs.add(identifier.toString());
		}

		return objectRefs;
	}

	private List<String> retrieveLogicalNodes(String ld) throws ServiceError {
		List<String> lns = new LinkedList<String>();
		String continueAfterRef = "";
		do {
			MmsPdu getLDDirectoryRequestMMSPdu = constructGetDirectoryRequest(ld, continueAfterRef, true);
			MmsPdu getLDDirectoryResponseMMSPdu = encodeWriteReadDecode(getLDDirectoryRequestMMSPdu);
			continueAfterRef = decodeGetDirectoryResponse(getLDDirectoryResponseMMSPdu, lns);

		} while (continueAfterRef != "");
		return lns;
	}

	private MmsPdu constructGetDirectoryRequest(String ldRef, String continueAfter, boolean logicalDevice) {

		ObjectClass objectClass = null;

		if (logicalDevice) {
			objectClass = new ObjectClass(new BerInteger(0));
		}
		else { // for data sets
			objectClass = new ObjectClass(new BerInteger(2));
		}

		GetNameListRequest getNameListRequest = null;

		SubChoice_objectScope objectScopeChoiceType = new SubChoice_objectScope(null, new BerVisibleString(ldRef), null);

		if (continueAfter != "") {
			getNameListRequest = new GetNameListRequest(objectClass, objectScopeChoiceType, new BerVisibleString(
					continueAfter));
		}
		else {
			getNameListRequest = new GetNameListRequest(objectClass, objectScopeChoiceType, null);
		}

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(getNameListRequest, null, null,
				null, null, null, null);
		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(getInvokeID()),
				confirmedServiceRequest);
		MmsPdu confirmedRequestMMSPdu = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);

		return confirmedRequestMMSPdu;
	}

	/**
	 * Decodes an MMS response which contains the structure of a LD and its LNs
	 * including names of DOs.
	 */
	private String decodeGetDirectoryResponse(MmsPdu getLDDirectoryResponseMmsPdu, List<String> lns)
			throws ServiceError {

		testForErrorResponse(getLDDirectoryResponseMmsPdu);

		if (getLDDirectoryResponseMmsPdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetLDDirectoryResponse: Error decoding server response");
		}

		ConfirmedResponsePdu confirmedResponsePdu = getLDDirectoryResponseMmsPdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;

		if (confirmedServiceResponse.getNameList == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetLDDirectoryResponse: Error decoding server response");
		}

		GetNameListResponse getNameListResponse = confirmedServiceResponse.getNameList;

		List<BerVisibleString> identifiers = getNameListResponse.listOfIdentifier.seqOf;

		if (identifiers.size() == 0) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"decodeGetLDDirectoryResponse: Instance not available");
		}

		BerVisibleString identifier = null;
		Iterator<BerVisibleString> it = identifiers.iterator();

		String idString;

		while (it.hasNext()) {
			identifier = it.next();
			idString = identifier.toString();

			if (idString.indexOf('$') == -1) {
				lns.add(idString);
			}
		}

		if (getNameListResponse.moreFollows != null && getNameListResponse.moreFollows.val == false) {
			return "";
		}
		else {
			return identifier.toString();
		}
	}

	private LogicalNode retrieveDataDefinitions(ObjectReference lnRef) throws ServiceError {
		MmsPdu getDataDefinitionRequestMMSPdu = constructGetDataDefinitionRequest(lnRef);
		MmsPdu getDataDefinitionResponseMMSPdu = encodeWriteReadDecode(getDataDefinitionRequestMMSPdu);
		return decodeGetDataDefinitionResponse(getDataDefinitionResponseMMSPdu, lnRef, invokeID);
	}

	private MmsPdu constructGetDataDefinitionRequest(ObjectReference lnRef) {

		SubSeq_domain_specific domainSpec = null;

		domainSpec = new SubSeq_domain_specific(new BerVisibleString(lnRef.get(0)), new BerVisibleString(lnRef.get(1)));

		GetVariableAccessAttributesRequest getVariableAccessAttributesRequest = new GetVariableAccessAttributesRequest(
				new ObjectName(null, domainSpec, null));

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, null, null,
				getVariableAccessAttributesRequest, null, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(getInvokeID()),
				confirmedServiceRequest);
		MmsPdu confirmedRequestMMSPdu = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);

		return confirmedRequestMMSPdu;
	}

	private LogicalNode decodeGetDataDefinitionResponse(MmsPdu getDataDefinitionResponseMMSPdu, ObjectReference lnRef,
			int invokeID) throws ServiceError {

		ClientAssociation.testForErrorResponse(getDataDefinitionResponseMMSPdu);

		if (getDataDefinitionResponseMMSPdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataDefinitionResponse: Error decoding GetDataDefinitionResponsePdu");
		}

		ConfirmedResponsePdu confirmedResponsePdu = getDataDefinitionResponseMMSPdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		return DataDefinitionResParser.parseGetDataDefinitionResponse(confirmedResponsePdu, lnRef);
	}

	public ModelNode getDataValues(FcModelNode modelNode) throws ServiceError {
		MmsPdu mmsReq = constructGetDataValuesRequest(modelNode);
		MmsPdu mmsRes = encodeWriteReadDecode(mmsReq);
		return decodeGetDataValuesResponse(mmsRes, modelNode);
	}

	private MmsPdu constructGetDataValuesRequest(FcModelNode modelNode) {
		List<VariableDef> listOfVariables = new ArrayList<VariableDef>(1);
		listOfVariables.add(modelNode.getMmsVariableDef());
		VariableAccessSpecification varAccessSpec = new VariableAccessSpecification(new SubSeqOf_listOfVariable(
				listOfVariables), null);

		ReadRequest readRequest = new ReadRequest(null, varAccessSpec);

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, readRequest, null, null,
				null, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(getInvokeID()),
				confirmedServiceRequest);
		MmsPdu mms = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);
		return mms;
	}

	private ModelNode decodeGetDataValuesResponse(MmsPdu responsePdu, ModelNode modelNode) throws ServiceError {
		testForErrorResponse(responsePdu);

		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding GetDataValuesReponsePdu");
		}

		ConfirmedResponsePdu confirmedResponsePdu = responsePdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.read == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding GetDataValuesReponsePdu");
		}

		List<AccessResult> listOfAccessResults = confirmedServiceResponse.read.listOfAccessResult.seqOf;

		if (listOfAccessResults.size() != 1) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "Multiple results received.");
		}

		AccessResult accRes = listOfAccessResults.iterator().next();

		if (accRes.failure != null) {
			throw new ServiceError(mmsDataAccessErrorToServiceError(accRes.failure),
					"decodeGetDataValuesResponse: DataAccessError received.");
		}
		modelNode.setValueFromMmsDataObj(accRes.success);
		return modelNode;
	}

	public void setDataValues(FcModelNode modelNode) throws ServiceError {
		MmsPdu mmsReq = constructSetDataValuesRequest(modelNode);
		MmsPdu mmsRes = encodeWriteReadDecode(mmsReq);
		decodeSetDataValuesResponse(mmsRes);
	}

	private MmsPdu constructSetDataValuesRequest(FcModelNode modelNode) throws ServiceError {
		List<VariableDef> listOfVariables = new ArrayList<VariableDef>(1);
		listOfVariables.add(modelNode.getMmsVariableDef());
		VariableAccessSpecification varAccessSpec = new VariableAccessSpecification(new SubSeqOf_listOfVariable(
				listOfVariables), null);

		List<Data> listOfData = new LinkedList<Data>();
		Data mmsData = modelNode.getMmsDataObj();
		listOfData.add(mmsData);

		WriteRequest writeRequest = new WriteRequest(varAccessSpec, new SubSeqOf_listOfData(listOfData));

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, null, writeRequest, null,
				null, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(getInvokeID()),
				confirmedServiceRequest);

		return new MmsPdu(confirmedRequestPdu, null, null, null, null, null);
	}

	private void decodeSetDataValuesResponse(MmsPdu setDataValuesRes) throws ServiceError {
		testForErrorResponse(setDataValuesRes);

		// there was no service error but might still be a write failure
		if (setDataValuesRes.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleSetDataValuesResponse: improper response");
		}

		ConfirmedResponsePdu confRes = setDataValuesRes.confirmedResponsePdu;

		if (confRes.confirmedServiceResponse == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleSetDataValuesResponse: improper response");
		}

		ConfirmedServiceResponse confSerRes = confRes.confirmedServiceResponse;

		if (confSerRes.write == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleSetDataValuesResponse: improper response");
		}

		WriteResponse response = confSerRes.write;

		WriteResponse.SubChoice subChoice = response.seqOf.get(0);

		if (subChoice.failure != null) {
			throw new ServiceError(ServiceError.DATA_ACCESS_ERROR);
		}
	}

	/**
	 * This function will get the definition of all persistent DataSets from the
	 * server and update the DataSets in the ServerModel that was returned by
	 * the retrieveModel() function. It will delete DataSets that have been
	 * deleted since the last update and add any new DataSets
	 * 
	 * @throws ServiceError
	 */
	public void updateDataSets() throws ServiceError {

		if (serverModel == null) {
			throw new IllegalStateException(
					"Before calling this function you have to get the ServerModel using the retrieveModel() function");
		}

		Collection<ModelNode> lds = serverModel.getChildren();

		for (ModelNode ld : lds) {
			MmsPdu getDsNamesRequest = constructGetDirectoryRequest(ld.getNodeName(), "", false);
			MmsPdu getDsNamesResponse = encodeWriteReadDecode(getDsNamesRequest);
			decodeAndRetrieveDsNamesAndDefinitions(getDsNamesResponse, (LogicalDevice) ld);
		}
	}

	private void decodeAndRetrieveDsNamesAndDefinitions(MmsPdu getLDDirectoryResponseMMSPdu, LogicalDevice ld)
			throws ServiceError {

		testForErrorResponse(getLDDirectoryResponseMMSPdu);

		if (getLDDirectoryResponseMMSPdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataSetResponse: Error decoding server response");
		}

		ConfirmedResponsePdu confirmedResponsePdu = getLDDirectoryResponseMMSPdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.getNameList == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataSetResponse: Error decoding server response");
		}

		GetNameListResponse getNameListResponse = confirmedServiceResponse.getNameList;

		List<BerVisibleString> identifiers = getNameListResponse.listOfIdentifier.seqOf;

		if (identifiers.size() == 0) {
			return;
		}

		for (BerVisibleString identifier : identifiers) {
			// TODO delete DataSets that no longer exist
			getDataSetDirectory(identifier, ld);
		}

		if (getNameListResponse.moreFollows != null && getNameListResponse.moreFollows.val == true) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT);
		}
	}

	private void getDataSetDirectory(BerVisibleString dsId, LogicalDevice ld) throws ServiceError {
		MmsPdu mmsReq = constructGetDataSetDirectoryRequest(dsId, ld);
		MmsPdu mmsRes = encodeWriteReadDecode(mmsReq);
		decodeGetDataSetDirectoryResponse(mmsRes, dsId, ld);
	}

	private MmsPdu constructGetDataSetDirectoryRequest(BerVisibleString dsId, LogicalDevice ld) throws ServiceError {
		ObjectName dataSetObj = new ObjectName(null, new ObjectName.SubSeq_domain_specific(new BerVisibleString(ld
				.getNodeName().getBytes()), dsId), null);
		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, null, null, null, null,
				dataSetObj, null);
		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(invokeID),
				confirmedServiceRequest);
		MmsPdu mms = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);
		return mms;
	}

	private void decodeGetDataSetDirectoryResponse(MmsPdu responsePdu, BerVisibleString dsId, LogicalDevice ld)
			throws ServiceError {

		testForErrorResponse(responsePdu);

		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataSetDirectoryResponse: Error decoding server response");
		}

		ConfirmedResponsePdu confirmedResponsePdu = responsePdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.getNamedVariableListAttributes == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeGetDataSetDirectoryResponse: Error decoding server response");
		}

		GetNamedVariableListAttributesResponse getNamedVariableListAttResponse = confirmedServiceResponse.getNamedVariableListAttributes;
		boolean deletable = getNamedVariableListAttResponse.mmsDeletable.val;
		List<VariableDef> variables = getNamedVariableListAttResponse.listOfVariable.seqOf;

		if (variables.size() == 0) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"decodeGetDataSetDirectoryResponse: Instance not available");
		}

		List<FcModelNode> dsMems = new ArrayList<FcModelNode>();

		for (VariableDef variableDef : variables) {

			FcModelNode member = serverModel.getNodeFromVariableDef(variableDef);
			if (member == null) {
				throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
						"decodeGetDataSetDirectoryResponse: data set memeber does not exist, call getDataDefinition first");
			}
			dsMems.add(member);
		}

		String dsObjRef = ld.getNodeName() + "/" + dsId.toString().replace('$', '.');

		DataSet dataSet = new DataSet(dsObjRef, dsMems, deletable);

		if (ld.getChild(dsId.toString().substring(0, dsId.toString().indexOf('$'))) == null) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"decodeGetDataSetDirectoryResponse: LN for returned DataSet is not available");
		}

		DataSet existingDs = serverModel.getDataSet(dsObjRef);
		if (existingDs == null) {
			serverModel.addDataSet(dataSet);
		}
		else if (!existingDs.isDeletable()) {
			return;
		}
		else {
			serverModel.removeDataSet(dsObjRef.toString());
			serverModel.addDataSet(dataSet);
		}

	}

	/**
	 * The client should create the data set first and add it to either the
	 * nonpersistent list or to the model. Then it should call this method for
	 * creation on the server side
	 */
	public void createDataSet(DataSet dataSet) throws ServiceError {
		MmsPdu mmsReq = constructCreateDataSetRequest(dataSet);
		MmsPdu mmsRes = encodeWriteReadDecode(mmsReq);
		handleCreateDataSetResponse(mmsRes, dataSet);
	}

	/**
	 * dsRef = either LD/LN.DataSetName (persistent) or @DataSetname
	 * (non-persistent) Names in dsMemberRef should be in the form:
	 * LD/LNName.DoName or LD/LNName.DoName.DaName
	 */
	private MmsPdu constructCreateDataSetRequest(DataSet dataSet) throws ServiceError {
		List<VariableDef> listOfVariable = new LinkedList<VariableDef>();

		for (FcModelNode dsMember : dataSet) {
			listOfVariable.add(dsMember.getMmsVariableDef());
		}

		DefineNamedVariableListRequest.SubSeqOf_listOfVariable seqOf = new DefineNamedVariableListRequest.SubSeqOf_listOfVariable(
				listOfVariable);
		DefineNamedVariableListRequest createDSRequest = new DefineNamedVariableListRequest(dataSet.getMmsObjectName(),
				seqOf);

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, null, null, null,
				createDSRequest, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(invokeID),
				confirmedServiceRequest);

		MmsPdu mms = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);

		return mms;

	}

	private void handleCreateDataSetResponse(MmsPdu responsePdu, DataSet dataSet) throws ServiceError {
		testForErrorResponse(responsePdu);
		serverModel.addDataSet(dataSet);
	}

	public void deleteDataSet(DataSet dataSet) throws ServiceError {
		MmsPdu request = constructDeleteDataSetRequest(dataSet);
		MmsPdu response = encodeWriteReadDecode(request);
		decodeDeleteDataSetResponse(response, dataSet);
	}

	private MmsPdu constructDeleteDataSetRequest(DataSet dataSet) throws ServiceError {
		List<ObjectName> listOfVariableListName = new ArrayList<ObjectName>(1);
		listOfVariableListName.add(dataSet.getMmsObjectName());

		DeleteNamedVariableListRequest requestDeleteDS = new DeleteNamedVariableListRequest(null,
				new SubSeqOf_listOfVariableListName(listOfVariableListName), null);

		ConfirmedServiceRequest requestService = new ConfirmedServiceRequest(null, null, null, null, null, null,
				requestDeleteDS);

		ConfirmedRequestPdu request = new ConfirmedRequestPdu(new BerInteger(invokeID), requestService);

		MmsPdu mmsRequest = new MmsPdu(request, null, null, null, null, null);

		return mmsRequest;
	}

	private void decodeDeleteDataSetResponse(MmsPdu responsePdu, DataSet dataSet) throws ServiceError {
		testForErrorResponse(responsePdu);

		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeDeleteDataSetResponse: Error decoding server response");
		}

		ConfirmedResponsePdu confirmedResponsePdu = responsePdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.deleteNamedVariableList == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"decodeDeleteDataSetResponse: Error decoding server response");
		}

		DeleteNamedVariableListResponse deleteNamedVariableListResponse = confirmedServiceResponse.deleteNamedVariableList;

		if (deleteNamedVariableListResponse.numberDeleted.val != 1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "number deleted not 1");
		}

		if (serverModel.removeDataSet(dataSet.getReferenceStr()) == null) {
			throw new ServiceError(ServiceError.DATA_ACCESS_ERROR, "unable to delete dataset locally");
		}

	}

	public List<ServiceError> getDataSetValues(DataSet dataSet) throws ServiceError {
		MmsPdu request = constructGetDataSetValuesRequest(dataSet);
		MmsPdu responsePdu = encodeWriteReadDecode(request);
		return decodeGetDataSetValuesResponse(responsePdu, dataSet);
	}

	private MmsPdu constructGetDataSetValuesRequest(DataSet dataSet) throws ServiceError {

		VariableAccessSpecification varAccSpec = new VariableAccessSpecification(null, dataSet.getMmsObjectName());
		ReadRequest getDataSetValuesRequest = new ReadRequest(new BerBoolean(true), varAccSpec);
		ConfirmedServiceRequest requestService = new ConfirmedServiceRequest(null, getDataSetValuesRequest, null, null,
				null, null, null);
		ConfirmedRequestPdu request = new ConfirmedRequestPdu(new BerInteger(invokeID), requestService);
		MmsPdu mmsRequest = new MmsPdu(request, null, null, null, null, null);
		return mmsRequest;
	}

	private List<ServiceError> decodeGetDataSetValuesResponse(MmsPdu responsePdu, DataSet ds) throws ServiceError {

		testForErrorResponse(responsePdu);

		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding GetDataValuesReponsePdu");
		}

		ConfirmedResponsePdu confirmedResponsePdu = responsePdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.read == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding GetDataValuesReponsePdu");
		}

		ReadResponse readResponse = confirmedServiceResponse.read;
		List<AccessResult> listOfAccessResults = readResponse.listOfAccessResult.seqOf;

		if (listOfAccessResults.size() != ds.getMembers().size()) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
					"Number of AccessResults does not match the number of DataSet members.");
		}

		Iterator<AccessResult> accessResultIterator = listOfAccessResults.iterator();

		List<ServiceError> serviceErrors = new ArrayList<ServiceError>(listOfAccessResults.size());

		for (FcModelNode dsMember : ds) {
			AccessResult accessResult = accessResultIterator.next();
			if (accessResult.success != null) {
				dsMember.setValueFromMmsDataObj(accessResult.success);
				serviceErrors.add(null);
			}
			else {
				for (BasicDataAttribute bda : dsMember.getBasicDataAttributes()) {
					bda.setValue(null);
				}
				serviceErrors.add(new ServiceError(mmsDataAccessErrorToServiceError(accessResult.failure)));
			}
		}

		return serviceErrors;
	}

	public List<ServiceError> setDataSetValues(DataSet dataSet) throws ServiceError {
		MmsPdu mmsRequest = constructSetDataSetValues(dataSet);
		MmsPdu mmsResponse = encodeWriteReadDecode(mmsRequest);
		return decodeSetDataSetValuesResponse(mmsResponse);
	}

	private MmsPdu constructSetDataSetValues(DataSet dataSet) throws ServiceError {
		VariableAccessSpecification varAccSpec = new VariableAccessSpecification(null, dataSet.getMmsObjectName());

		List<Data> listOfData = new LinkedList<Data>();

		for (ModelNode member : dataSet) {
			listOfData.add(member.getMmsDataObj());
		}

		WriteRequest setDataSetValuesRequest = new WriteRequest(varAccSpec, new SubSeqOf_listOfData(listOfData));
		ConfirmedServiceRequest requestService = new ConfirmedServiceRequest(null, null, setDataSetValuesRequest, null,
				null, null, null);
		ConfirmedRequestPdu request = new ConfirmedRequestPdu(new BerInteger(invokeID), requestService);
		MmsPdu mmsRequest = new MmsPdu(request, null, null, null, null, null);
		return mmsRequest;
	}

	private List<ServiceError> decodeSetDataSetValuesResponse(MmsPdu responsePdu) throws ServiceError {
		testForErrorResponse(responsePdu);

		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding SetDataSetValuesReponsePdu");
		}

		ConfirmedResponsePdu confirmedResponsePdu = responsePdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.write == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding SetDataSetValuesReponsePdu");
		}

		WriteResponse writeResponse = confirmedServiceResponse.write;
		List<WriteResponse.SubChoice> writeResChoiceType = writeResponse.seqOf;
		List<ServiceError> serviceErrors = new ArrayList<ServiceError>(writeResChoiceType.size());

		for (WriteResponse.SubChoice accessResult : writeResChoiceType) {
			if (accessResult.success != null) {
				serviceErrors.add(null);
			}
			else {
				serviceErrors.add(new ServiceError(mmsDataAccessErrorToServiceError(accessResult.failure)));
			}
		}
		return serviceErrors;

	}

	public void getRCBValues(ReportControlBlock rcb) throws ServiceError {
		MmsPdu mmsReq = constructGetRCBValuesRequest(rcb);
		MmsPdu mmsRes = encodeWriteReadDecode(mmsReq);
		decodeGetRCBValuesResponse(mmsRes, rcb);
	}

	private MmsPdu constructGetRCBValuesRequest(ReportControlBlock rcb) {
		ObjectReference objRef = rcb.getReference();
		ObjectName objectName = null;
		ObjectName.SubSeq_domain_specific subSeq = null;
		String funcCon = "";

		if (rcb instanceof UnbufferedReportContrlBlock) {
			funcCon = "$RP$";
		}
		else if (rcb instanceof BufferedReportControlBlock) {
			funcCon = "$BR$";
		}

		subSeq = new ObjectName.SubSeq_domain_specific(new BerVisibleString(objRef.get(0)), new BerVisibleString(
				objRef.get(1) + funcCon + objRef.get(2)));

		objectName = new ObjectName(null, subSeq, null);

		VariableSpecification varSpec = new VariableSpecification(objectName);

		VariableDef seq = new VariableDef(varSpec, null);

		List<VariableDef> listOfVariables = new ArrayList<VariableDef>(1);
		listOfVariables.add(seq);

		VariableAccessSpecification varAccessSpec = new VariableAccessSpecification(new SubSeqOf_listOfVariable(
				listOfVariables), null);

		ReadRequest readRequest = new ReadRequest(new BerBoolean(false), varAccessSpec);

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, readRequest, null, null,
				null, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(getInvokeID()),
				confirmedServiceRequest);
		MmsPdu mms = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);
		return mms;
	}

	private void decodeGetRCBValuesResponse(MmsPdu responsePdu, ReportControlBlock rcb) throws ServiceError {
		// data in the msg should correspond to order of children
		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding GetDataValuesReponsePdu");
		}

		ConfirmedResponsePdu confirmedResponsePdu = responsePdu.confirmedResponsePdu;
		checkInvokeID(confirmedResponsePdu);

		ConfirmedServiceResponse confirmedServiceResponse = confirmedResponsePdu.confirmedServiceResponse;
		if (confirmedServiceResponse.read == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Error decoding GetDataValuesReponsePdu");
		}

		ReadResponse readResponse = confirmedServiceResponse.read;
		List<AccessResult> listOfAccessResults = readResponse.listOfAccessResult.seqOf;

		if (listOfAccessResults.size() != 1) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "Multiple results received.");
		}

		AccessResult accRes = listOfAccessResults.iterator().next();
		if (accRes.failure == null) {

			List<Data> results = accRes.success.structure.seqOf;
			if (results.size() != rcb.getChildren().size()) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "Too many results received");
			}

			int i = 0;

			for (ModelNode child : rcb.getChildren()) {
				// since convertMmsDataToDataContainer processes structures
				// based on the FunctionalConstraint
				// it does not work here. Therefore need to convert each Data
				// separately

				child.setValueFromMmsDataObj(results.get(i++));

				// TODO was commented out:
				// MmsAsn1ComplexTypeConverter.convertMmsDataToDataContainer(child,
				// null, results.get(i++), mmsFCs);
			}

		}
		else {
			throw new ServiceError(mmsDataAccessErrorToServiceError(accRes.failure),
					"decodeGetDataValuesResponse: DataAccessError received.");
		}
	}

	public void setRCBValues(ReportControlBlock rcb) throws ServiceError {
		// sent for each value of the report control block

		DaBoolean rptEna = (DaBoolean) rcb.getChild("RptEna");
		boolean oldValue = rptEna.getValue();
		if (oldValue == true) {
			mmsScsmClientReceiver.enableReportQueueing();
		}
		else {
			mmsScsmClientReceiver.disableReportQueueing();
		}

		// need to send RptEna = false first - otherwise cannot change values
		rptEna.setValue(false);

		MmsPdu mmsReq = constructSetRCBValuesRequest(rptEna, rcb);
		MmsPdu mmsRes = encodeWriteReadDecode(mmsReq);
		decodeSetRCBValuesResponse(mmsRes);

		for (ModelNode child : rcb.getChildren()) {
			// these appear to only be set by the server
			if (!child.getNodeName().equals("SqNum") && !child.getNodeName().equals("ConfRev")
					&& !child.getNodeName().equals("RptEna")) {

				mmsReq = constructSetRCBValuesRequest(child, rcb);
				mmsRes = encodeWriteReadDecode(mmsReq);
				decodeSetRCBValuesResponse(mmsRes);

			}
		}

		if (oldValue == true) {
			rptEna.setValue(true);
			mmsReq = constructSetRCBValuesRequest(rptEna, rcb);
			mmsRes = encodeWriteReadDecode(mmsReq);
			decodeSetRCBValuesResponse(mmsRes);
		}

	}

	private MmsPdu constructSetRCBValuesRequest(ModelNode node, ReportControlBlock rcb) throws ServiceError {
		ObjectReference objRef = rcb.getReference();
		ObjectName objectName = null;
		ObjectName.SubSeq_domain_specific subSeq = null;
		String funcCon = "";

		if (rcb instanceof UnbufferedReportContrlBlock) {
			funcCon = "$RP$";
		}
		else if (rcb instanceof BufferedReportControlBlock) {
			funcCon = "$BR$";
		}

		subSeq = new ObjectName.SubSeq_domain_specific(new BerVisibleString(objRef.get(0)), new BerVisibleString(
				objRef.get(1) + funcCon + objRef.get(2) + "$" + node.getNodeName()));

		objectName = new ObjectName(null, subSeq, null);

		VariableSpecification varSpec = new VariableSpecification(objectName);

		List<VariableDef> listOfVariable = new LinkedList<VariableDef>();
		VariableDef varSeq = new VariableDef(varSpec, null);
		listOfVariable.add(varSeq);

		VariableAccessSpecification variableAccessSpec = new VariableAccessSpecification(new SubSeqOf_listOfVariable(
				listOfVariable), null);

		List<Data> listOfData = new LinkedList<Data>();
		Data mmsData = node.getMmsDataObj();
		listOfData.add(mmsData);

		WriteRequest writeRequest = new WriteRequest(variableAccessSpec, new SubSeqOf_listOfData(listOfData));

		ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest(null, null, writeRequest, null,
				null, null, null);

		ConfirmedRequestPdu confirmedRequestPdu = new ConfirmedRequestPdu(new BerInteger(getInvokeID()),
				confirmedServiceRequest);

		MmsPdu mms = new MmsPdu(confirmedRequestPdu, null, null, null, null, null);

		return mms;
	}

	private void decodeSetRCBValuesResponse(MmsPdu responsePdu) throws ServiceError {
		testForErrorResponse(responsePdu);

		if (responsePdu.confirmedResponsePdu == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleSetDataValuesResponse: improper response");
		}

		ConfirmedResponsePdu confRes = responsePdu.confirmedResponsePdu;

		if (confRes.confirmedServiceResponse == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleSetDataValuesResponse: improper response");
		}

		ConfirmedServiceResponse confSerRes = confRes.confirmedServiceResponse;

		if (confSerRes.write == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleSetDataValuesResponse: improper response");
		}

		WriteResponse response = confSerRes.write;

		// only one?
		WriteResponse.SubChoice subChoice = response.seqOf.get(0);

		if (subChoice.failure != null) {
			throw new ServiceError(ServiceError.DATA_ACCESS_ERROR);
		}

	}

	public Report getReport() throws ServiceError {
		MmsPdu mmsPdu = null;
		try {
			mmsPdu = incomingReports.take();
		} catch (InterruptedException e) {
		}

		if (mmsPdu.unconfirmedPDU == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"getReport: Error decoding server response");
		}

		UnconfirmedPDU unconfirmedRes = mmsPdu.unconfirmedPDU;

		if (unconfirmedRes.unconfirmedService == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"getReport: Error decoding server response");
		}

		UnconfirmedService unconfirmedServ = unconfirmedRes.unconfirmedService;

		if (unconfirmedServ.informationReport == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"getReport: Error decoding server response");
		}

		return processReport(unconfirmedServ.informationReport);
	}

	public Report getReport(int timeout) throws ServiceError {
		MmsPdu mmsPdu = null;
		try {
			mmsPdu = incomingReports.poll(timeout, TimeUnit.MILLISECONDS);
			if (mmsPdu == null) {
				return null;
			}
		} catch (InterruptedException e) {
		}
		if (mmsPdu.unconfirmedPDU == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"getReport: Error decoding server response");
		}

		UnconfirmedPDU unconfirmedRes = mmsPdu.unconfirmedPDU;

		if (unconfirmedRes.unconfirmedService == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"getReport: Error decoding server response");
		}

		UnconfirmedService unconfirmedServ = unconfirmedRes.unconfirmedService;

		if (unconfirmedServ.informationReport == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"getReport: Error decoding server response");
		}

		// TODO was commented out:
		// return processReport(unconfirmedServ.informationReport);
		return null;
	}

	/**
	 * See iec61850-8-1 Table 64 for the order of the report information To get
	 * the updated values, use the getDataSet method for reports
	 * 
	 * @param rpt
	 * @return
	 */
	private Report processReport(InformationReport rpt) throws ServiceError {
		List<AccessResult> listRes = rpt.listOfAccessResult.seqOf;
		int index = 0;

		if (listRes.get(index).success.visible_string == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"processReport: report does not contain RptID");
		}

		String rptID = listRes.get(index++).success.visible_string.toString();

		if (listRes.get(index).success.bit_string == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"processReport: report does not contain OptFlds");
		}

		OptFields optFlds = new OptFields(new ObjectReference("temp"));
		optFlds.setValue(listRes.get(index++).success.bit_string.bitString);

		Report report = new Report();

		if (optFlds.isSeqNum()) {
			report.setSqNum((int) listRes.get(index++).success.unsigned.val);
		}

		if (optFlds.isTimeStamp()) {
			DaEntryTime time = new DaEntryTime(new ObjectReference(rptID), null, "", null, false, false);
			time.setValueFromMmsDataObj(listRes.get(index++).success);
			report.setTimeOfEntry(time);
		}

		if (optFlds.isDataSet()) {
			report.setDataSetRef(listRes.get(index++).success.visible_string.toString().replace('$', '.'));
		}

		if (optFlds.isBufOvfl()) {
			report.setBufOvfl(listRes.get(index++).success.boolean_.val);
		}

		if (optFlds.isEntryId()) {
			// TODO change report.EntryId to OctetString
			// report.setEntryId(listRes.get(index++).integer.val);
			byte[] b = listRes.get(index++).success.octet_string.octetString;
			long val = 0;
			for (int i = 0; i < b.length; i++) {
				val += (b[i] & 0xff) << (8 * i);
			}
			report.setEntryId(val);
		}

		if (optFlds.isConfigRef()) {
			// TODO change report.convRev to unsigned instead of integer?
			report.setConvRev(listRes.get(index++).success.unsigned.val);
		}

		if (optFlds.isSegmentation()) {
			report.setSubSqNum((int) listRes.get(index++).success.unsigned.val);
			report.setMoreSegmentsFollow(listRes.get(index++).success.boolean_.val);
		}

		byte[] inclusion = listRes.get(index++).success.bit_string.bitString;
		report.setInclusionBitString(inclusion);

		if (optFlds.isDataRef()) {
			// this is just to move the index to the right place
			// The next part will process the changes to the values
			// without the dataRefs
			for (int i = 0; i < inclusion.length * 8; i++) {
				if ((inclusion[i / 8] & (1 << (7 - i % 8))) == (1 << (7 - i % 8))) {
					index++;
				}
			}
		}

		// updating of data set copy - original stays the same
		DataSet ds = serverModel.getDataSet(report.getDataSetRef()).copy();
		int shiftNum = 0;
		int numChanged = 0;

		for (ModelNode child : ds.getMembers()) {
			if ((inclusion[shiftNum / 8] & (1 << (7 - shiftNum % 8))) == (1 << (7 - shiftNum % 8))) {

				AccessResult accessRes = listRes.get(index++);
				child.setValueFromMmsDataObj(accessRes.success);

				numChanged++;
			}
			shiftNum++;
		}

		report.setDataSet(ds);

		if (optFlds.isReasonCode()) {
			List<ReasonCode> reasonCodes = new ArrayList<ReasonCode>();
			for (int i = 0; i < numChanged; i++) {

				byte[] reason = listRes.get(index++).success.bit_string.bitString;

				if ((reason[0] & 0x02) == 0x02) {
					reasonCodes.add(ReasonCode.DCHG);
				}
				else if ((reason[0] & 0x04) == 0x04) {
					reasonCodes.add(ReasonCode.QCHG);
				}
				else if ((reason[0] & 0x08) == 0x08) {
					reasonCodes.add(ReasonCode.DUPD);
				}
				else if ((reason[0] & 0x10) == 0x10) {
					reasonCodes.add(ReasonCode.INTEGRITY);
				}
				else if ((reason[0] & 0x20) == 0x20) {
					reasonCodes.add(ReasonCode.GI);
				}
				else if ((reason[0] & 0x40) == 0x40) {
					reasonCodes.add(ReasonCode.APPTRIGGER);
				}
			}
		}

		return report;
	}

	/**
	 * will close the connection simply by closing the socket (i.e. the TCP
	 * connection)
	 */
	public void close() {
		mmsScsmClientReceiver.close();
	}

	/**
	 * will send a disconnect request first and then close the socket (i.e. the
	 * TCP connection)
	 */
	public void disconnect() {
		mmsScsmClientReceiver.disconnect();
	}

}
