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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.openiec61850.common.model.report.BufferedReportControlBlock;
import org.openiec61850.common.model.report.Report;
import org.openiec61850.common.model.report.ReportControlBlock;
import org.openiec61850.common.model.report.UnbufferedReportContrlBlock;
import org.openiec61850.jmms.iso.acse.AcseAssociation;
import org.openiec61850.jmms.mms.asn1.AccessResult;
import org.openiec61850.jmms.mms.asn1.ConfirmedRequestPdu;
import org.openiec61850.jmms.mms.asn1.ConfirmedResponsePdu;
import org.openiec61850.jmms.mms.asn1.ConfirmedServiceRequest;
import org.openiec61850.jmms.mms.asn1.ConfirmedServiceResponse;
import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.DefineNamedVariableListRequest;
import org.openiec61850.jmms.mms.asn1.DeleteNamedVariableListRequest;
import org.openiec61850.jmms.mms.asn1.DeleteNamedVariableListResponse;
import org.openiec61850.jmms.mms.asn1.GetNameListRequest;
import org.openiec61850.jmms.mms.asn1.GetNameListResponse;
import org.openiec61850.jmms.mms.asn1.GetNameListResponse.SubSeqOf_listOfIdentifier;
import org.openiec61850.jmms.mms.asn1.GetNamedVariableListAttributesResponse;
import org.openiec61850.jmms.mms.asn1.GetNamedVariableListAttributesResponse.SubSeqOf_listOfVariable;
import org.openiec61850.jmms.mms.asn1.GetVariableAccessAttributesRequest;
import org.openiec61850.jmms.mms.asn1.GetVariableAccessAttributesResponse;
import org.openiec61850.jmms.mms.asn1.InitResponseDetail;
import org.openiec61850.jmms.mms.asn1.InitiateRequestPdu;
import org.openiec61850.jmms.mms.asn1.InitiateResponsePdu;
import org.openiec61850.jmms.mms.asn1.MmsPdu;
import org.openiec61850.jmms.mms.asn1.ObjectName;
import org.openiec61850.jmms.mms.asn1.ObjectName.SubSeq_domain_specific;
import org.openiec61850.jmms.mms.asn1.ReadRequest;
import org.openiec61850.jmms.mms.asn1.ReadResponse;
import org.openiec61850.jmms.mms.asn1.ReadResponse.SubSeqOf_listOfAccessResult;
import org.openiec61850.jmms.mms.asn1.ServiceError.SubChoice_errorClass;
import org.openiec61850.jmms.mms.asn1.StructComponent;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_structure;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_structure.SubSeqOf_components;
import org.openiec61850.jmms.mms.asn1.VariableAccessSpecification;
import org.openiec61850.jmms.mms.asn1.VariableDef;
import org.openiec61850.jmms.mms.asn1.WriteRequest;
import org.openiec61850.jmms.mms.asn1.WriteResponse;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerBoolean;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerNull;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the SCSMConnectionHandler. One MMSConnectionHandler is created for
 * every new Association Request.
 * 
 */
final class ConnectionHandler {

	private static final int DEFAULT_PROPOSED_MAX_SERV_OUTSTANDING_CALLING = 5;
	private static final int DEFAULT_PROPOSED_MAX_SERV_OUTSTANDING_CALLED = 5;
	private static final int DEFAULT_PROPOSED_DATA_STRUCTURE_NESTING_LEVEL = 10;
	private static final int DEFAULT_MAX_Pdu_SIZE = 65000;
	private static final int MINIMUM_Pdu_SIZE = 1000;

	private static Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

	private AcseAssociation pConnection = null;
	private boolean associationStarted = false;

	private int proposedMaxPduSize = DEFAULT_MAX_Pdu_SIZE;
	// TODO the following 3 parameters are ignored at the moment
	private final int proposedMaxServOutstandingCalling = DEFAULT_PROPOSED_MAX_SERV_OUTSTANDING_CALLING;
	private final int proposedMaxServOutstandingCalled = DEFAULT_PROPOSED_MAX_SERV_OUTSTANDING_CALLED;
	private final int proposedDataStructureNestingLevel = DEFAULT_PROPOSED_DATA_STRUCTURE_NESTING_LEVEL;

	private int negotiatedMaxServOutstandingCalling;
	private int negotiatedMaxServOutstandingCalled;
	private int negotiatedDataStructureNestingLevel;
	private long negotiatedMaxPduSize;

	private final AccessPoint accessPoint;

	private boolean notifyAssociationOfClosedSocket;

	private boolean insertRef;
	String continueAfter;

	// The following FCs are not part of this enum because they are not really
	// FCs and only defined in part 8-1:
	// RP (report), LG (log), BR (buffered report), GO, GS, MS, US

	private static String[] mmsFCs = { "MX", "ST", "CO", "CF", "DC", "SP", "SG", "RP", "LG", "BR", "GO", "GS", "SV",
			"SE", "EX", "SR", "OR", "BL" };

	public ConnectionHandler(AccessPoint accessPoint) {
		this.accessPoint = accessPoint;

	}

	/**
	 * Set the maximum MMS Pdu size in bytes. The default size is 65000.
	 * 
	 * @param maxPduSize
	 *            should be at least 1000
	 */
	public void setMaxPduSize(int maxPduSize) {
		if (associationStarted) {
			throw new RuntimeException("MaxPduSize cannot be changed once the association has been started");
		}
		if (maxPduSize >= MINIMUM_Pdu_SIZE) {
			this.proposedMaxPduSize = maxPduSize;
		}
		else {
			throw new IllegalArgumentException("Maximum size is too small");
		}
	}

	public void connectionIndication(AcseAssociation acseAssociation, ByteBuffer associationRequest)
			throws ServiceError, IOException {

		logger.debug("someone connected");

		this.pConnection = acseAssociation;

		associate(acseAssociation, associationRequest);
		associationStarted = true;

		try {
			handleConnection(acseAssociation);
		} finally {
			associationStarted = false;
		}
	}

	private void associate(AcseAssociation acseAssociation, ByteBuffer associationRequest) throws ServiceError {

		MmsPdu mmsPdu = new MmsPdu();

		ByteArrayInputStream iStream = new ByteArrayInputStream(associationRequest.array(),
				associationRequest.arrayOffset() + associationRequest.position(), associationRequest.limit()
						- associationRequest.position());
		try {
			mmsPdu.decode(iStream, null);
		} catch (IOException e1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "Error decoding PDU");
		}

		// TODO association needs to be processed
		if (!processAssociationRequest(mmsPdu.initiateRequestPdu)) {
			throw new ServiceError(ServiceError.FATAL);
		}
		else {

			// TODO check permissions?
			MmsPdu initiateResponseMMSpdu = constructAssociationResponsePdu();

			BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(500, true);
			try {
				initiateResponseMMSpdu.encode(berOStream, true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new ServiceError(0);
			}

			try {
				acseAssociation.accept(berOStream.getByteBuffer());
			} catch (IOException e) {
				acseAssociation.close();
			}
		}

	}

