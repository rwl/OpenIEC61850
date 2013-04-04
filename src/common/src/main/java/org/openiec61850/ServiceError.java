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

public class ServiceError extends Exception {

	public static final int NO_ERROR = 0;
	public static final int INSTANCE_NOT_AVAILABLE = 1;
	public static final int INSTANCE_IN_USE = 2;
	public static final int ACCESS_VIOLATION = 3;
	public static final int ACCESS_NOT_ALLOWED_IN_CURRENT_STATE = 4;
	public static final int PARAMETER_VALUE_INAPPROPRIATE = 5;
	public static final int PARAMETER_VALUE_INCONSISTENT = 6;
	public static final int CLASS_NOT_SUPPORTED = 7;
	public static final int INSTANCE_LOCKED_BY_OTHER_CLIENT = 8;
	public static final int CONTROL_MUST_BE_SELECTED = 9;
	public static final int TYPE_CONFLICT = 10;
	public static final int FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT = 11;
	public static final int FAILED_DUE_TO_SERVER_CONSTRAINT = 12;
	public static final int APPLICATION_UNREACHABLE = 13;
	public static final int CONNECTION_LOST = 14;
	public static final int MEMORY_UNAVAILABLE = 15;
	public static final int PROCESSOR_RESOURCE_UNAVAILABLE = 16;
	public static final int FATAL = 20;
	// added to handle data access errors mentioned in iec61850-8-1
	public static final int DATA_ACCESS_ERROR = 21;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4290107163231828564L;

	private final int errorCode;

	public ServiceError(int errorCode) {
		super("Error code=" + errorCode);
		this.errorCode = errorCode;
	}

	public ServiceError(int errorCode, String s) {
		super(s);
		this.errorCode = errorCode;
	}

	public ServiceError(int errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public ServiceError(int errorCode, String s, Throwable cause) {
		super(s, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
