///*
// * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
// *
// * This file is part of openIEC61850.
// * For more information visit http://www.openmuc.org 
// *
// * openIEC61850 is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 2.1 of the License, or
// * (at your option) any later version.
// *
// * openIEC61850 is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with openIEC61850.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//package org.openiec61850.server.report;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//
//public class MonitorThread extends Thread {
//
//	private static MonitorThread instance = new MonitorThread();
//	private final List<Runnable> runnables = new ArrayList<Runnable>();
//	private static Logger logger = LoggerFactory.getLogger(MonitorThread.class);
//
//	protected MonitorThread() {
//		setName("MonitorThread");
//		start();
//	}
//
//	public static MonitorThread theThread() {
//		return instance;
//	}
//
//	@Override
//	public void run() {
//		while (true) {
//			try {
//				for (Runnable runnable : runnables) {
//					runnable.run();
//				}
//			} catch (Throwable exc) {
//				logger.error("Error in {} {}", getName(), exc.getMessage());
//			}
//			try {
//				sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public void addService(Runnable runnable) {
//		runnables.add(runnable);
//	}
//
// }
