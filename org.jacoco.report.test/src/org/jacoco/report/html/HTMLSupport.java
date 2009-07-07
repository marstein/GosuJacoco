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
package org.jacoco.report.html;

import javax.xml.parsers.ParserConfigurationException;

import org.jacoco.report.xml.XMLSupport;

/**
 * Support for verifying XHTML documents.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class HTMLSupport extends XMLSupport {

	public HTMLSupport() throws ParserConfigurationException {
		super(HTMLSupport.class);
	}

}
