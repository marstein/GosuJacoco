/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.test.targets;

/**
 * Collection of stub methods that are called from the coverage targets. *
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Stubs {

	/**
	 * Dummy method.
	 */
	public static void nop() {
	}

	/**
	 * @return always <code>true</code>
	 */
	public static boolean t() {
		return true;
	}

	/**
	 * @return always <code>false</code>
	 */
	public static boolean f() {
		return false;
	}

	public static class Base {

		public Base() {
		}

		public Base(boolean b) {
		}

	}

}