	private boolean processAssociationRequest(InitiateRequestPdu associationRequestMMSpdu) {

		if (associationRequestMMSpdu.localDetailCalling != null) {
			negotiatedMaxPduSize = (int) associationRequestMMSpdu.localDetailCalling.val;
		}
		if (negotiatedMaxPduSize > proposedMaxPduSize) {
			negotiatedMaxPduSize = proposedMaxPduSize;
		}
		else if (negotiatedMaxPduSize < MINIMUM_Pdu_SIZE) {
			negotiatedMaxPduSize = MINIMUM_Pdu_SIZE;
		}

		negotiatedMaxServOutstandingCalling = (int) associationRequestMMSpdu.proposedMaxServOutstandingCalling.val;

		if (negotiatedMaxServOutstandingCalling > proposedMaxServOutstandingCalling) {
			negotiatedMaxServOutstandingCalling = proposedMaxServOutstandingCalling;
		}

		negotiatedMaxServOutstandingCalled = (int) associationRequestMMSpdu.proposedMaxServOutstandingCalled.val;

		if (negotiatedMaxServOutstandingCalled > proposedMaxServOutstandingCalled) {
			negotiatedMaxServOutstandingCalled = proposedMaxServOutstandingCalled;
		}

		if (associationRequestMMSpdu.proposedDataStructureNestingLevel != null) {
			negotiatedDataStructureNestingLevel = (int) associationRequestMMSpdu.proposedDataStructureNestingLevel.val;

		}
		if (negotiatedDataStructureNestingLevel > proposedDataStructureNestingLevel) {
			negotiatedDataStructureNestingLevel = proposedDataStructureNestingLevel;
		}

		if (negotiatedMaxServOutstandingCalling < 0 || negotiatedMaxServOutstandingCalled < 0
				|| negotiatedDataStructureNestingLevel < 0) {
			return false;
		}

		return true;

	}

	private MmsPdu constructAssociationResponsePdu() {

		// TODO what are the CBB Parameter?
		// byte[] negotiatedParameterCbbBitString = new byte[] { (byte) (0xf1 &
		// 0xff), 0x00 };

		byte[] negotiatedParameterCbbBitString = new byte[] { (byte) (0xfb), 0x00 };

		// byte[] servicesSupportedCalledBitString = new byte[] { 0x40, 0x00,
		// 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		// 0x00, 0x00 };
		// set getNameList

		byte[] servicesSupportedCalledBitString = new byte[] { (byte) 0xee, (byte) 0x1c, 0x00, 0x00, 0x04, 0x08, 0x00,
				0x00, 0x79, (byte) 0xef, 0x18 };

		InitResponseDetail initRespDetail = new InitResponseDetail(new BerInteger(1), new BerBitString(
				negotiatedParameterCbbBitString, negotiatedParameterCbbBitString.length * 8 - 5), new BerBitString(
				servicesSupportedCalledBitString, servicesSupportedCalledBitString.length * 8 - 3));

		InitiateResponsePdu initRespPdu = new InitiateResponsePdu(new BerInteger(negotiatedMaxPduSize), new BerInteger(
				negotiatedMaxServOutstandingCalling), new BerInteger(negotiatedMaxServOutstandingCalled),
				new BerInteger(negotiatedDataStructureNestingLevel), initRespDetail);

		MmsPdu initiateResponseMMSpdu = new MmsPdu(null, null, null, null, initRespPdu, null);

		return initiateResponseMMSpdu;
	}

