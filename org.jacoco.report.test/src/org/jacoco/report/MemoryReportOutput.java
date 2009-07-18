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
package org.jacoco.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory report output for test purposes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MemoryReportOutput implements IReportOutput {

	private final Map<String, ByteArrayOutputStream> files = new HashMap<String, ByteArrayOutputStream>();

	public OutputStream createFile(String path) throws IOException {
		assertFalse("Duplicate output " + path, files.containsKey(path));
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		files.put(path, out);
		return out;
	}

	public void assertFile(String path) {
		assertNotNull("Missing " + path, files.get(path));
	}

	public void assertSingleFile(String path) {
		assertEquals(Collections.singleton(path), files.keySet());
	}

	public byte[] getFile(String path) {
		assertFile(path);
		return files.get(path).toByteArray();
	}

}
