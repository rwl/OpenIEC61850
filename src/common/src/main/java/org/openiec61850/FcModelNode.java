package org.openiec61850;

import java.util.ArrayList;
import java.util.List;

import org.openiec61850.jmms.mms.asn1.AlternateAccess;
import org.openiec61850.jmms.mms.asn1.AlternateAccess.SubChoice.SubSeq_selectAlternateAccess;
import org.openiec61850.jmms.mms.asn1.AlternateAccess.SubChoice.SubSeq_selectAlternateAccess.SubChoice_accessSelection;
import org.openiec61850.jmms.mms.asn1.ObjectName;
import org.openiec61850.jmms.mms.asn1.VariableDef;
import org.openiec61850.jmms.mms.asn1.VariableSpecification;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;

public abstract class FcModelNode extends ModelNode {

	private VariableDef variableDef = null;
	protected FunctionalConstraint fc;

	public FunctionalConstraint getFunctionalConstraint() {
		return fc;
	}

	@Override
	public String toString() {
		return getReference().toString() + " [" + fc + "]";

	}

	VariableDef getMmsVariableDef() {

		if (variableDef != null) {
			return variableDef;
		}

		AlternateAccess alternateAccess = null;

		StringBuilder preArrayIndexItemId = new StringBuilder(objectReference.get(1));
		preArrayIndexItemId.append("$");
		preArrayIndexItemId.append(fc);

		int arrayIndexPosition = objectReference.getArrayIndexPosition();
		if (arrayIndexPosition != -1) {

			for (int i = 2; i < arrayIndexPosition; i++) {
				preArrayIndexItemId.append("$");
				preArrayIndexItemId.append(objectReference.get(i));
			}

			List<AlternateAccess.SubChoice> subSeqOfAlternateAccess = new ArrayList<AlternateAccess.SubChoice>();
			BerInteger indexBerInteger = new BerInteger(Integer.parseInt(objectReference.get(arrayIndexPosition)));

			if (arrayIndexPosition < (objectReference.size() - 1)) {
				// this reference points to a subnode of an array element

				StringBuilder postArrayIndexItemId = new StringBuilder(objectReference.get(arrayIndexPosition + 1));

				for (int i = (arrayIndexPosition + 2); i < objectReference.size(); i++) {
					postArrayIndexItemId.append("$");
					postArrayIndexItemId.append(objectReference.get(i));
				}

				// component name is stored in an AlternateAccess
				List<AlternateAccess.SubChoice> subSeqOf = new ArrayList<AlternateAccess.SubChoice>();
				subSeqOf.add(new AlternateAccess.SubChoice(null, new BerVisibleString(postArrayIndexItemId.toString()
						.getBytes()), null, null, null, null));
				AlternateAccess subArrayEle = new AlternateAccess(subSeqOf);

				SubChoice_accessSelection accSel = new SubChoice_accessSelection(null, indexBerInteger, null, null);
				SubSeq_selectAlternateAccess selectAltAcc = new SubSeq_selectAlternateAccess(accSel, subArrayEle);

				subSeqOfAlternateAccess.add(new AlternateAccess.SubChoice(selectAltAcc, null, null, null, null, null));

			}
			else {
				subSeqOfAlternateAccess
						.add(new AlternateAccess.SubChoice(null, null, indexBerInteger, null, null, null));
			}

			alternateAccess = new AlternateAccess(subSeqOfAlternateAccess);

		}
		else {

			for (int i = 2; i < objectReference.size(); i++) {
				preArrayIndexItemId.append("$");
				preArrayIndexItemId.append(objectReference.get(i));
			}
		}

		ObjectName objectName = new ObjectName(null, new ObjectName.SubSeq_domain_specific(new BerVisibleString(
				objectReference.get(0).getBytes()), new BerVisibleString(preArrayIndexItemId.toString().getBytes())),
				null);

		VariableSpecification varSpec = new VariableSpecification(objectName);

		variableDef = new VariableDef(varSpec, alternateAccess);
		return variableDef;
	}

}