	private void handleConnection(AcseAssociation acseAssociation) throws IOException {
		while (true) {
			try {
				MmsPdu mmsRequestPdu = readAnMmsPdu(acseAssociation);
				if (mmsRequestPdu == null) {
					return;
				}

				if (mmsRequestPdu.confirmedRequestPdu == null) {
					throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
							"Got an invalid MMS packet: is not an ConfirmedRequestPdu");
				}

				ConfirmedRequestPdu confirmedRequestPdu = mmsRequestPdu.confirmedRequestPdu;
				ConfirmedServiceRequest confirmedServiceRequest = confirmedRequestPdu.confirmedServiceRequest;

				ConfirmedServiceResponse confirmedServiceResponse = null;

				if (confirmedServiceRequest.getNameList != null) {

					GetNameListRequest getNameListRequest = confirmedServiceRequest.getNameList;
					GetNameListResponse response = null;

					if (getNameListRequest.objectClass.basicObjectClass == null) {
						throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
								"Got an invalid MMS packet: ObjectClass was not selected in GetNameList request");
					}

					long basicObjectClass = getNameListRequest.objectClass.basicObjectClass.val;
					if (basicObjectClass == 9) {
						logger.debug("Got a GetServerDirectory (MMS GetNameList[DOMAIN]) request");
						response = handleGetServerDirectoryRequest(getNameListRequest);
					}
					else if (basicObjectClass == 0) {
						logger.debug("Got a Get{LD|LN}Directory (MMS GetNameList[NAMED_VARIABLE]) request");
						response = handleGetDirectoryRequest(getNameListRequest);
					}
					else if (basicObjectClass == 2) {
						logger.debug("Got a GetLogicalNodeDirectory[DataSet] (MMS GetNameList[NAMED_VARIABLE_LIST]) request");
						response = handleGetDataSetNamesRequest(getNameListRequest);
					}
					// else if (basicObjectClass == 8) {
					// logger.debug("Got a GetLogicalNodeDirectory[Log] (MMS GetNameList[JOURNAL]) request");
					// response =
					// handleGetNameListJournalRequest(getNameListRequest);
					// }

					confirmedServiceResponse = new ConfirmedServiceResponse(response, null, null, null, null, null,
							null);

				}

				else if (confirmedServiceRequest.getVariableAccessAttributes != null) {
					logger.debug("Got a GetVariableAccessAttributes request (GetDataDirectory/GetDataDefinition)");
					GetVariableAccessAttributesResponse response = handleGetVariableAccessAttributesRequest(

					confirmedServiceRequest.getVariableAccessAttributes);
					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, response, null, null,
							null);

				}
				else if (confirmedServiceRequest.read != null) {
					// both getDataValues and getDataSetValues map to this
					ReadResponse response = handleGetDataValuesRequest(confirmedServiceRequest.read);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, response, null, null, null, null,
							null);
				}
				else if (confirmedServiceRequest.write != null) {
					logger.debug("Got a Write request");

					WriteResponse response = handleSetDataValuesRequest(confirmedServiceRequest.write);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, response, null, null, null,
							null);

				}

				// for Data Sets
				else if (confirmedServiceRequest.defineNamedVariableList != null) {
					logger.debug("Got a CreateDataSet request");

					BerNull response = handleCreateDataSetRequest(confirmedServiceRequest.defineNamedVariableList);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, null, response, null,
							null);
				}

				else if (confirmedServiceRequest.getNamedVariableListAttributes != null) {
					logger.debug("Got a GetDataSetDirectory request");
					GetNamedVariableListAttributesResponse response = handleGetDataSetDirectoryRequest(confirmedServiceRequest.getNamedVariableListAttributes);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, null, null, response,
							null);

				}

				else if (confirmedServiceRequest.deleteNamedVariableList != null) {
					logger.debug("Got a DeleteDataSet request");
					DeleteNamedVariableListResponse response = handleDeleteDataSetRequest(confirmedServiceRequest.deleteNamedVariableList);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, null, null, null,
							response);
				}

				else {
					throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
							"invalid MMS packet: unknown request type.");
				}

				ConfirmedResponsePdu confirmedResponsePdu = new ConfirmedResponsePdu(confirmedRequestPdu.invokeID,
						confirmedServiceResponse);

				MmsPdu mmsResponsePdu = new MmsPdu(null, confirmedResponsePdu, null, null, null, null);
				sendAnMmsPdu(acseAssociation, mmsResponsePdu);
			}

			catch (ServiceError e) {
				logger.warn(e.getMessage());
				sendAnMmsPdu(acseAssociation, createServiceErrorResponse(e));
			}
		}
	}

	private void sendAnMmsPdu(AcseAssociation acseAssociation, MmsPdu mmsResponsePdu) throws IOException {
		try {
			// byte[] mmsResponsePdubytes = encodeMMSpdu(mmsResponsePdu);
			BerByteArrayOutputStream baos = new BerByteArrayOutputStream(500, true);
			mmsResponsePdu.encode(baos, false);
			// pConnection.send(baos.getArray());
			acseAssociation.send(baos.getByteBuffer());
		} catch (IOException e) {
			acseAssociation.close();
			throw e;
		}
	}

	private MmsPdu readAnMmsPdu(AcseAssociation acseAssociation) throws ServiceError {

		MmsPdu mmsRequestPdu = null;
		ByteBuffer rcvdPdu = null;
		try {
			rcvdPdu = acseAssociation.receive();
		} catch (IOException e) {
			// accessPoint.abort("Socket was closed.");
			logger.debug("IOException reading, client probably closed socket. closing ConnectionHandler. {}");
			acseAssociation.close();
			return null;
		}
		mmsRequestPdu = new MmsPdu();
		try {
			mmsRequestPdu.decode(new ByteArrayInputStream(rcvdPdu.array(), rcvdPdu.arrayOffset() + rcvdPdu.position(),
					rcvdPdu.limit() - rcvdPdu.position()), null);
		} catch (IOException e) {
			// accessPoint.abort("Socket was closed.");
			logger.debug("IOException decoding received packet. closing ConnectionHandler. {}");
			acseAssociation.close();
		}

		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// // throw new
		// // ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
		// // "1Got an invalid MMS packet", e);
		// }
		return mmsRequestPdu;
	}

	private MmsPdu createServiceErrorResponse(ServiceError e) {

		SubChoice_errorClass errClass = null;

		switch (e.getErrorCode()) {

		case ServiceError.NO_ERROR:

			break;
		case ServiceError.INSTANCE_NOT_AVAILABLE:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, new BerInteger(
					e.getErrorCode()), null, null, null, null, null);
			break;
		case ServiceError.INSTANCE_IN_USE:
			errClass = new SubChoice_errorClass(null, null, new BerInteger(e.getErrorCode()), null, null, null, null,
					null, null, null, null, null, null);
			break;
		case ServiceError.ACCESS_VIOLATION:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, new BerInteger(
					e.getErrorCode()), null, null, null, null, null);
			break;
		case ServiceError.ACCESS_NOT_ALLOWED_IN_CURRENT_STATE:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(e.getErrorCode()));
			break;
		case ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(2), null);
			break;
		case ServiceError.TYPE_CONFLICT:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(4), null);
			break;
		default:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(e.getErrorCode()));
		}
		org.openiec61850.jmms.mms.asn1.ServiceError asn1ServiceError = null;

		asn1ServiceError = new org.openiec61850.jmms.mms.asn1.ServiceError(errClass, null, new BerVisibleString(
				e.getMessage()));

		MmsPdu mmsPdu = new MmsPdu(null, null, null, null, null, asn1ServiceError);

		return mmsPdu;
	}

	private GetNameListResponse handleGetServerDirectoryRequest(GetNameListRequest getNameListRequest)
			throws ServiceError {

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();
		BerVisibleString identifier = null;

		for (ModelNode ld : accessPoint.serverModel) {
			identifier = new BerVisibleString(ld.getNodeName());
			identifiers.add(identifier);
		}

		GetNameListResponse getNameListResponse = new GetNameListResponse(new SubSeqOf_listOfIdentifier(identifiers),
				new BerBoolean(false));

		return getNameListResponse;
	}

	private GetNameListResponse handleGetDirectoryRequest(GetNameListRequest getNameListRequest) throws ServiceError {

		// the ObjectScope can be vmdSpecific,domainSpecific, or aaSpecific
		// vmdSpecific and aaSpecific are not part of 61850-8-1 but are used by
		// some IEC 61850 clients anyways.
		// this stack will return an empty list on vmdSpecific and aaSpecific
		// requests.

		if (getNameListRequest.objectScope.aaSpecific != null || getNameListRequest.objectScope.vmdSpecific != null) {
			SubSeqOf_listOfIdentifier listOfIden = new SubSeqOf_listOfIdentifier(new Vector<BerVisibleString>());
			GetNameListResponse getNameListResponse = new GetNameListResponse(listOfIden, new BerBoolean(false));
			return getNameListResponse;
		}

		String mmsDomainId = getNameListRequest.objectScope.domainSpecific.toString();

		ModelNode logicalDevice = accessPoint.serverModel.findSubNode(mmsDomainId);
		if (logicalDevice == null || !(logicalDevice instanceof LogicalDevice)) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
					"Got an invalid MMS request: given Domain name in GetNameList request is not a Logical Device name");
		}

		logger.debug(" ->ObjectReference: {}", mmsDomainId);

		insertRef = true;

		if (getNameListRequest.continueAfter != null) {
			continueAfter = getNameListRequest.continueAfter.toString();
			logger.debug(" ->continue after: {}", continueAfter.toString());
			insertRef = false;

		}

		List<String> mmsReferences = new LinkedList<String>();

		Collection<String> buffered = new LinkedList<String>();
		Collection<String> unbuffered = new LinkedList<String>();

		for (ModelNode logicalNode : logicalDevice) {
			mmsReferences.add(logicalNode.getNodeName());

			boolean reportsAdded = false;

			for (String mmsFC : mmsFCs) {
				FunctionalConstraint fc = FunctionalConstraint.fromString(mmsFC);
				if (fc != null) {

					List<FcDataObject> dataObjects = ((LogicalNode) logicalNode).getChildren(fc);
					if (dataObjects != null) {
						mmsReferences.add(logicalNode.getNodeName() + "$" + mmsFC);
						for (FcDataObject dataObject : dataObjects) {
							insertMMSRef(dataObject, mmsReferences, logicalNode.getNodeName() + "$" + mmsFC);

						}
					}
				}
				// TODO need to handle logs?
				else { // it could be RP or BR or LG??

					if (!reportsAdded) {
						Collection<ReportControlBlock> rcbs = ((LogicalNode) logicalNode).getReportControlBlocks();
						if (!rcbs.isEmpty()) {

							for (ReportControlBlock rcb : rcbs) {
								if (rcb instanceof UnbufferedReportContrlBlock) {

									String main = logicalNode.getNodeName() + "$" + "RP" + "$" + rcb.getNodeName();

									unbuffered.add(main);
									for (ModelNode node : rcb.getChildren()) {

										// if (node instanceof DataSet) {
										// unbuffered.add(main + "$DatSet");
										//
										// }
										// else {
										unbuffered.add(main + "$" + node.getNodeName());

										// }
									}

								}

								else if (rcb instanceof BufferedReportControlBlock) {

									String main = logicalNode.getNodeName() + "$" + "BR" + "$" + rcb.getNodeName();

									buffered.add(main);

									for (ModelNode node : rcb.getChildren()) {

										// if (node instanceof DataSet) {
										// buffered.add(main + "$DatSet");
										//
										// }
										// else {
										buffered.add(main + "$" + node.getNodeName());

										// }
									}

								}
							}

							if (!unbuffered.isEmpty()) {
								mmsReferences.add(logicalNode.getNodeName() + "$" + mmsFC);

								mmsReferences.addAll(unbuffered);
								unbuffered.clear();
							}

							if (!buffered.isEmpty()) {
								mmsReferences.add(logicalNode.getNodeName() + "$" + mmsFC);

								mmsReferences.addAll(buffered);
								buffered.clear();
							}

							reportsAdded = true;

						}
					}
				}
			}
		}

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();

		int identifierSize = 0;
		boolean moreFollows = false;
		for (String mmsReference : mmsReferences) {
			if (insertRef == true) {
				if (identifierSize > negotiatedMaxPduSize - 200) {
					moreFollows = true;
					logger.debug(" ->maxMMSPduSize of " + negotiatedMaxPduSize + " Bytes reached");
					break;
				}

				BerVisibleString identifier = null;

				identifier = new BerVisibleString(mmsReference);

				identifiers.add(identifier);
				identifierSize += mmsReference.length() + 2;
			}
			else {
				if (mmsReference.equals(continueAfter)) {
					insertRef = true;
				}
			}
		}

		SubSeqOf_listOfIdentifier listOfIden = new SubSeqOf_listOfIdentifier(identifiers);

		return new GetNameListResponse(listOfIden, new BerBoolean(moreFollows));
	}

	private static void insertMMSRef(ModelNode node, List<String> mmsRefs, String parentRef) {
		String ref = parentRef + '$' + node.getNodeName();
		mmsRefs.add(ref);
		if (!(node instanceof Array)) {
			for (ModelNode childNode : node) {
				insertMMSRef(childNode, mmsRefs, ref);
			}
		}
	}

	/**
	 * GetVariableAccessAttributes (GetDataDefinition/GetDataDirectory) can be
	 * called with different kinds of references. Examples: 1. DGEN1 2. DGEN1$CF
	 * 3. DGEN1$CF$GnBlk
	 * 
	 */
	private GetVariableAccessAttributesResponse handleGetVariableAccessAttributesRequest(
			GetVariableAccessAttributesRequest getVariableAccessAttributesRequest) throws ServiceError {
		if (getVariableAccessAttributesRequest.name == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Got an invalid MMS packet: name is not selected in GetVariableAccessAttributesRequest");
		}

		SubSeq_domain_specific domainSpecific = getVariableAccessAttributesRequest.name.domain_specific;

		if (domainSpecific == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Got an invalid MMS packet: Domain specific is not selected in GetVariableAccessAttributesRequest");
		}

		ModelNode modelNode = accessPoint.serverModel.getChild(domainSpecific.domainId.toString());

		if (modelNode == null) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
							+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
							+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
		}

		String itemIdString = domainSpecific.itemId.toString();

		int index1 = itemIdString.indexOf('$');

		LogicalNode logicalNode = null;

		if (index1 != -1) {
			logicalNode = (LogicalNode) modelNode.getChild(itemIdString.substring(0, index1));
			if (logicalNode == null) {
				throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
						"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
								+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
								+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
			}
			int index2 = itemIdString.indexOf('$', index1 + 2);
			if (index2 != -1) {
				FunctionalConstraint fc = FunctionalConstraint.fromString(itemIdString.substring(index1 + 1, index2));
				if (fc == null) {
					throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
							"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
									+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
									+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
				}
				index1 = itemIdString.indexOf('$', index2 + 2);
				ModelNode subNode;
				if (index1 == -1) {
					subNode = logicalNode.getChild(itemIdString.substring(index2 + 1), fc);
					if (subNode == null) {
						throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
								"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
										+ getVariableAccessAttributesRequest.name.domain_specific.domainId
										+ " and ItemID "
										+ getVariableAccessAttributesRequest.name.domain_specific.itemId
										+ " was found.");
					}
				}
				else {
					subNode = logicalNode.getChild(itemIdString.substring(index2 + 1, index1), fc);
					if (subNode == null) {
						throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
								"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
										+ getVariableAccessAttributesRequest.name.domain_specific.domainId
										+ " and ItemID "
										+ getVariableAccessAttributesRequest.name.domain_specific.itemId
										+ " was found.");
					}
					index2 = itemIdString.indexOf('$', index1 + 2);
					while (index2 != -1) {
						subNode = subNode.getChild(itemIdString.substring(index1 + 1, index2));
						if (subNode == null) {
							throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
									"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
											+ getVariableAccessAttributesRequest.name.domain_specific.domainId
											+ " and ItemID "
											+ getVariableAccessAttributesRequest.name.domain_specific.itemId
											+ " was found.");
						}
						index1 = index2;
						index2 = itemIdString.indexOf('$', index1 + 2);
					}
					subNode = subNode.getChild(itemIdString.substring(index1 + 1));
					if (subNode == null) {
						throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
								"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
										+ getVariableAccessAttributesRequest.name.domain_specific.domainId
										+ " and ItemID "
										+ getVariableAccessAttributesRequest.name.domain_specific.itemId
										+ " was found.");
					}
				}
				return new GetVariableAccessAttributesResponse(new BerBoolean(false), subNode.getMmsTypeSpec());
			}
			else {
				FunctionalConstraint fc = FunctionalConstraint.fromString(itemIdString.substring(index1 + 1));

				if (fc == null) {
					throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
							"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
									+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
									+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
				}

				List<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);

				if (fcDataObjects == null || fcDataObjects.size() == 0) {
					throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
							"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
									+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
									+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
				}
				List<StructComponent> doStructComponents = new LinkedList<StructComponent>();

				for (ModelNode child : fcDataObjects) {
					doStructComponents.add(new StructComponent(new BerVisibleString(child.getNodeName().getBytes()),
							child.getMmsTypeSpec()));
				}

				SubSeqOf_components comp = new SubSeqOf_components(doStructComponents);
				SubSeq_structure struct = new SubSeq_structure(null, comp);

				return new GetVariableAccessAttributesResponse(new BerBoolean(false), new TypeSpecification(null,
						struct, null, null, null, null, null, null, null, null, null, null));

			}
		}

		logicalNode = (LogicalNode) modelNode.getChild(itemIdString);
		if (logicalNode == null) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
							+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
							+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
		}

		List<StructComponent> structComponents = new LinkedList<StructComponent>();
		List<StructComponent> urcbStructComponents = new LinkedList<StructComponent>();
		List<StructComponent> brcbStructComponents = new LinkedList<StructComponent>();
		boolean rptCompAdded = false;

		for (String mmsFC : mmsFCs) {
			FunctionalConstraint fc = FunctionalConstraint.fromString(mmsFC);
			if (fc != null) {

				List<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);

				if (fcDataObjects != null) {
					List<StructComponent> doStructComponents = new LinkedList<StructComponent>();

					for (ModelNode child : fcDataObjects) {
						doStructComponents.add(new StructComponent(
								new BerVisibleString(child.getNodeName().getBytes()), child.getMmsTypeSpec()));
					}

					SubSeqOf_components comp = new SubSeqOf_components(doStructComponents);
					SubSeq_structure struct = new SubSeq_structure(null, comp);

					TypeSpecification fcTypeSpec = new TypeSpecification(null, struct, null, null, null, null, null,
							null, null, null, null, null);

					StructComponent structCom = null;

					structCom = new StructComponent(new BerVisibleString(mmsFC), fcTypeSpec);

					structComponents.add(structCom);

				}

			}
			else {
				if (!rptCompAdded) {
					Collection<ReportControlBlock> rcbs = (logicalNode).getReportControlBlocks();
					if (!rcbs.isEmpty()) {

						for (ReportControlBlock rcb : rcbs) {
							if (rcb instanceof UnbufferedReportContrlBlock) {
								// TODO commented out:
								// urcbStructComponents.add(getMmsStructComponent(rcb));
							}

							else if (rcb instanceof BufferedReportControlBlock) {
								// TODO commented out:
								// brcbStructComponents.add(getMmsStructComponent(rcb));
							}
						}

						if (!urcbStructComponents.isEmpty()) {

							SubSeqOf_components comp = new SubSeqOf_components(urcbStructComponents);
							SubSeq_structure struct = new SubSeq_structure(null, comp);

							TypeSpecification fcTypeSpec = new TypeSpecification(null, struct, null, null, null, null,
									null, null, null, null, null, null);

							StructComponent structCom = null;
							structCom = new StructComponent(new BerVisibleString(mmsFC), fcTypeSpec);

							structComponents.add(structCom);
						}
					}

					if (!brcbStructComponents.isEmpty()) {

						SubSeqOf_components comp = new SubSeqOf_components(brcbStructComponents);
						SubSeq_structure struct = new SubSeq_structure(null, comp);

						TypeSpecification fcTypeSpec = new TypeSpecification(null, struct, null, null, null, null,
								null, null, null, null, null, null);
						StructComponent structCom = null;
						structCom = new StructComponent(new BerVisibleString(mmsFC), fcTypeSpec);

						structComponents.add(structCom);
					}
					rptCompAdded = true;
				}
				// }

				// TODO handle other mms fcs

			}
		}

		SubSeqOf_components comp = new SubSeqOf_components(structComponents);
		SubSeq_structure struct = new SubSeq_structure(null, comp);

		TypeSpecification typeSpec = new TypeSpecification(null, struct, null, null, null, null, null, null, null,
				null, null, null);

		return new GetVariableAccessAttributesResponse(new BerBoolean(false), typeSpec);

	}

	private ReadResponse handleGetDataValuesRequest(ReadRequest mmsReadRequest) throws ServiceError {

		VariableAccessSpecification variableAccessSpecification = mmsReadRequest.variableAccessSpecification;

		List<AccessResult> listOfAccessResult = new LinkedList<AccessResult>();

		if (mmsReadRequest.specificationWithResult == null || mmsReadRequest.specificationWithResult.val == false) {
			logger.debug("Got a GetDataValues (MMS) request.");

			if (variableAccessSpecification.listOfVariable == null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"handleGetDataValuesRequest: Got an invalid MMS packet");
			}

			List<VariableDef> listOfVariable = variableAccessSpecification.listOfVariable.seqOf;

			if (listOfVariable.size() != 1) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
						"handleGetDataValuesRequest: more than one variableAccessSpecification is not allowed");
			}

			VariableDef variableDef = listOfVariable.get(0);

			// FcObjectReference fcObjRef =
			// MmsReference.toFCObjectReference(variableSeq);
			//
			// if (fcObjRef.objectReference.size() < 3) {
			// throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
			// "handleGetDataValuesRequest: objectReference: " +
			// fcObjRef.objectReference
			// +
			// " doesn't give a valid reference of functionally constraint data.");
			// }
			//
			// ModelNode modelNode;
			//
			// if (fcObjRef.fc == null) { // it may be either RP or BR
			//
			// VariableSpecification varSpec =
			// variableSeq.variableSpecification;
			// ObjectName objName = varSpec.name;
			// SubSeq_domain_specific domSpec = objName.domain_specific;
			// String mmsDomainID = domSpec.domainId.toString();
			// String mmsItemId = domSpec.itemId.toString();
			//
			// String funcCon = mmsItemId.substring(mmsItemId.indexOf("$") + 1,
			// mmsItemId.lastIndexOf("$"));
			//
			// if (!(funcCon.equals("RP")) && !(funcCon.equals("BR")) &&
			// !(funcCon.equals("LG"))) {
			// throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
			// "handleGetDataValuesRequest: DomainID: " + mmsDomainID +
			// " and ItemID: " + mmsItemId
			// +
			// " doesn't give a valid reference of functionally constraint data.");
			// }
			//
			// modelNode = accessPoint.getRCB(association,
			// fcObjRef.objectReference);
			//
			// if (modelNode == null) {
			// throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
			// "object not found: "
			// + fcObjRef.objectReference);
			// }
			//
			// if (!(modelNode instanceof ReportControlBlock)) {
			// throw new
			// ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
			// "invalid object reference "
			// + fcObjRef.objectReference);
			// }
			//
			// // TODO other types with fc = null??
			// }
			// else {

			ModelNode modelNode = accessPoint.serverModel.getNodeFromVariableDef(variableDef);
			// modelNode = accessPoint.getDataValues(association,
			// fcObjRef.objectReference, fcObjRef.fc);

			modelNode = modelNode.copy();

			List<BasicDataAttribute> basicDataAttributes = modelNode.getBasicDataAttributes();
			accessPoint.dataSource.readValues(basicDataAttributes);

			// }
			AccessResult accessRes = null;
			Data result = null;
			if (modelNode != null) {
				result = modelNode.getMmsDataObj();
			}

			// AccessResult accessResult;
			if (result == null) {
				// DataAccessError dataAccessError = new DataAccessError();
				// dataAccessError.setValue(0L); // TODO korrekten Fehlercode
				// übergeben
				// accessResult.selectFailure(dataAccessError);
				accessRes = new AccessResult(new BerInteger(0L), null);
				if (modelNode == null) {
					logger.debug("DataAccessError");
				}
				else {
					logger.debug("DataAccessError accessing " + modelNode.getReference());
				}
			}
			else {
				accessRes = new AccessResult(null, result);
			}

			listOfAccessResult.add(accessRes); // exactly one element
		}

		else {
			// then this is a getDataSetValues request

			String dataSetReference = convertToDataSetReference(variableAccessSpecification.variableListName);

			if (dataSetReference == "") {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
						"handleGetDataValuesRequest: data set name incorrect");
			}

			// TODO handle non-persistend DataSets too

			DataSet dataSetCopy = accessPoint.serverModel.getDataSet(dataSetReference).copy();

			accessPoint.dataSource.readValues(dataSetCopy.getBasicDataAttributes());

			for (FcModelNode dsMember : dataSetCopy) {
				Data result = dsMember.getMmsDataObj();

				AccessResult accessResult = null;
				if (result == null) {
					accessResult = new AccessResult(new BerInteger(0L), null);
				}
				else {
					accessResult = new AccessResult(null, result);
				}
				listOfAccessResult.add(accessResult);
			}

		}

		SubSeqOf_listOfAccessResult listOf = new SubSeqOf_listOfAccessResult(listOfAccessResult);
		return new ReadResponse(null, listOf);
		// readResponse.setListOfAccessResult(listOfAccessResult);
	}

	private WriteResponse handleSetDataValuesRequest(WriteRequest mmsWriteRequest) throws ServiceError {

		VariableAccessSpecification variableAccessSpecification = mmsWriteRequest.variableAccessSpecification;

		// FunctionalConstraint fc = null;

		List<Data> listOfData = mmsWriteRequest.listOfData.seqOf;

		List<WriteResponse.SubChoice> mmsResponseValues = new ArrayList<WriteResponse.SubChoice>(listOfData.size());

		if (variableAccessSpecification.listOfVariable != null) {

			logger.debug("Got a SetDataValues (MMS) request.");

			List<VariableDef> listOfVariable = variableAccessSpecification.listOfVariable.seqOf;

			if (listOfVariable.size() != 1) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
						"handleSetDataValuesRequest: more than one variableAccessSpecification is not allowed");
			}

			VariableDef variableDef = listOfVariable.get(0);

			// FcObjectReference fcObjRef =
			// MmsReference.toFCObjectReference(variableSeq);
			//
			// if (fcObjRef.objectReference.size() < 3) {
			// throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
			// "handleSetDataValuesRequest: objectReference: " +
			// fcObjRef.objectReference
			// +
			// " doesn't give a valid reference of functionally constraint data.");
			// }

			for (Data mmsData : listOfData) {
				WriteResponse.SubChoice result = null;
				try {

					// if (fcObjRef.fc == null) {
					// // TODO was commented out:
					// // ReportControlBlock rcb = (ReportControlBlock)
					// // accessPoint.getRCB(association,
					// // new
					// //
					// ObjectReference(fcObjRef.objectReference.getParentRef()));
					// ReportControlBlock rcb = (ReportControlBlock)
					// accessPoint.getRCB(association,
					// new ObjectReference(""));
					//
					// ModelNode container =
					// rcb.getChild(fcObjRef.objectReference.getName()).copy();
					//
					// container.setValueFromMmsDataObj(mmsData);
					// //
					// MmsAsn1ComplexTypeConverter.convertMmsDataToDataContainer(container,
					// // null, mmsData, mmsFCs);
					//
					// if (rcb instanceof UnbufferedReportContrlBlock) {
					//
					// accessPoint.setURCBValues(association,
					// (UnbufferedReportContrlBlock) rcb, container);
					// }
					// else {
					// // TODO for buffered report blocks
					// }
					// }
					// else {
					// String objRef = fcObjRef.objectReference.toString();
					//
					// ModelNode modelNode =
					// accessPoint.getDataDefinition(association, new
					// ObjectReference(objRef),
					// fcObjRef.fc).copy();

					ModelNode modelNode = accessPoint.serverModel.getNodeFromVariableDef(variableDef);
					modelNode.setValueFromMmsDataObj(mmsData);

					List<BasicDataAttribute> basicDataAttributes = modelNode.getBasicDataAttributes();

					accessPoint.dataSource.writeValues(basicDataAttributes);

					result = new WriteResponse.SubChoice(null, new BerNull());
				} catch (ServiceError e) {
					logger.warn(e.getMessage());
					// correct error code?
					result = new WriteResponse.SubChoice(new BerInteger(0L), null);
				}
				mmsResponseValues.add(result);
			}
		}
		else if (variableAccessSpecification.variableListName != null) {
			// data set
			String dataSetRef = convertToDataSetReference(variableAccessSpecification.variableListName);

			// TODO handle non-persisten DataSets too

			DataSet dataSetCopy = accessPoint.serverModel.getDataSet(dataSetRef).copy();

			Iterator<Data> dataIterator = listOfData.iterator();

			for (FcModelNode dataSetMember : dataSetCopy) {
				dataSetMember.setValueFromMmsDataObj(dataIterator.next());
			}

			accessPoint.dataSource.writeValues(dataSetCopy.getBasicDataAttributes());

			// TODO handle errors
			// for (ServiceError e : results) {
			// if (e.getErrorCode() == ServiceError.NO_ERROR) {
			// WriteResponse.SubChoice result = new
			// WriteResponse.SubChoice(null, new BerNull());
			// mmsResponseValues.add(result);
			// }
			// else {
			// WriteResponse.SubChoice result = new WriteResponse.SubChoice(new
			// BerInteger(0L), null);
			// mmsResponseValues.add(result);
			// }
			// }

		}
		else {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
					"handleSetDataValuesRequest: invalid MMS request");
		}

		return new WriteResponse(mmsResponseValues);
	}

	private GetNameListResponse handleGetDataSetNamesRequest(GetNameListRequest getNameListRequest) throws ServiceError {

		BerVisibleString domainSpecific = getNameListRequest.objectScope.domainSpecific;

		if (domainSpecific == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleGetDataSetNamesRequest: domainSpecific not selected");
		}

		List<String> dsList = accessPoint.serverModel.getDataSetNames(domainSpecific.toString());

		insertRef = true;
		if (getNameListRequest.continueAfter != null) {
			continueAfter = getNameListRequest.continueAfter.toString();
			insertRef = false;
		}

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();

		int identifierSize = 0;
		boolean moreFollows = false;

		if (dsList != null) {
			for (String dsRef : dsList) {
				if (insertRef == true) {
					if (identifierSize > negotiatedMaxPduSize - 200) {
						moreFollows = true;
						logger.info("maxMMSPduSize reached");
						break;
					}
					identifiers.add(new BerVisibleString(dsRef.getBytes()));
					identifierSize += dsRef.length() + 2;
				}
				else {
					if (dsRef.equals(continueAfter)) {
						insertRef = true;
					}
				}
			}
		}

		SubSeqOf_listOfIdentifier listOf = new SubSeqOf_listOfIdentifier(identifiers);

		return new GetNameListResponse(listOf, new BerBoolean(moreFollows));
	}

	private GetNamedVariableListAttributesResponse handleGetDataSetDirectoryRequest(ObjectName mmsGetNamedVarListAttReq)
			throws ServiceError {

		String dataSetReference = convertToDataSetReference(mmsGetNamedVarListAttReq);

		DataSet dataSet = accessPoint.serverModel.getDataSet(dataSetReference);

		List<VariableDef> listOfVariable = new ArrayList<VariableDef>();

		for (FcModelNode member : dataSet) {
			listOfVariable.add(member.getMmsVariableDef());
		}
		return new GetNamedVariableListAttributesResponse(new BerBoolean(dataSet.isDeletable()),
				new SubSeqOf_listOfVariable(listOfVariable));
	}

	private static String convertToDataSetReference(ObjectName mmsObjectName) {
		if (mmsObjectName.domain_specific != null) {
			return mmsObjectName.domain_specific.domainId.toString() + "/"
					+ mmsObjectName.domain_specific.itemId.toString().replace('$', '.');
		}
		else if (mmsObjectName.aa_specific != null) {
			// format is "@DataSetName"
			return mmsObjectName.aa_specific.toString();
		}
		return "";
	}

	private BerNull handleCreateDataSetRequest(DefineNamedVariableListRequest mmsDefineNamedVariableListRequest)
			throws ServiceError {
		String dataSetReference = convertToDataSetReference(mmsDefineNamedVariableListRequest.variableListName);
		if (dataSetReference == null) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
					"handleCreateDataSetRequest: invalid MMS request (No DataSet Name Specified)");
		}

		List<VariableDef> nameList = mmsDefineNamedVariableListRequest.listOfVariable.seqOf;

		List<FcModelNode> dataSetMembers = new ArrayList<FcModelNode>(nameList.size());

		for (VariableDef variableDef : nameList) {
			dataSetMembers.add(accessPoint.serverModel.getNodeFromVariableDef(variableDef));
		}

		DataSet dataSet = new DataSet(dataSetReference, dataSetMembers, true);

		if (dataSetReference.startsWith("@")) {
			accessPoint.addNonPersistentDataSet(dataSet, this);
		}
		else {
			accessPoint.serverModel.addDataSet(dataSet);
		}

		return new BerNull();
	}

	private DeleteNamedVariableListResponse handleDeleteDataSetRequest(
			DeleteNamedVariableListRequest mmsDelNamVarListReq) throws ServiceError {
		String dataSetRef = convertToDataSetReference(mmsDelNamVarListReq.listOfVariableListName.seqOf.get(0));

		// TODO handle non-persistent DataSet

		if (accessPoint.serverModel.removeDataSet(dataSetRef) == null) {
			if (accessPoint.serverModel.getDataSet(dataSetRef) == null) {
				return new DeleteNamedVariableListResponse(new BerInteger(0), new BerInteger(0));
			}
			else {
				return new DeleteNamedVariableListResponse(new BerInteger(1), new BerInteger(0));
			}
		}
		else {
			return new DeleteNamedVariableListResponse(new BerInteger(1), new BerInteger(1));
		}
	}

	// public void sendReport(Report report) throws IOException {
	// try {
	// // InformationReport infoReport = new InformationReport();
	//
	// // ObjectName rptName = new ObjectName();
	// // rptName.selectVmd_specific(new Identifier("RPT"));
	// //
	// // VariableAccessSpecification varAccessSpec = new
	// // VariableAccessSpecification();
	// // varAccessSpec.selectVariableListName(rptName);
	// //
	// // infoReport.setVariableAccessSpecification(varAccessSpec);
	//
	// VariableAccessSpecification varAccSpec = new
	// VariableAccessSpecification(null, new ObjectName(
	// new BerVisibleString("RPT"), null, null));
	//
	// OptFields optFlds = report.getOptFlds();
	//
	// List<AccessResult> listOfAccessResult = new LinkedList<AccessResult>();
	//
	// // rptID
	//
	// AccessResult accessRes1 = new AccessResult(null, null, null, null, null,
	// null, null, null, null,
	// new BerVisibleString(report.getRptId()), null, null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes1);
	//
	// // Reported OptFlds
	// // Data rptOptFlds = new Data();
	// // rptOptFlds.selectBit_string(new BitString(report.getOptFlds()
	// // .getValue()));
	// // AccessResult accessRes2 = new AccessResult();
	// // accessRes2.selectSuccess(rptOptFlds);
	// AccessResult accessRes2 = new AccessResult(null, null, null, null, new
	// BerBitString(report.getOptFlds()
	// .getValue(), 10), null, null, null, null, null, null, null, null, null,
	// null, null);
	//
	// listOfAccessResult.add(accessRes2);
	//
	// if (optFlds.isSeqNum()) {
	// // Data seqNum = new Data();
	// // seqNum.selectUnsigned(new Long(report.getSqNum()));
	// // AccessResult accessRes3 = new AccessResult();
	// // accessRes3.selectSuccess(seqNum);
	// AccessResult accessRes3 = new AccessResult(null, null, null, null, null,
	// null, new BerInteger(
	// report.getSqNum()), null, null, null, null, null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes3);
	// }
	//
	// if (optFlds.isTimeStamp()) {
	// // Data timeStamp = new Data();
	// // timeStamp.selectBinary_time(new TimeOfDay(
	// // MmsAsn1PrimitiveTypeConverter.toByteArray(report
	// // .getTimeOfEntry())));
	// // AccessResult accessRes4 = new AccessResult();
	// // accessRes4.selectSuccess(timeStamp);
	// AccessResult accessRes4 = new AccessResult(null, null, null, null, null,
	// null, null, null, null, null,
	// null, new
	// BerOctetString(MmsAsn1PrimitiveTypeConverter.toByteArray(report.getTimeOfEntry())),
	// null, null, null, null);
	// listOfAccessResult.add(accessRes4);
	// }
	//
	// if (optFlds.isDataSet()) {
	// // Data dataSet = new Data();
	// // dataSet.selectVisible_string(report.getDataSet().replace(".",
	// // "$"));
	// // AccessResult accessRes5 = new AccessResult();
	// // accessRes5.selectSuccess(dataSet);
	// AccessResult accessRes5 = new AccessResult(null, null, null, null, null,
	// null, null, null, null,
	// new BerVisibleString(report.getDataSetRef().replace(".", "$")), null,
	// null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes5);
	// }
	//
	// if (optFlds.isBufOvfl()) {
	// // Data bufOvfl = new Data();
	// // bufOvfl.selectBoolean_(report.getBufOvfl());
	// // AccessResult accessRes6 = new AccessResult();
	// // accessRes6.selectSuccess(bufOvfl);
	// AccessResult accessRes6 = new AccessResult(null, null, null, new
	// BerBoolean(report.getBufOvfl()), null,
	// null, null, null, null, null, null, null, null, null, null, null);
	// listOfAccessResult.add(accessRes6);
	// }
	//
	// if (optFlds.isEntryId()) {
	// // Data entryId = new Data();
	// // entryId.selectOctet_string(MmsAsn1PrimitiveTypeConverter
	// // .toByteArray(report.getEntryId()));
	// // AccessResult accessRes7 = new AccessResult();
	// // accessRes7.selectSuccess(entryId);
	// AccessResult accessRes7 = new AccessResult(null, null, null, null, null,
	// null, null, null,
	// new
	// BerOctetString(MmsAsn1PrimitiveTypeConverter.toByteArray(report.getEntryId())),
	// null, null,
	// null, null, null, null, null);
	// listOfAccessResult.add(accessRes7);
	// }
	//
	// if (optFlds.isConfigRef()) {
	// // Data confRev = new Data();
	// // confRev.selectUnsigned(report.getConvRev());
	// // AccessResult accessRes8 = new AccessResult();
	// // accessRes8.selectSuccess(confRev);
	// AccessResult accessRes8 = new AccessResult(null, null, null, null, null,
	// null, new BerInteger(
	// report.getConvRev()), null, null, null, null, null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes8);
	// }
	//
	// // TODO check if segmentation number is used
	//
	// // if (optFlds.isSeqNum()) {
	// // Data subSeqNum = new Data();
	// // subSeqNum.selectUnsigned(new Long(report.getSubSqNum()));
	// // AccessResult accessRes9 = new AccessResult();
	// // accessRes9.selectSuccess(subSeqNum);
	// // listOfAccessResult.add(accessRes9);
	// //
	// // Data moreFollows = new Data();
	// // moreFollows.selectBoolean_(report.getMoreSegmentsFollow());
	// // AccessResult accessRes10 = new AccessResult();
	// // accessRes10.selectSuccess(moreFollows);
	// // listOfAccessResult.add(accessRes10);
	// // }
	//
	// // TODO padding required?
	// // Data inclusionString = new Data();
	// // inclusionString.selectBit_string(new BitString(report
	// // .getInclusionBitString()));
	// // AccessResult accessRes11 = new AccessResult();
	// // accessRes11.selectSuccess(inclusionString);
	// AccessResult accessRes11 = new AccessResult(null, null, null, null, new
	// BerBitString(
	// report.getInclusionBitString(), 8), null, null, null, null, null, null,
	// null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes11);
	//
	// List<ReportEntryData> entryData = report.getEntryData();
	// List<AccessResult> dataRefs = new ArrayList<AccessResult>();
	// List<AccessResult> values = new ArrayList<AccessResult>();
	// List<AccessResult> reasonCodes = new ArrayList<AccessResult>();
	//
	// AccessResult dataRefAC;
	// // Data dataRef;
	// AccessResult reasonCodeAC;
	// AccessResult value;
	// // Data reasonCode;
	// byte[] reasonCodeValue = new byte[1];
	//
	// for (ReportEntryData data : entryData) {
	// if (optFlds.isDataRef()) {
	// // dataRef = new Data();
	// // dataRef.selectVisible_string(data.getValue().getReference()
	// // .get(0)
	// // + "/"
	// // + MMSReference.getMMSItemId(data.getValue()
	// // .getReference(), data.getValue()
	// // .getFunctionalConstraint()));
	// // dataRefAC = new AccessResult();
	// // dataRefAC.selectSuccess(dataRef);
	// dataRefAC = new AccessResult(null, null, null, null, null, null, null,
	// null, null,
	// new BerVisibleString(data.getValue().getReference().get(0)
	// + "/"
	// + MmsReference.getMMSItemId(data.getValue().getReference(),
	// data.getValue()
	// .getFunctionalConstraint())), null, null, null, null, null, null);
	// dataRefs.add(dataRefAC);
	// }
	//
	// Data result = data.getValue().getMmsDataObj();
	//
	// if (result == null) {
	// value = new AccessResult(new BerInteger(0L), null, null, null, null,
	// null, null, null, null, null,
	// null, null, null, null, null, null);
	// }
	// else {
	// value = new AccessResult(null, result.array, result.structure,
	// result.boolean_, result.bit_string,
	// result.integer, result.unsigned, result.floating_point,
	// result.octet_string,
	// result.visible_string, result.generalized_time, result.binary_time,
	// result.bcd,
	// result.booleanArray, result.mMSString, result.utc_time);
	// }
	//
	// values.add(value);
	//
	// if (optFlds.isReasonCode()) {
	// // reasonCode = new Data();
	// switch (data.getReasonCode()) {
	// case DCHG:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 6));
	// break;
	// case QCHG:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 5));
	// break;
	// case DUPD:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 4));
	// break;
	// case INTEGRITY:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 3));
	// break;
	// case GI:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 2));
	// break;
	// case APPTRIGGER:
	// reasonCodeValue[0] = (byte) (byte) (reasonCodeValue[0] | (1 << 1));
	// }
	// // reasonCode.selectBit_string(new
	// // BitString(reasonCodeValue,
	// // 1));
	// // reasonCodeAC = new AccessResult();
	// // reasonCodeAC.selectSuccess(reasonCode);
	// reasonCodeAC = new AccessResult(null, null, null, null, new
	// BerBitString(reasonCodeValue, 8), null,
	// null, null, null, null, null, null, null, null, null, null);
	// reasonCodes.add(reasonCodeAC);
	// }
	// }
	//
	// listOfAccessResult.addAll(dataRefs);
	// listOfAccessResult.addAll(values);
	// listOfAccessResult.addAll(reasonCodes);
	//
	// // infoReport.setListOfAccessResult(listOfAccessResult);
	// // UnconfirmedService unconfirmedSer = new UnconfirmedService();
	// // unconfirmedSer.selectInformationReport(infoReport);
	// //
	// // UnconfirmedPDU unconfirmedPDU = new UnconfirmedPDU();
	// // unconfirmedPDU.setUnconfirmedService(unconfirmedSer);
	// //
	// // MmsPdu mmsResponsePdu = new MmsPdu();
	// // mmsResponsePdu.selectUnconfirmedPDU(unconfirmedPDU);
	// // sendAnMmsPdu(pConnection, mmsResponsePdu);
	// InformationReport infoReport = new InformationReport(varAccSpec,
	// new InformationReport.SubSeqOf_listOfAccessResult(listOfAccessResult));
	// UnconfirmedService unconfirmedSer = new UnconfirmedService(infoReport);
	// MmsPdu mmsResponsePdu = new MmsPdu(null, null, new
	// UnconfirmedPDU(unconfirmedSer), null, null, null);
	// sendAnMmsPdu(pConnection, mmsResponsePdu);
	// } catch (ServiceError e) {
	// logger.warn(e.getMessage());
	// sendAnMmsPdu(pConnection, createServiceErrorResponse(e));
	// }
	// }

	private GetNameListResponse handleGetNameListJournalRequest(GetNameListRequest getNameListRequest) {
		// TODO liefert momentan leere Liste zurück:

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();
		SubSeqOf_listOfIdentifier listOf = new SubSeqOf_listOfIdentifier(identifiers);

		return new GetNameListResponse(listOf, new BerBoolean(false));
	}

	public void close() {
		if (pConnection != null) {
			pConnection.disconnect();
		}
	}

	public void sendReport(Report report) throws IOException {
		// TODO Auto-generated method stub

	}

}
