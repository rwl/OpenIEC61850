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

/**
 * This Enumeration includes all possible Types for IEC 61850 leave nodes (
 * {@link BasicDataAttribute}). This includes BasicTypes and CommonACSITypes as
 * defined in part 7-2.
 * 
 */
public enum DaType {

	BOOLEAN, INT8, INT16, INT32, INT64, INT8U, INT16U, INT32U, FLOAT32, FLOAT64, OCTET_STRING, VISIBLE_STRING, UNICODE_STRING, TIMESTAMP, ENTRY_TIME, BIT_STRING;
}
