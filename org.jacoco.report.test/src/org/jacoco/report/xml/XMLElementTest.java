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
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link XMLElement}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class XMLElementTest {

	private StringWriter buffer;

	private XMLElement root;

	@Before
	public void setUp() throws IOException {
		buffer = new StringWriter();
		root = new XMLElement(buffer, "root");
		root.beginOpenTag();
	}

	@Test
	public void testEmptyNode() throws IOException {
		root.close();
		// Second close has no effect:
		root.close();
		assertEquals("<root/>", buffer.toString());
	}

	@Test(expected = IOException.class)
	public void testAddAttributeToClosedNode() throws IOException {
		root.close();
		root.attr("attr", "value");
	}

	@Test(expected = IOException.class)
	public void testAddChildToClosedNode() throws IOException {
		root.close();
		root.element("child");
	}

	@Test(expected = IOException.class)
	public void testAddTextToClosedNode() throws IOException {
		root.close();
		root.text("text");
	}

	@Test
	public void testNestedElement() throws IOException {
		root.element("world");
		root.close();
		assertEquals("<root><world/></root>", buffer.toString());
	}

	@Test
	public void test2NestedElements() throws IOException {
		root.element("world");
		root.element("universe");
		root.close();
		assertEquals("<root><world/><universe/></root>", buffer.toString());
	}

	@Test
	public void testText() throws IOException {
		root.text("world");
		root.close();
		assertEquals("<root>world</root>", buffer.toString());
	}

	@Test
	public void testMixedContent() throws IOException {
		root.element("tag1");
		root.text("world");
		root.element("tag2");
		root.close();
		assertEquals("<root><tag1/>world<tag2/></root>", buffer.toString());
	}

	@Test
	public void testQuotedText() throws IOException {
		root.text("<black&white\">");
		root.close();
		assertEquals("<root>&lt;black&amp;white&quot;&gt;</root>", buffer
				.toString());
	}

	@Test
	public void testAttributes() throws IOException {
		root.attr("id", "12345").attr("quote", "<\">");
		root.close();
		assertEquals("<root id=\"12345\" quote=\"&lt;&quot;&gt;\"/>", buffer
				.toString());
	}

	@Test(expected = IOException.class)
	public void testInvalidAttributeOutput1() throws IOException {
		root.text("text");
		root.attr("id", "12345");
	}

	@Test(expected = IOException.class)
	public void testInvalidAttributeOutput2() throws IOException {
		root.element("child");
		root.attr("id", "12345");
	}

}
