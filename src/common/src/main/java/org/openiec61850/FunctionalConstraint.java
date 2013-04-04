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

public enum FunctionalConstraint {

	// The following FCs are not part of this enum because they are not really
	// FCs and only defined in part 8-1:
	// RP (report), LG (log), BR (buffered report), GO, GS, MS, US

	// FCs according to IEC 61850-7-2:
	/** Status information */
	ST,
	/** Measurands - analogue values */
	MX,
	/** Setpoint */
	SP,
	/** Substitution */
	SV,
	/** Configuration */
	CF,
	/** Description */
	DC,
	/** Setting group */
	SG,
	/** Setting group editable */
	SE,
	/** Service response / Service tracking */
	SR,
	/** Operate received */
	OR,
	/** Blocking */
	BL,
	/** Extended definition */
	EX,
	/** Control, deprecated but kept here for backward compatibility */
	CO;

	/*
	 * * @param fc
	 * 
	 * @return
	 */

	public static FunctionalConstraint fromString(String fc) {
		try {
			return FunctionalConstraint.valueOf(fc);
		} catch (Exception e) {
			return null;
		}
	}

}
