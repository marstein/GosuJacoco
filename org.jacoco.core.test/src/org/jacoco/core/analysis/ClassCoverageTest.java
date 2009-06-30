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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Unit test for {@link ClassCoverage}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassCoverageTest {

	@Test
	public void testProperties() {
		ClassCoverage data = new ClassCoverage("Sample", "Sample.java",
				new ArrayList<MethodCoverage>());
		assertEquals(ICoverageNode.ElementType.CLASS, data.getElementType());
		assertEquals("Sample", data.getName());
		assertEquals("Sample.java", data.getSourceFileName());
		assertNotNull(data.getLines());
	}

	@Test
	public void testGetPackageName1() {
		ClassCoverage data = new ClassCoverage("ClassInDefaultPackage",
				"Sample.java", new ArrayList<MethodCoverage>());
		assertEquals("", data.getPackageName());
	}

	@Test
	public void testGetPackageName2() {
		ClassCoverage data = new ClassCoverage("org/jacoco/examples/Sample",
				"Sample.java", new ArrayList<MethodCoverage>());
		assertEquals("org/jacoco/examples", data.getPackageName());
	}

	@Test
	public void testEmptyClass() {
		ICoverageNode data = new ClassCoverage("Sample", "Sample.java",
				new ArrayList<MethodCoverage>());
		assertEquals(0, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testNotCovered() {
		final ArrayList<MethodCoverage> methods = new ArrayList<MethodCoverage>();
		methods.add(createMethod(false));
		methods.add(createMethod(false));
		ICoverageNode data = new ClassCoverage("Sample", "Sample.java", methods);
		assertEquals(10, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCovered() {
		final ArrayList<MethodCoverage> methods = new ArrayList<MethodCoverage>();
		methods.add(createMethod(false));
		methods.add(createMethod(true));
		ICoverageNode data = new ClassCoverage("Sample", "Sample.java", methods);
		assertEquals(10, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(5, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getClassCounter().getCoveredCount(), 0.0);
	}

	private MethodCoverage createMethod(boolean covered) {
		final MethodCoverage m = new MethodCoverage("sample", "()V", null);
		m.addBlock(5, new int[0], covered);
		return m;
	}

}
